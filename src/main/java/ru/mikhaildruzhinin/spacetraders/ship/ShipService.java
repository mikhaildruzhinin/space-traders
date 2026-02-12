package ru.mikhaildruzhinin.spacetraders.ship;

import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import ru.mikhaildruzhinin.spacetraders.generated.client.api.FleetApi;
import ru.mikhaildruzhinin.spacetraders.generated.client.model.*;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class ShipService {

    private static final Logger LOG = Logger.getLogger(ShipService.class);

    @RestClient
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

    public Uni<ShipCargo> ensureExtraction(
        ShipSymbol ship,
        Set<ContractDeliverGood> requiredResources,
        Waypoint waypoint
    ) {
        // TODO: check if ship is in orbit first
        Set<String> requiredTradeSymbols = requiredResources.stream()
            .map(ContractDeliverGood::getTradeSymbol)
            .collect(Collectors.toSet());

        boolean hasMarketplace = waypoint.getTraits()
            .stream()
            .map(WaypointTrait::getSymbol)
            .collect(Collectors.toSet())
            .contains(WaypointTraitSymbol.MARKETPLACE);

        // TODO: encapsulate client calls
        return fleetApi.orbitShip(ship.getSymbol())
            .chain(() -> extractResources(ship))
            .invoke(r -> LOG.infof("Resources extracted: %s", r.toString()))
            .flatMap(x -> {
                ExtractionYield extractionYield = x.getExtraction().getYield();
                boolean isRequired = requiredTradeSymbols.contains(extractionYield.getSymbol().value());

                if (hasMarketplace && !isRequired) {
                    return fleetApi.dockShip(ship.symbol())
                        .chain(() -> sellCargo(ship, extractionYield))
                        .invoke(r -> LOG.infof("Resources sold: %s", r.toString()))
                        .chain(() -> fleetApi.getMyShip(ship.symbol()));
                }
                return fleetApi.getMyShip(ship.symbol());
            }).map(GetMyShip200Response::getData)
            .call(r ->
                Uni.createFrom().voidItem().onItem().delayIt().by(
                    Duration.ofSeconds(r.getCooldown().getRemainingSeconds())
                )
            ).repeat().until(r -> {
                ShipCargo cargo = r.getCargo();
                return cargo.getUnits() >= cargo.getCapacity();
            }).select().last().toUni().map(Ship::getCargo);
    }

    @CacheInvalidateAll(cacheName = "ships")
    protected Uni<ExtractResources201ResponseData> extractResources(ShipSymbol ship) {
        return fleetApi.extractResources(ship.getSymbol())
            .map(ExtractResources201Response::getData);
    }

    @CacheInvalidateAll(cacheName = "agent")
    @CacheInvalidateAll(cacheName = "ships")
    public Uni<PurchaseCargo201ResponseData> sellCargo(Ship ship, ShipCargoItem cargo) {
        SellCargoRequest scr = new SellCargoRequest();
        scr.setSymbol(cargo.getSymbol());
        scr.setUnits(cargo.getUnits());
        return fleetApi.sellCargo(ship.getSymbol(), scr)
            .map(SellCargo201Response::getData);
    }

    @CacheInvalidateAll(cacheName = "agent")
    @CacheInvalidateAll(cacheName = "ships")
    public Uni<PurchaseCargo201ResponseData> sellCargo(ShipSymbol ship, ExtractionYield yield) {
        SellCargoRequest scr = new SellCargoRequest();
        scr.setSymbol(yield.getSymbol());
        scr.setUnits(yield.getUnits());
        return fleetApi.sellCargo(ship.getSymbol(), scr)
            .map(SellCargo201Response::getData);
    }

    private Uni<Ship> fetchShip(Uni<ShipSymbol> shipSymbol) {
        return shipSymbol.flatMap(s -> fleetApi.getMyShip(s.symbol()))
            .map(GetMyShip200Response::getData);
    }

}
