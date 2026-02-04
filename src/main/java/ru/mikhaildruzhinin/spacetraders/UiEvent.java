package ru.mikhaildruzhinin.spacetraders;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ru.mikhaildruzhinin.spacetraders.generated.client.model.*;

import java.time.Instant;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = UiEvent.UiStatusEvent.class, name = "status"),
    @JsonSubTypes.Type(value = UiEvent.UiAgentEvent.class, name = "agent"),
    @JsonSubTypes.Type(value = UiEvent.UiContractEvent.class, name = "contract"),
    @JsonSubTypes.Type(value = UiEvent.UiShipEvent.class, name = "ships")
})
public sealed interface UiEvent
    permits UiEvent.UiStatusEvent, UiEvent.UiAgentEvent, UiEvent.UiContractEvent, UiEvent.UiShipEvent {

    record UiStatusEvent(UiStatusEventData data) implements UiEvent {

        record UiStatusEventData(String status) {}

        public static UiStatusEvent from(String status) {
            return new UiStatusEvent(
                new UiStatusEventData(status)
            );
        }
    }

    record UiAgentEvent(UiAgentEventData data) implements UiEvent {

        record UiAgentEventData(
            String id,
            String symbol,
            String headquarters,
            Long credits,
            String faction,
            int shipsCount
        ) {}

        public static UiAgentEvent from(Agent agent) {
            return new UiAgentEvent(
                new UiAgentEventData(
                    agent.getAccountId(),
                    agent.getSymbol(),
                    agent.getHeadquarters(),
                    agent.getCredits(),
                    agent.getStartingFaction(),
                    agent.getShipCount()
                )
            );
        }
    }

    record UiContractEvent(UiContractEventData data) implements UiEvent {

        record UiContractEventData(
            String id,
            String factionSymbol,
            Contract.TypeEnum type,
            Instant deadline,
            Integer paymentOnAccepted,
            Integer paymentOnFulfilled,
            List<UiContractEventDelivery> delivery,
            Boolean isAccepted,
            Boolean isFulfilled,
            Instant expiration,
            Instant deadlineToAccept
        ) {}

        record UiContractEventDelivery(
            String tradeSymbol,
            String destinationSymbol,
            Integer unitsRequired,
            Integer unitsFulfilled
        ) {}

        public static UiContractEvent from(Contract contract) {

            List<UiContractEventDelivery> delivery = contract.getTerms()
                .getDeliver()
                .stream()
                .map(d ->
                    new UiContractEventDelivery(
                        d.getTradeSymbol(),
                        d.getDestinationSymbol(),
                        d.getUnitsRequired(),
                        d.getUnitsFulfilled()
                    )
                ).toList();

            return new UiContractEvent(
                new UiContractEventData(
                    contract.getId(),
                    contract.getFactionSymbol(),
                    contract.getType(),
                    contract.getTerms().getDeadline().toInstant(),
                    contract.getTerms().getPayment().getOnAccepted(),
                    contract.getTerms().getPayment().getOnFulfilled(),
                    delivery,
                    contract.getAccepted(),
                    contract.getFulfilled(),
                    contract.getExpiration().toInstant(),
                    contract.getDeadlineToAccept().toInstant()
                )
            );
        }
    }

    record UiShipEvent(UiShipEventData data) implements UiEvent {

        record UiShipEventData(List<UiShipEventShip> ships) {}

        record UiShipEventShip(
            String symbol,
            ShipRole role,
            String waypointSymbol,
            ShipNavStatus status,
            Integer crew,
            String frame,
            String reactor,
            String engine,
            List<String> modules,
            List<String> mounts,
            Integer cargoCapacity,
            Integer cargoStored,
            List<String> inventory,
            Integer fuelCapacity,
            Integer fuelCurrent,
            Integer cooldownTotal,
            Integer cooldownRemaining,
            Instant cooldownExpiration
        ) {}

        public static UiShipEvent from(List<Ship> ships) {

            return new UiShipEvent(
                new UiShipEventData(
                    ships.stream().map(ship -> new UiShipEventShip(
                        ship.getSymbol(),
                        ship.getRegistration().getRole(),
                        ship.getNav().getWaypointSymbol(),
                        ship.getNav().getStatus(),
                        ship.getCrew().getCurrent(),
                        ship.getFrame().getName(),
                        ship.getReactor().getName(),
                        ship.getEngine().getName(),
                        ship.getModules().stream().map(ShipModule::getName).toList(),
                        ship.getMounts().stream().map(ShipMount::getName).toList(),
                        ship.getCargo().getCapacity(),
                        ship.getCargo().getUnits(),
                        ship.getCargo().getInventory().stream().map(ShipCargoItem::toString).toList(),
                        ship.getFuel().getCapacity(),
                        ship.getFuel().getCurrent(),
                        ship.getCooldown().getTotalSeconds(),
                        ship.getCooldown().getRemainingSeconds(),
                        ship.getCooldown().getExpiration().toInstant()
                    )).toList()
                )
            );
        }
    }
}
