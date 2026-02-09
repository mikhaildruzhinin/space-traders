package ru.mikhaildruzhinin.spacetraders;

import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import ru.mikhaildruzhinin.spacetraders.generated.client.api.*;
import ru.mikhaildruzhinin.spacetraders.generated.client.model.*;
import ru.mikhaildruzhinin.spacetraders.ship.ShipService;
import ru.mikhaildruzhinin.spacetraders.ship.ShipSymbol;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Path("/")
public class IndexResource {

    private static final Logger LOG = Logger.getLogger(IndexResource.class);

    public static final Duration STREAM_UPDATE_FREQUENCY = Duration.ofSeconds(2);

    @RestClient
    @Inject
    GlobalApi globalApi;

    @RestClient
    @Inject
    AgentsApi agentsApi;

    @RestClient
    @Inject
    SystemsApi systemsApi;

    @RestClient
    @Inject
    ContractsApi contractsApi;

    @RestClient
    @Inject
    FleetApi fleetApi;

    @Inject
    Sse sse;

    @Inject
    ShipService shipService;

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance index();

        public static native TemplateInstance agent(Agent agent);

        public static native TemplateInstance contracts(List<Contract> contracts);

        public static native TemplateInstance ships(List<Ship> ships);
    }

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public Uni<TemplateInstance> index() {
        return Uni.createFrom().item(Templates.index());
    }


    @GET
    @Path("/status")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<OutboundSseEvent> status() {
        Duration streamUpdateFrequency = Duration.ofSeconds(2);
        return Multi.createFrom().ticks().every(streamUpdateFrequency)
            .onOverflow()
            .drop()
            .onItem().transformToUniAndConcatenate(tick -> fetchStatus())
            .map(status -> sse.newEventBuilder().name("status").data(status).build());
    }

    @GET
    @Path("/agent")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<OutboundSseEvent> agent() {
        Duration streamUpdateFrequency = Duration.ofSeconds(2);

        return Multi.createFrom().ticks().every(streamUpdateFrequency)
            .onOverflow()
            .drop()
            .onItem().transformToUniAndConcatenate(tick ->
                fetchMyAgent().map(Templates::agent)
                    .flatMap(t -> Uni.createFrom().completionStage(t.renderAsync()))
            ).map(e -> sse.newEventBuilder().name("agent").data(e).build());
    }

    @GET
    @Path("/contracts")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    // TODO: check out ServerSentEvent type
    public Multi<OutboundSseEvent> contracts() {
        Duration streamUpdateFrequency = Duration.ofSeconds(2);

        return Multi.createFrom().ticks().every(streamUpdateFrequency)
            .onOverflow()
            .drop()
            .onItem().transformToUniAndConcatenate(tick ->
                fetchContracts().map(Templates::contracts)
                    .flatMap(t -> Uni.createFrom().completionStage(t.renderAsync()))
            ).map(e -> sse.newEventBuilder().name("contracts").data(e).build());
    }

    @GET
    @Path("/ships")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    // TODO: check out ServerSentEvent type
    public Multi<OutboundSseEvent> ships() {

        return Multi.createFrom().ticks().every(STREAM_UPDATE_FREQUENCY)
            .onOverflow()
            .drop()
            .onItem().transformToUniAndConcatenate(tick ->
                shipService.fetchShips().map(Templates::ships)
                    .flatMap(t -> Uni.createFrom().completionStage(t.renderAsync()))
            ).map(e -> sse.newEventBuilder().name("ships").data(e).build());
    }

    @CacheResult(cacheName = "status")
    protected Uni<String> fetchStatus() {
        return globalApi.getStatus().map(GetStatus200Response::getStatus);
    }

    @CacheResult(cacheName = "agent")
    protected Uni<Agent> fetchMyAgent() {
        return agentsApi.getMyAgent().map(GetMyAgent200Response::getData);
    }

    @CacheInvalidateAll(cacheName = "contracts")
    protected Uni<Contract> negotiateContract(Ship ship) {
        return contractsApi.negotiateContract(ship.getSymbol())
            .map(response -> response.getData().getContract());
    }



    private boolean checkIfDocked(Ship s) {
        return s.getNav().getStatus() == ShipNavStatus.DOCKED;
    }

    @CacheResult(cacheName = "contracts")
    protected Uni<List<Contract>> fetchContracts() {
        // TODO: add pagination
        return contractsApi.getContracts(1, 20)
            .map(GetContracts200Response::getData);
    }

    @POST
    @Path("/submit")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> submit() {

        Uni<WaypointSymbol> homeSystem = fetchMyAgent().map(a -> WaypointSymbol.from(a.getHeadquarters()))
            .invoke(w -> LOG.infof("Home system: %s", w.toString()))
            .memoize()
            .indefinitely();

        Uni<String> contractId = ensureContractAccepted()
            .invoke(c -> LOG.infof("Accepted contract: %s", c.toString()))
            .map(Contract::getId)
            .memoize()
            .indefinitely(); // TODO: research .memoize.until(...)

        // Reuse cached ship symbol ONLY!
        Uni<ShipSymbol> shipSymbol = contractId
            .chain(() -> homeSystem.flatMap(this::ensureShipPurchased))
            .map(ShipSymbol::from)
            .memoize()
            .indefinitely();


        // TODO: verify there's only one ENGINEERED_ASTEROID
        Uni<Waypoint> asteroid = shipSymbol.chain(() ->
            homeSystem.flatMap(hs -> findWaypointsInSystem(hs.system(), WaypointType.ENGINEERED_ASTEROID, null))
        )
            .map(List::getFirst)
            .invoke(w -> LOG.infof("Located destination: %s", w.toString()));

        Uni<NavigateShip200ResponseData> navigationStart = Uni.combine().all().unis(shipSymbol, asteroid).asTuple()
            .flatMap(t -> shipService.startNavigation(t.getItem1(), t.getItem2()))
            .map(NavigateShip200Response::getData)
            .memoize()
            .indefinitely();

        Uni<ShipNav> navigationFinish = Uni.combine().all().unis(shipSymbol, navigationStart).asTuple()
            .flatMap(t -> shipService.finishNavigation(t.getItem1(), t.getItem2().getNav()));

        Uni<RefuelShip200ResponseData> refueling = navigationFinish.chain(() ->
            Uni.combine().all().unis(shipSymbol, navigationStart).asTuple()
                .flatMap(t -> shipService.refuelShip(t.getItem1(), t.getItem2().getFuel()))
                .invoke(r -> LOG.infof("Ship refueled: %s", r.toString()))
        );

        Uni<Set<String>> requiredResources = contractId.flatMap(c -> contractsApi.getContract(c)
                .map(r ->
                    r.getData().getTerms()
                        .getDeliver()
                        .stream()
                        .filter(g -> g.getUnitsFulfilled() >= g.getUnitsRequired())
                        .map(ContractDeliverGood::getTradeSymbol)
                        .collect(Collectors.toSet())
                ))
            .invoke(r -> LOG.infof("Resources: %s", r.toString()));

        Uni<ExtractResources201ResponseData> extraction = refueling.chain(() ->
            shipSymbol.flatMap(this::ensureExtraction)
        );

        Uni<Market> market = Uni.createFrom().voidItem().chain(() ->
            asteroid.flatMap(a -> systemsApi.getMarket(a.getSystemSymbol(), a.getSymbol()))
            .map(GetMarket200Response::getData)
            .invoke(m -> LOG.infof("Market: %s", m.toString()))
        );

        Uni<DockShip200ResponseData> docked = shipSymbol.flatMap(s -> fleetApi.dockShip(s.symbol()))
            .map(DockShip200Response::getData);

        Uni<List<PurchaseCargo201ResponseData>> cargoSold = docked.chain(() -> Uni.combine()
            .all().unis(fetchShip(shipSymbol), requiredResources).asTuple()
            .map(t ->
                t.getItem1()
                    .getCargo()
                    .getInventory()
                    .stream()
                    .peek(r -> LOG.infof("Resource in cargo hold: %s", r.toString()))
                    .filter(cargoItem -> !t.getItem2().contains(cargoItem.getSymbol().value()))
                    .map(cargoItem -> Tuple2.of(t.getItem1(), cargoItem))
                    .toList()
            )
            .onItem().transformToMulti(Multi.createFrom()::iterable)
            .onItem().transformToUniAndConcatenate(t ->
                sellCargo(t.getItem1(), t.getItem2()).onItem().delayIt().by(Duration.ofMillis(600))
            )
            .invoke(r -> LOG.infof("Cargo sold: %s", r.toString()))
            .collect().asList());
        return cargoSold.replaceWith(Response.noContent().build());
    }

    @CacheInvalidateAll(cacheName = "agent")
    @CacheInvalidateAll(cacheName = "ships")
    protected Uni<PurchaseCargo201ResponseData> sellCargo(Ship ship, ShipCargoItem cargo) {
        SellCargoRequest scr = new SellCargoRequest();
        scr.setSymbol(cargo.getSymbol());
        scr.setUnits(cargo.getUnits());
        return fleetApi.sellCargo(ship.getSymbol(), scr)
            .map(SellCargo201Response::getData);
    }

    private Uni<Ship> fetchShip(Uni<ShipSymbol> shipSymbol) {
        return shipSymbol.flatMap(s -> fleetApi.getMyShip(s.symbol()))
            .map(GetMyShip200Response::getData);
    }

    protected Uni<ExtractResources201ResponseData> ensureExtraction(ShipSymbol ship) {
        // TODO: check if ship is in orbit first
        // TODO: sell immediately if not in requiredResources
        return shipService.extractResources(ship)
            .call(r ->
                Uni.createFrom().voidItem().onItem().delayIt().by(
                    Duration.ofSeconds(r.getCooldown().getRemainingSeconds())
                )
            ).repeat().until(r -> {
                ShipCargo cargo = r.getCargo();
                return cargo.getUnits() >= cargo.getCapacity();
            }).select().last().toUni();
    }

    private Uni<Contract> ensureContractAccepted() {
        return fetchContracts()
            .flatMap(this::ensureContractExists)
            .flatMap(contracts -> acceptContract(contracts.getFirst().getId()));
    }

    private Uni<List<Contract>> ensureContractExists(List<Contract> contracts) {
        return shipService.fetchShips().flatMap(ships -> {
            if (contracts == null || contracts.isEmpty()) {
                Optional<Ship> maybeDockedShip = ships.stream().filter(this::checkIfDocked).findAny();
                if (maybeDockedShip.isPresent()) {
                    // TODO: handle being unable to negotiate a new contract
                    return negotiateContract(maybeDockedShip.get())
                        .replaceWithVoid()
                        .replaceWith(fetchContracts());
                }
            }
            return Uni.createFrom().item(contracts);
        });
    }

    @CacheInvalidateAll(cacheName = "agent")
    @CacheInvalidateAll(cacheName = "contracts")
    protected Uni<Contract> acceptContract(String contractId) {
        return contractsApi.acceptContract(contractId)
            .map(response -> response.getData().getContract());
    }

    private Uni<Ship> ensureShipPurchased(WaypointSymbol system) {
        return findWaypointsInSystem(system.system(), null, List.of(WaypointTraitSymbol.SHIPYARD))
            .flatMap(this::getShipyards)
            .map(List::getFirst)
            .flatMap(s -> shipService.purchaseShip(s, ShipType.SHIP_MINING_DRONE));
    }

    private Uni<List<Shipyard>> getShipyards(List<Waypoint> waypoints) {
        return Multi.createFrom().iterable(waypoints)
            .onItem().transformToUniAndConcatenate(w -> systemsApi.getShipyard(w.getSystemSymbol(), w.getSymbol()))
            .map(GetShipyard200Response::getData)
            .filter(x -> (x.getShips() != null && !x.getShips().isEmpty()))
            .collect().asList();
    }

    private Uni<List<Waypoint>> findWaypointsInSystem(
        String system,
        WaypointType type,
        List<WaypointTraitSymbol> traits
    ) {
        int initialPageSize = 20;
        return systemsApi.getSystemWaypoints(system, 1, initialPageSize, type, traits)
            .flatMap(result -> {
                List<Waypoint> firstWaypoints = result.getData();
                List<Waypoint> all;
                all = Objects.requireNonNullElseGet(firstWaypoints, () -> new ArrayList<>(Collections.emptyList()));

                Meta meta = result.getMeta();
                if (meta == null || meta.getTotal() == null || meta.getLimit() == null) {
                    return Uni.createFrom().item(all);
                }

                int total = meta.getTotal();
                int pageSize = meta.getLimit();
                int totalPages = (int) Math.ceil(total / (double) pageSize);

                if (totalPages <= 1) {
                    return Uni.createFrom().item(all);
                }

                return Multi.createFrom().range(2, totalPages + 1)
                    .onItem().transformToUniAndConcatenate(page ->
                        systemsApi.getSystemWaypoints(
                            system,
                            page,
                            pageSize,
                            type,
                            traits
                        )
                    )
                    .map(GetSystemWaypoints200Response::getData)
                    .onItem().transformToMulti(Multi.createFrom()::iterable)
                    .merge()
                    .collect().asList()
                    .map(x -> {
                        all.addAll(x);
                        return all;
                    });
            });
    }
}
