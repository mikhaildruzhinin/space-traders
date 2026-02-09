package ru.mikhaildruzhinin.spacetraders.ship;

import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import ru.mikhaildruzhinin.spacetraders.generated.client.api.FleetApi;
import ru.mikhaildruzhinin.spacetraders.generated.client.model.*;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@ApplicationScoped
public class ShipService {

    private static final Logger LOG = Logger.getLogger(ShipService.class);

    @Inject
    FleetApi fleetApi;

    // TODO: add pagination
    @CacheResult(cacheName = "ships")
    public Uni<List<Ship>> fetchShips() {
        return fleetApi.getMyShips(1, 20)
            .map(GetMyShips200Response::getData);
    }

    @CacheInvalidateAll(cacheName = "agent")
    @CacheInvalidateAll(cacheName = "ships")
    public Uni<Ship> purchaseShip(Shipyard shipyard, ShipType type) {
        PurchaseShipRequest psr = new PurchaseShipRequest();
        psr.setShipType(type);
        psr.setWaypointSymbol(shipyard.getSymbol());
        return fleetApi.purchaseShip(psr)
            .map(r -> r.getData().getShip())
            .invoke(s -> LOG.infof("Purchased ship: %s", s.toString()));
    }

    @CacheInvalidateAll(cacheName = "ships")
    public Uni<NavigateShip200Response> startNavigation(ShipSymbol ship, Waypoint destination) {
        // TODO: handle events
        // class NavigateShip200ResponseData {
        //     ...
        //     events: [class ShipConditionEvent {
        //         symbol: THERMAL_STRESS
        //         component: FRAME
        //         name: Thermal Stress
        //         description: Experiencing extreme temperature fluctuations during navigation induced thermal stress on the ship's frame. Although structural integrity remains uncompromised, prolonged exposure may lead to material fatigue.
        //     }]
        // }
        NavigateShipRequest nsr = new NavigateShipRequest();
        nsr.setWaypointSymbol(destination.getSymbol());
        return fleetApi.orbitShip(ship.getSymbol())
            .chain(() -> fleetApi.navigateShip(ship.getSymbol(), nsr))
            .invoke(r -> LOG.infof("Started navigation: %s", r.toString()));
    }

    @CacheInvalidateAll(cacheName = "ships")
    public Uni<ShipNav> finishNavigation(ShipSymbol ship, ShipNav nav) {
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
            .map(r -> r.getData().getNav())
            .invoke(s -> LOG.infof("Finished navigation: %s", s.toString()));
    }

    @CacheInvalidateAll(cacheName = "agent")
    @CacheInvalidateAll(cacheName = "ships")
    public Uni<RefuelShip200ResponseData> refuelShip(ShipSymbol ship, ShipFuel fuel) {
        // TODO: handle unhappy paths
        RefuelShipRequest rsr = new RefuelShipRequest();
        rsr.setUnits(fuel.getConsumed().getAmount());
        return fleetApi.refuelShip(ship.getSymbol(), rsr).map(RefuelShip200Response::getData);
    }

    @CacheInvalidateAll(cacheName = "ships")
    public Uni<ExtractResources201ResponseData> extractResources(ShipSymbol ship) {
        return fleetApi.orbitShip(ship.getSymbol())
            .chain(() -> fleetApi.extractResources(ship.getSymbol()))
            .map(ExtractResources201Response::getData)
            .invoke(r -> LOG.infof("Resources extracted: %s", r.toString()));
    }
}
