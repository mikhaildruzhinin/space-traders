package ru.mikhaildruzhinin.spacetraders;

import ru.mikhaildruzhinin.spacetraders.generated.client.model.Ship;

public record ShipSymbol(String symbol) {

    public static ShipSymbol from(Ship ship) {
        return new ShipSymbol(ship.getSymbol());
    }

    // For compatibility with ru.mikhaildruzhinin.spacetraders.generated.client.model.Ship
    public String getSymbol() {
        return symbol;
    }
}
