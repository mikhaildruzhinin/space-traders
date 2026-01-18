package ru.mikhaildruzhinin.spacetraders;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record WaypointSymbol(String sector, String system, String waypoint) {

    public static WaypointSymbol from(String waypointSymbol) {

        String regex = "^([^-]+)-([^-]+)-([^-]+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(waypointSymbol);
        if (!matcher.matches()) {
            // TODO: custom exception
            throw new RuntimeException(String.format("Invalid waypoint: %s", waypointSymbol));
        }

        String sector = matcher.group(1);
        String system = String.format("%s-%s", sector, matcher.group(2));
        String waypoint = String.format("%s-%s", system, matcher.group(3));

        return new WaypointSymbol(sector, system, waypoint);
    }
}
