package ru.mikhaildruzhinin.spacetraders.domain

import enumeratum._

object WaypointDomain {

  /**
   * The type of waypoint.
   */
  sealed trait WaypointType extends EnumEntry

  case object WaypointType extends Enum[WaypointType] with CirceEnum[WaypointType] {
    case object PLANET extends WaypointType

    case object GAS_GIANT extends WaypointType

    case object MOON extends WaypointType

    case object ORBITAL_STATION extends WaypointType

    case object JUMP_GATE extends WaypointType

    case object ASTEROID_FIELD extends WaypointType

    case object ASTEROID extends WaypointType

    case object ENGINEERED_ASTEROID extends WaypointType

    case object ASTEROID_BASE extends WaypointType

    case object NEBULA extends WaypointType

    case object DEBRIS_FIELD extends WaypointType

    case object GRAVITY_WELL extends WaypointType

    case object ARTIFICIAL_GRAVITY_WELL extends WaypointType

    case object FUEL_STATION extends WaypointType

    val values = findValues
  }
}
