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
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import ru.mikhaildruzhinin.spacetraders.generated.client.api.*;
import ru.mikhaildruzhinin.spacetraders.generated.client.model.*;

import java.util.*;

@Path("/")
public class IndexResource {

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

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance index();

        public static native TemplateInstance agent(Agent agent);

        public static native TemplateInstance waypoint(Waypoint waypoint);

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
    @Produces(MediaType.TEXT_PLAIN)
    @CacheResult(cacheName = "status")
    public Uni<String> status() {
        return globalApi.getStatus()
            .map(GetStatus200Response::getStatus);
    }

    @GET
    @Path("/agent")
    @Produces(MediaType.TEXT_HTML)
    public Uni<TemplateInstance> agent() {
        return fetchMyAgent().map(Templates::agent);
    }

    @GET
    @Path("/starting-location")
    @Produces(MediaType.TEXT_HTML)
    @CacheResult(cacheName = "starting-location")
    public Uni<TemplateInstance> startingLocation() {
        return fetchMyAgent()
            .map(agent -> WaypointSymbol.from(agent.getHeadquarters()))
            .flatMap(waypoint -> systemsApi.getWaypoint(waypoint.system(), waypoint.waypoint()))
            .map(GetWaypoint200Response::getData)
            .map(Templates::waypoint);
    }

    @CacheResult(cacheName = "my-agent")
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

    @POST
    @Path("/negotiate-contract")
    @Produces(MediaType.TEXT_HTML)
    public Uni<TemplateInstance> negotiate() {
        // TODO: testing
        // FIXME: 2026-01-26 06:34:31,450 ERROR [io.ver.cor.htt.imp.HttpClientRequestImpl] null:-1 (vert.x-eventloop-thread-1) The timeout period of 30000ms has been exceeded while executing GET /v2/systems/X1-HD80/waypoints/X1-HD80-H48 for server null
        // For now there's an assumption that an agent can only ever be a member of his starting faction.
        return fetchMyAgent().map(Agent::getStartingFaction)
            .flatMap(this::findDockedShipWithinFaction)
            .flatMap(ship -> negotiateContract(ship).replaceWithVoid())
            .onFailure(ClientWebApplicationException.class).recoverWithItem((Void) null) // TODO: log exception
            .flatMap(ignored -> fetchContracts())
            .map(Templates::contracts);
    }

    @CacheInvalidateAll(cacheName = "contracts")
    protected Uni<Contract> negotiateContract(Ship ship) {
        return contractsApi.negotiateContract(ship.getSymbol())
            .map(response -> response.getData().getContract());
    }

    private Uni<Ship> findDockedShipWithinFaction(String faction) {
        FactionSymbol factionSymbol;
        try {
            factionSymbol = FactionSymbol.fromString(faction);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().failure(e);
        }

        return fetchShips()
            .onItem().transformToMulti(Multi.createFrom()::iterable)
            .filter(this::checkIfDocked)
            .onItem().transformToUniAndConcatenate(this::attachWaypointFaction)
            .filter(sf -> sf.factionSymbol() == factionSymbol)
            .map(ShipWithFactionSymbol::ship)
            .select().first().toUni()
            .onItem().ifNull().failWith(() -> new RuntimeException("No docked ships within faction " + faction)); // TODO: add custom exception
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

    private Uni<ShipWithFactionSymbol> attachWaypointFaction(Ship s) {
        ShipNav shipNav = s.getNav();
        String systemSymbol = shipNav.getSystemSymbol();
        String waypointSymbol = shipNav.getWaypointSymbol();

        return systemsApi.getWaypoint(systemSymbol, waypointSymbol)
            .map(response -> response.getData().getFaction().getSymbol())
            .map(f -> ShipWithFactionSymbol.from(s, f));
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

        Uni<Contract> acceptedContract = fetchContracts().flatMap(contracts ->
                fetchShips().flatMap(ships -> {
                    if (contracts == null || contracts.isEmpty()) {
                        Optional<Ship> maybeDockedShip = ships.stream().filter(this::checkIfDocked).findAny();
                        if (maybeDockedShip.isPresent()) {
                            // TODO: handle being unable to negotiate a new contract
                            return negotiateContract(maybeDockedShip.get())
                                .replaceWithVoid()
                                .replaceWith(contracts);
                        }
                    }
                    return Uni.createFrom().item(contracts);
                }))
            .flatMap(contracts -> acceptContract(contracts.getFirst().getId()));

        return acceptedContract.replaceWithVoid()
            .replaceWith(fetchMyAgent())
            .map(agent -> WaypointSymbol.from(agent.getHeadquarters()))
            .flatMap(hq ->
                findWaypointsInSystem(hq.system(), null, List.of(WaypointTraitSymbol.SHIPYARD))
            )
            .flatMap(this::getShipyards)
            .map(List::getFirst)
            .flatMap(s -> purchaseShip(s, ShipType.SHIP_MINING_DRONE))
            .replaceWith(Response.noContent().build());
    }

    @CacheInvalidateAll(cacheName = "contracts")
    protected Uni<Contract> acceptContract(String contractId) {
        return contractsApi.acceptContract(contractId)
            .map(response -> response.getData().getContract());
    }

    private Uni<List<Shipyard>> getShipyards(List<Waypoint> waypoints) {
        return Multi.createFrom().iterable(waypoints)
            .onItem().transformToUniAndConcatenate(w -> systemsApi.getShipyard(w.getSystemSymbol(), w.getSymbol()))
            .map(GetShipyard200Response::getData)
            .filter(x -> (x.getShips() != null && !x.getShips().isEmpty()))
            .collect().asList();
    }

    @SuppressWarnings("SameParameterValue")
    @CacheInvalidateAll(cacheName = "my-agent")
    @CacheInvalidateAll(cacheName = "my-ships")
    protected Uni<PurchaseShip201ResponseData> purchaseShip(Shipyard shipyard, ShipType type) {
        PurchaseShipRequest psr = new PurchaseShipRequest();
        psr.setShipType(type);
        psr.setWaypointSymbol(shipyard.getSymbol());
        return fleetApi.purchaseShip(psr).map(PurchaseShip201Response::getData);
    }

    private Uni<List<Waypoint>> findWaypointsInSystem(
        String system,
        @SuppressWarnings("SameParameterValue") WaypointType type,
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

    @GET
    @Path("/ships")
    @Produces(MediaType.APPLICATION_JSON)
    @CacheResult(cacheName = "my-ships")
    public Uni<TemplateInstance> ships() {
        // TODO: add pagination
        return fleetApi.getMyShips(1, 20)
            .map(GetMyShips200Response::getData).
            map(Templates::ships);
    }
}
