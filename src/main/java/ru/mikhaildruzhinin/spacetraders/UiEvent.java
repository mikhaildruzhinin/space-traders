package ru.mikhaildruzhinin.spacetraders;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ru.mikhaildruzhinin.spacetraders.generated.client.model.Agent;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = UiEvent.UiAgentEvent.class, name = "agent"),
    @JsonSubTypes.Type(value = UiEvent.UiContractEvent.class, name = "contract"),
    @JsonSubTypes.Type(value = UiEvent.UiShipEvent.class, name = "ship")
})
public sealed interface UiEvent permits UiEvent.UiAgentEvent, UiEvent.UiContractEvent, UiEvent.UiShipEvent {

    record UiAgentEvent(UiAgentEventData data) implements UiEvent {

        record UiAgentEventData(
            String id,
            String symbol,
            String headquarters,
            Long credits,
            String faction,
            int shipsCount) {

            public static UiAgentEventData from(Agent agent) {
                return new UiAgentEventData(
                    agent.getAccountId(),
                    agent.getSymbol(),
                    agent.getHeadquarters(),
                    agent.getCredits(),
                    agent.getStartingFaction(),
                    agent.getShipCount()
                );
            }
        }

        public static UiAgentEvent from(Agent agent) {
            return new UiAgentEvent(UiAgentEventData.from(agent));
        }

    }

    record UiContractEvent(Data data) implements UiEvent {}

    record UiShipEvent() implements UiEvent {}

    record Data() {}

}
