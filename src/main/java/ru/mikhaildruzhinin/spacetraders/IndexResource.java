package ru.mikhaildruzhinin.spacetraders;

import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
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

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;

@Path("/")
public class IndexResource {

    private static final Logger LOG = Logger.getLogger(IndexResource.class);

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

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance index();

        public static native TemplateInstance agent();

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
    @Path("/events")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    // TODO: check out ServerSentEvent type
    public Multi<OutboundSseEvent> events() {
        Duration streamUpdateFrequency = Duration.ofSeconds(2);

        Multi<OutboundSseEvent> statusEventStream = Multi.createFrom().ticks().every(streamUpdateFrequency)
            .onItem().transformToUniAndConcatenate(tick -> fetchStatus())
            .map(status -> sse.newEventBuilder().name("status").data(status).build());

//        Multi<OutboundSseEvent> agentEventStream = Multi.createFrom().ticks().every(streamUpdateFrequency)
//            .onItem().transformToUniAndConcatenate(tick ->
//                fetchMyAgent().map(UiEvent.UiAgentEvent::from)
//            ).map(e -> sse.newEventBuilder().name("agent").data(e).build());
//
//        // TODO: event with a current state of contracts
//        Multi<OutboundSseEvent> contractEventStream = Multi.createFrom().ticks().every(streamUpdateFrequency)
//            .onItem().transformToUniAndConcatenate(tick -> fetchContracts())
//            .onItem().transformToMultiAndMerge(Multi.createFrom()::iterable)
//            .map(UiEvent.UiContractEvent::from)
//            .map(e -> sse.newEventBuilder().name("contracts").data(e).build());;
//
//        Multi<OutboundSseEvent> shipEventStream = Multi.createFrom().ticks().every(streamUpdateFrequency)
//            .onItem().transformToUniAndConcatenate(tick -> fetchShips())
//            .map(UiEvent.UiShipEvent::from)
//            .map(e -> sse.newEventBuilder().name("ships").data(e).build());

        return Multi.createBy().merging().streams(
            statusEventStream
//            agentEventStream,
//            contractEventStream,
//            shipEventStream
        );
    }

    @CacheResult(cacheName = "status")
    protected Uni<String> fetchStatus() {
        return globalApi.getStatus().map(GetStatus200Response::getStatus);
    }

    @CacheResult(cacheName = "agent")
    protected Uni<Agent> fetchMyAgent() {
        return agentsApi.getMyAgent().map(GetMyAgent200Response::getData);
    }

    @GET
    @Path("/contracts")
    @Produces(MediaType.TEXT_HTML)
    public Uni<TemplateInstance> contracts() {
        // TODO: persist contract
        return fetchContracts().map(Templates::contracts);
    }

    @CacheInvalidateAll(cacheName = "contracts")
    protected Uni<Contract> negotiateContract(Ship ship) {
        return contractsApi.negotiateContract(ship.getSymbol())
            .map(response -> response.getData().getContract());
    }

    // TODO: add pagination
    @CacheResult(cacheName = "ships")
    protected Uni<List<Ship>> fetchShips() {
        return fleetApi.getMyShips(1, 20)
            .map(GetMyShips200Response::getData);
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

        Uni<Ship> ship = ensureContractAccepted()
            .invoke(c -> LOG.infof("Accepted contract: %s", c.toString()))
            .chain(() -> homeSystem.flatMap(this::ensureShipPurchased))
            .invoke(s -> LOG.infof("Purchased ship: %s", s.toString()))
            .memoize()
            .indefinitely();

        // TODO: verify there's only one ENGINEERED_ASTEROID
        Uni<Waypoint> asteroid = ship.chain(() ->
            homeSystem.flatMap(hs -> findWaypointsInSystem(hs.system(), WaypointType.ENGINEERED_ASTEROID, null))
        )
            .map(List::getFirst)
            .invoke(w -> LOG.infof("Located destination: %s", w.toString()));

        Uni<NavigateShip200ResponseData> navigationStart = Uni.combine().all().unis(ship, asteroid).asTuple()
            .flatMap(t -> startNavigation(t.getItem1(), t.getItem2()))
            .map(NavigateShip200Response::getData)
            .invoke(r -> LOG.infof("Started navigation: %s", r.toString()))
            .memoize()
            .indefinitely();

        Uni<ShipNav> navigationFinish = Uni.combine().all().unis(ship, navigationStart).asTuple()
            .flatMap(t -> finishNavigation(t.getItem1(), t.getItem2().getNav()))
            .invoke(r -> LOG.infof("Finished navigation: %s", r.toString()));

        Uni<RefuelShip200ResponseData> refuel = navigationFinish.chain(() -> Uni.combine().all().unis(ship, navigationStart).asTuple()
            .flatMap(t -> refuelShip(t.getItem1(), t.getItem2().getFuel()))
            .invoke(r -> LOG.infof("Ship refueled: %s", r.toString()))
            .map(RefuelShip200Response::getData));
        return refuel.replaceWith(Response.noContent().build());
    }

    @CacheInvalidateAll(cacheName = "agent")
    @CacheInvalidateAll(cacheName = "ships")
    protected Uni<RefuelShip200Response> refuelShip(Ship ship, ShipFuel fuel) {
        // TODO: handle unhappy paths
        RefuelShipRequest rsr = new RefuelShipRequest();
        rsr.setUnits(fuel.getConsumed().getAmount());
        return fleetApi.refuelShip(ship.getSymbol(), rsr);
    }

    @CacheInvalidateAll(cacheName = "ships")
    protected Uni<ShipNav> finishNavigation(Ship ship, ShipNav nav) {
        ShipNavRoute route = nav.getRoute();
        OffsetDateTime departureTime = route.getDepartureTime();
        OffsetDateTime arrivalTime = route.getArrival();

        Duration flightDuration = Duration.between(departureTime, arrivalTime);
        if (flightDuration.isNegative()) {
            flightDuration = Duration.ZERO;
        }

        return Uni.createFrom().voidItem()
            .onItem().delayIt().by(flightDuration)
            .chain(() -> fleetApi.dockShip(ship.getSymbol()))
            .map(r -> r.getData().getNav());
    }

    @CacheInvalidateAll(cacheName = "ships")
    protected Uni<NavigateShip200Response> startNavigation(Ship ship, Waypoint destination) {
        NavigateShipRequest nsr = new NavigateShipRequest();
        nsr.setWaypointSymbol(destination.getSymbol());
        return fleetApi.orbitShip(ship.getSymbol()).chain(() -> fleetApi.navigateShip(ship.getSymbol(), nsr));
    }

    private Uni<Contract> ensureContractAccepted() {
        return fetchContracts()
            .flatMap(this::ensureContractExists)
            .flatMap(contracts -> acceptContract(contracts.getFirst().getId()));
    }

    private Uni<List<Contract>> ensureContractExists(List<Contract> contracts) {
        return fetchShips().flatMap(ships -> {
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

    @CacheInvalidateAll(cacheName = "contracts")
    protected Uni<Contract> acceptContract(String contractId) {
        return contractsApi.acceptContract(contractId)
            .map(response -> response.getData().getContract());
    }

    private Uni<Ship> ensureShipPurchased(WaypointSymbol system) {
        return findWaypointsInSystem(system.system(), null, List.of(WaypointTraitSymbol.SHIPYARD))
            .flatMap(this::getShipyards)
            .map(List::getFirst)
            .flatMap(s -> purchaseShip(s, ShipType.SHIP_MINING_DRONE));
    }

    private Uni<List<Shipyard>> getShipyards(List<Waypoint> waypoints) {
        return Multi.createFrom().iterable(waypoints)
            .onItem().transformToUniAndConcatenate(w -> systemsApi.getShipyard(w.getSystemSymbol(), w.getSymbol()))
            .map(GetShipyard200Response::getData)
            .filter(x -> (x.getShips() != null && !x.getShips().isEmpty()))
            .collect().asList();
    }

    @SuppressWarnings("SameParameterValue")
    @CacheInvalidateAll(cacheName = "agent")
    @CacheInvalidateAll(cacheName = "ships")
    protected Uni<Ship> purchaseShip(Shipyard shipyard, ShipType type) {
        PurchaseShipRequest psr = new PurchaseShipRequest();
        psr.setShipType(type);
        psr.setWaypointSymbol(shipyard.getSymbol());
        return fleetApi.purchaseShip(psr).map(r -> r.getData().getShip());
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
