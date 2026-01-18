package ru.mikhaildruzhinin.spacetraders;

import io.quarkus.cache.CacheResult;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
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

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance index(String status);

        public static native TemplateInstance agent(Agent agent);

        public static native TemplateInstance waypoint(Waypoint waypoint);

        public static native TemplateInstance contracts(List<Contract> contracts);
    }

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public Uni<TemplateInstance> index() {
        // TODO: cache API response
        return globalApi.getStatus()
            .map(GetStatus200Response::getStatus)
            .map(Templates::index);
    }

    @GET
    @Path("/agent")
    @Produces(MediaType.TEXT_HTML)
    public Uni<TemplateInstance> agent() {
        return getMyAgent()
            .map(GetMyAgent200Response::getData)
            .map(Templates::agent);
    }

    @GET
    @Path("/starting-location")
    @Produces(MediaType.TEXT_HTML)
    @CacheResult(cacheName = "statring-location")
    public Uni<TemplateInstance> startingLocation() {

        return getMyAgent()
            .map(GetMyAgent200Response::getData)
            .map(Agent::getHeadquarters)
            .map(WaypointSymbol::from)
            .flatMap(waypoint -> systemsApi.getWaypoint(waypoint.system(), waypoint.waypoint()))
            .map(GetWaypoint200Response::getData)
            .map(Templates::waypoint);
    }

    @CacheResult(cacheName = "my-agent")
    protected Uni<GetMyAgent200Response> getMyAgent() {
        return agentsApi.getMyAgent();
    }

    @GET
    @Path("/contracts")
    @Produces(MediaType.TEXT_HTML)
    @CacheResult(cacheName = "contracts")
    public Uni<TemplateInstance> contracts() {
        // TODO: persist contract
        // TODO: add pagination
        return contractsApi.getContracts(1, 20)
            .map(GetContracts200Response::getData)
            .map(Templates::contracts);
    }
}
