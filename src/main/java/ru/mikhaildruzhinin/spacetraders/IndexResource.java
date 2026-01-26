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
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import ru.mikhaildruzhinin.spacetraders.generated.client.api.*;
import ru.mikhaildruzhinin.spacetraders.generated.client.model.*;

import java.util.List;

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
    public Uni<TemplateInstance> negotiateContract() {
        // TODO: testing
        // FIXME: 2026-01-26 06:34:31,450 ERROR [io.ver.cor.htt.imp.HttpClientRequestImpl] null:-1 (vert.x-eventloop-thread-1) The timeout period of 30000ms has been exceeded while executing GET /v2/systems/X1-HD80/waypoints/X1-HD80-H48 for server null
        // For now there's an assumption that an agent can only ever be a member of his starting faction.
        return fetchMyAgent().map(Agent::getStartingFaction)
            .flatMap(this::findDockedShipWithinFaction)
            .flatMap(ship -> getNegotiateContract201ResponseUni(ship).replaceWithVoid())
            .onFailure(ClientWebApplicationException.class).recoverWithItem((Void) null)
            .flatMap(ignored -> fetchContracts())
            .map(Templates::contracts);
    }

    @CacheInvalidateAll(cacheName = "contracts")
    protected Uni<Contract> getNegotiateContract201ResponseUni(Ship ship) {
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
        // TODO: cache ships
        // TODO: add pagination
        return fleetApi.getMyShips(1, 20)
            .map(GetMyShips200Response::getData)
            .onItem().transformToMulti(Multi.createFrom()::iterable)
            .filter(this::checkIfDocked)
            .onItem().transformToUniAndMerge(this::attachWaypointFaction)
            .filter(sf -> sf.factionSymbol() == factionSymbol)
            .map(ShipWithFactionSymbol::ship)
            .select().first().toUni()
            .onItem().ifNull().failWith(() -> new RuntimeException("No docked ships within faction " + faction)); // TODO: add custom exception
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

    @POST
    @Path("/accept-contract")
    @Produces(MediaType.TEXT_HTML)
    public Uni<TemplateInstance> accept(@FormParam("contract_id") String contractId) {
        return acceptContract(contractId)
            .replaceWithVoid()
            .onFailure(ClientWebApplicationException.class).recoverWithItem((Void) null)
            .flatMap(ignored -> fetchContracts())
            .map(Templates::contracts);
    }

    @CacheInvalidateAll(cacheName = "contracts")
    protected Uni<Contract> acceptContract(String contractId) {
        return contractsApi.acceptContract(contractId)
            .map(response -> response.getData().getContract());
    }

    @CacheResult(cacheName = "contracts")
    protected Uni<List<Contract>> fetchContracts() {
        // TODO: add pagination
        return contractsApi.getContracts(1, 20)
            .map(GetContracts200Response::getData);
    }
}
