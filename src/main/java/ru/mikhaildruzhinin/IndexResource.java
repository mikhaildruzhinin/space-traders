package ru.mikhaildruzhinin;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import ru.mikhaildruzhinin.spacetraders.generated.client.api.GlobalApi;
import ru.mikhaildruzhinin.spacetraders.generated.client.model.GetStatus200Response;

@Path("/")
public class IndexResource {

    @RestClient
    @Inject
    GlobalApi globalApi;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<GetStatus200Response> hello() {
        return globalApi.getStatus();
    }
}
