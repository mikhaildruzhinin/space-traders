package ru.mikhaildruzhinin.spacetraders.health;

import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import ru.mikhaildruzhinin.spacetraders.generated.client.api.GlobalApi;
import ru.mikhaildruzhinin.spacetraders.generated.client.model.GetStatus200Response;

import java.time.Duration;

@Readiness
@ApplicationScoped
public class ReadinessCheck implements AsyncHealthCheck {

    @RestClient
    @Inject
    GlobalApi globalApi;

    @Override
    public Uni<HealthCheckResponse> call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("SpaceTrader API check");
        return globalApi.getStatus()
            .ifNoItem().after(Duration.ofSeconds(2L)).fail()
            .map(GetStatus200Response::getStatus)
            .map(status -> responseBuilder.up().withData("status", status))
            .onFailure().recoverWithItem(t ->
                responseBuilder.down()
                    .withData("error", t.getClass().getSimpleName())
                    .withData("error_message", String.valueOf(t.getMessage()))
            )
            .map(HealthCheckResponseBuilder::build);
    }
}
