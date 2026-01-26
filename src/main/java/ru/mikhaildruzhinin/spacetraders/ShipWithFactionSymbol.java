package ru.mikhaildruzhinin.spacetraders;

import ru.mikhaildruzhinin.spacetraders.generated.client.model.FactionSymbol;
import ru.mikhaildruzhinin.spacetraders.generated.client.model.Ship;

public record ShipWithFactionSymbol(Ship ship, FactionSymbol factionSymbol) {

    public static ShipWithFactionSymbol from(Ship ship, FactionSymbol factionSymbol) {
        return new ShipWithFactionSymbol(ship, factionSymbol);
    }
}
