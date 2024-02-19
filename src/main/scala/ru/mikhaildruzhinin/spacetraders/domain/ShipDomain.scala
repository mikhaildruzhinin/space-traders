package ru.mikhaildruzhinin.spacetraders.domain

import enumeratum._
import ru.mikhaildruzhinin.spacetraders.domain.FactionDomain.FactionSymbol
import ru.mikhaildruzhinin.spacetraders.domain.WaypointDomain.WaypointType

import java.time.Instant

object ShipDomain {

  /**
   * Ship details.
   *
   * @param symbol           The globally unique identifier of the ship in the following format: [AGENT_SYMBOL]-[HEX_ID]
   * @param shipRegistration The public registration information of the ship
   * @param nav              The navigation information of the ship.
   * @param crew             The ship's crew service and maintain the ship's systems and equipment.
   * @param frame            The frame of the ship.
   *                         The frame determines the number of modules and mounting points of the ship,
   *                         as well as base fuel capacity.
   *                         As the condition of the frame takes more wear,
   *                         the ship will become more sluggish and less maneuverable.
   * @param reactor          The reactor of the ship. The reactor is responsible for powering the ship's systems and weapons.
   * @param engine           The engine determines how quickly a ship travels between waypoints.
   * @param cooldown         A cooldown is a period of time in which a ship cannot perform certain actions.
   * @param modules          Modules installed in this ship.
   * @param mounts           Mounts installed in this ship.
   * @param cargo            Ship cargo details.
   * @param fuel             Details of the ship's fuel tanks including how much fuel was consumed during the last transit or action.
   */
  case class Ship(symbol: String,
                  registration: ShipRegistration,
                  nav: ShipNav,
                  crew: ShipCrew,
                  frame: ShipFrame,
                  reactor: ShipReactor,
                  engine: ShipEngine,
                  cooldown: Cooldown,
                  modules: List[ShipModule],
                  mounts: List[ShipMount],
                  cargo: ShipCargo,
                  fuel: ShipFuel)

  /**
   * The public registration information of the ship
   *
   * @param name          The agent's registered name of the ship
   * @param factionSymbol The symbol of the faction the ship is registered with
   * @param role          The registered role of the ship
   */
  case class ShipRegistration(name: String,
                              factionSymbol: FactionSymbol,
                              role: ShipRole)

  /**
   * The registered role of the ship
   */
  sealed trait ShipRole extends EnumEntry

  //noinspection ScalaUnusedSymbol
  case object ShipRole extends Enum[ShipRole] with CirceEnum[ShipRole] {
    case object FABRICATOR extends ShipRole

    case object HARVESTER extends ShipRole

    case object HAULER extends ShipRole

    case object INTERCEPTOR extends ShipRole

    case object EXCAVATOR extends ShipRole

    case object TRANSPORT extends ShipRole

    case object REPAIR extends ShipRole

    case object SURVEYOR extends ShipRole

    case object COMMAND extends ShipRole

    case object CARRIER extends ShipRole

    case object PATROL extends ShipRole

    case object SATELLITE extends ShipRole

    case object EXPLORER extends ShipRole

    case object REFINERY extends ShipRole

    val values = findValues
  }

  /**
   * The navigation information of the ship.
   *
   * @param systemSymbol   The symbol of the system.
   * @param waypointSymbol The symbol of the waypoint.
   * @param route          The routing information for the ship's most recent transit or current location.
   * @param status         The current status of the ship
   * @param flightMode     The ship's set speed when traveling between waypoints or systems.
   */
  case class ShipNav(systemSymbol: String,
                     waypointSymbol: String,
                     route: ShipNavRoute,
                     status: String,
                     flightMode: ShipNavFlightMode = ShipNavFlightMode.CRUISE)

  /**
   * The routing information for the ship's most recent transit or current location.
   *
   * @param destination   The destination of a ships nav route.
   * @param origin        The departure of a ships nav route.
   * @param departureTime The date time of the ship's departure.
   * @param arrival       The date time of the ship's arrival. If the ship is in-transit, this is the expected time of arrival.
   */
  case class ShipNavRoute(destination: ShipNavRouteWaypoint,
                          origin: ShipNavRouteWaypoint,
                          departureTime: Instant,
                          arrival: Instant)

  /**
   * The destination or departure of a ships nav route.
   *
   * @param symbol The symbol of the waypoint.
   * @param `type` The type of waypoint.
   * @param systemSymbol The symbol of the system.
   * @param x Position in the universe in the x axis.
   * @param y Position in the universe in the y axis.
   */
  case class ShipNavRouteWaypoint(symbol: String,
                                  `type`: WaypointType,
                                  systemSymbol: String,
                                  x: Int,
                                  y: Int)

  /**
   * The ship's set speed when traveling between waypoints or systems.
   */
  sealed trait ShipNavFlightMode extends EnumEntry

  //noinspection ScalaUnusedSymbol
  case object ShipNavFlightMode extends Enum[ShipNavFlightMode] with CirceEnum[ShipNavFlightMode] {
    case object DRIFT extends ShipNavFlightMode

    case object STEALTH extends ShipNavFlightMode

    case object CRUISE extends ShipNavFlightMode

    case object BURN extends ShipNavFlightMode

    val values = findValues
  }

  /**
   * The ship's crew service and maintain the ship's systems and equipment.
   *
   * @param current The current number of crew members on the ship.
   * @param required The minimum number of crew members required to maintain the ship.
   * @param capacity The maximum number of crew members the ship can support.
   * @param rotation The rotation of crew shifts. A stricter shift improves the ship's performance.
   *                 A more relaxed shift improves the crew's morale.
   * @param morale A rough measure of the crew's morale.
   *               A higher morale means the crew is happier and more productive.
   *               A lower morale means the ship is more prone to accidents.
   * @param wages The amount of credits per crew member paid per hour. Wages are paid when a ship docks at a civilized waypoint.
   */
  case class ShipCrew(current: Int,
                     required: Int,
                      capacity: Int,
                      rotation: ShipCrewRotation = ShipCrewRotation.STRICT,
                      morale: Int,
                      wages: Int)

  /**
   * The rotation of crew shifts.
   * A stricter shift improves the ship's performance.
   * A more relaxed shift improves the crew's morale.
   */
  sealed trait ShipCrewRotation extends EnumEntry

  //noinspection ScalaUnusedSymbol
  case object ShipCrewRotation extends Enum[ShipCrewRotation] with CirceEnum[ShipCrewRotation] {
    case object STRICT extends ShipCrewRotation

    case object RELAXED extends ShipCrewRotation

    val values = findValues
  }

  /**
   * The frame of the ship. The frame determines the number of modules and mounting points of the ship, as well as
   * base fuel capacity. As the condition of the frame takes more wear, the ship will become more sluggish and less
   * maneuverable.
   *
   * @param symbol Symbol of the frame.
   * @param name Name of the frame.
   * @param description Description of the frame.
   * @param condition Condition is a range of 0 to 100 where 0 is completely worn out and 100 is brand new.
   * @param moduleSlots The amount of slots that can be dedicated to modules installed in the ship.
   *                    Each installed module take up a number of slots, and once there are no more slots,
   *                    no new modules can be installed.
   * @param mountingPoints The amount of slots that can be dedicated to mounts installed in the ship.
   *                       Each installed mount takes up a number of points, and once there are no more points
   *                       remaining, no new mounts can be installed.
   * @param fuelCapacity The maximum amount of fuel that can be stored in this ship. When refueling, the ship will be
   *                     refueled to this amount.
   * @param requirements The requirements for installation on a ship
   */
  case class ShipFrame(symbol: ShipFrameSymbol,
                       name: String,
                       description: String,
                       condition: Int,
                       moduleSlots: Int,
                       mountingPoints: Int,
                       fuelCapacity: Int,
                       requirements: ShipRequirements)

  /**
   * Symbol of the frame.
   */
  sealed trait ShipFrameSymbol extends EnumEntry

  //noinspection ScalaUnusedSymbol
  case object ShipFrameSymbol extends Enum[ShipFrameSymbol] with CirceEnum[ShipFrameSymbol] {
    case object FRAME_PROBE extends ShipFrameSymbol

    case object FRAME_DRONE extends ShipFrameSymbol

    case object FRAME_INTERCEPTOR extends ShipFrameSymbol

    case object FRAME_RACER extends ShipFrameSymbol

    case object FRAME_FIGHTER extends ShipFrameSymbol

    case object FRAME_FRIGATE extends ShipFrameSymbol

    case object FRAME_SHUTTLE extends ShipFrameSymbol

    case object FRAME_EXPLORER extends ShipFrameSymbol

    case object FRAME_MINER extends ShipFrameSymbol

    case object FRAME_LIGHT_FREIGHTER extends ShipFrameSymbol

    case object FRAME_HEAVY_FREIGHTER extends ShipFrameSymbol

    case object FRAME_TRANSPORT extends ShipFrameSymbol

    case object FRAME_DESTROYER extends ShipFrameSymbol

    case object FRAME_CRUISER extends ShipFrameSymbol

    case object FRAME_CARRIER extends ShipFrameSymbol

    val values = findValues
  }

  /**
   * The requirements for installation on a ship
   *
   * @param power The amount of power required from the reactor.
   * @param crew The number of crew required for operation.
   * @param slots The number of module slots required for installation.
   */
  case class ShipRequirements(power: Option[Int],
                              crew: Option[Int],
                              slots: Option[Int])

  /**
   * The reactor of the ship. The reactor is responsible for powering the ship's systems and weapons.
   *
   * @param symbol Symbol of the reactor.
   * @param name Name of the reactor.
   * @param description Description of the reactor.
   * @param condition Condition is a range of 0 to 100 where 0 is completely worn out and 100 is brand new.
   * @param powerOutput The amount of power provided by this reactor. The more power a reactor provides to the ship,
   *                    the lower the cooldown it gets when using a module or mount that taxes the ship's power.
   * @param requirements The requirements for installation on a ship
   */
  case class ShipReactor(symbol: ShipReactorSymbol,
                         name: String,
                         description: String,
                         condition: Int,
                         powerOutput: Int,
                         requirements: ShipRequirements)

  /**
   * Symbol of the reactor.
   */
  sealed trait ShipReactorSymbol extends EnumEntry

  //noinspection ScalaUnusedSymbol
  case object ShipReactorSymbol extends Enum[ShipReactorSymbol] with CirceEnum[ShipReactorSymbol] {
    case object REACTOR_SOLAR_I extends ShipReactorSymbol

    case object REACTOR_FUSION_I extends ShipReactorSymbol

    case object REACTOR_FISSION_I extends ShipReactorSymbol

    case object REACTOR_CHEMICAL_I extends ShipReactorSymbol

    case object REACTOR_ANTIMATTER_I extends ShipReactorSymbol

    val values = findValues
  }

  /**
   * The engine determines how quickly a ship travels between waypoints.
   *
   * @param symbol The symbol of the engine.
   * @param name The name of the engine.
   * @param description The description of the engine.
   * @param condition Condition is a range of 0 to 100 where 0 is completely worn out and 100 is brand new.
   * @param speed The speed stat of this engine. The higher the speed, the faster a ship can travel from one point
   *              to another. Reduces the time of arrival when navigating the ship.
   * @param requirements The requirements for installation on a ship
   */
  case class ShipEngine(symbol: ShipEngineSymbol,
                        name: String,
                        description: String,
                        condition: Int,
                        speed: Int,
                        requirements: ShipRequirements)

  /**
   * The symbol of the engine.
   */
  sealed trait ShipEngineSymbol extends EnumEntry

  //noinspection ScalaUnusedSymbol
  case object ShipEngineSymbol extends Enum[ShipEngineSymbol] with CirceEnum[ShipEngineSymbol] {
    case object ENGINE_IMPULSE_DRIVE_I extends ShipEngineSymbol

    case object ENGINE_ION_DRIVE_I extends ShipEngineSymbol

    case object ENGINE_ION_DRIVE_II extends ShipEngineSymbol

    case object ENGINE_HYPER_DRIVE_I extends ShipEngineSymbol

    val values = findValues
  }

  /**
   * A cooldown is a period of time in which a ship cannot perform certain actions.
   *
   * @param shipSymbol The symbol of the ship that is on cooldown
   * @param totalSeconds The total duration of the cooldown in seconds
   * @param remainingSeconds The remaining duration of the cooldown in seconds
   * @param expiration The date and time when the cooldown expires in ISO 8601 format
   */
  case class Cooldown(shipSymbol: String,
                      totalSeconds: Int,
                      remainingSeconds: Int,
                      expiration: Option[Instant])

  /**
   * A module can be installed in a ship and provides a set of capabilities such as storage space or quarters for crew.
   * Module installations are permanent.
   *
   * @param symbol The symbol of the module.
   * @param capacity Modules that provide capacity, such as cargo hold or crew quarters will show this value to denote
   *                 how much of a bonus the module grants.
   * @param range Modules that have a range will such as a sensor array show this value to denote how far
   *              can the module reach with its capabilities.
   * @param name Name of this module.
   * @param description Description of this module.
   * @param requirements The requirements for installation on a ship
   */
  case class ShipModule(symbol: ShipModuleSymbol,
                        capacity: Option[Int],
                        range: Option[Int],
                        name: String,
                        description: String,
                        requirements: ShipRequirements)

  /**
   * The symbol of the module.
   */
  sealed trait ShipModuleSymbol extends EnumEntry

  //noinspection ScalaUnusedSymbol
  case object ShipModuleSymbol extends Enum[ShipModuleSymbol] with CirceEnum[ShipModuleSymbol] {
    case object MODULE_MINERAL_PROCESSOR_I extends ShipModuleSymbol

    case object MODULE_GAS_PROCESSOR_I extends ShipModuleSymbol

    case object MODULE_CARGO_HOLD_I extends ShipModuleSymbol

    case object MODULE_CARGO_HOLD_II extends ShipModuleSymbol

    case object MODULE_CARGO_HOLD_III extends ShipModuleSymbol

    case object MODULE_CREW_QUARTERS_I extends ShipModuleSymbol

    case object MODULE_ENVOY_QUARTERS_I extends ShipModuleSymbol

    case object MODULE_PASSENGER_CABIN_I extends ShipModuleSymbol

    case object MODULE_MICRO_REFINERY_I extends ShipModuleSymbol

    case object MODULE_ORE_REFINERY_I extends ShipModuleSymbol

    case object MODULE_FUEL_REFINERY_I extends ShipModuleSymbol

    case object MODULE_SCIENCE_LAB_I extends ShipModuleSymbol

    case object MODULE_JUMP_DRIVE_I extends ShipModuleSymbol

    case object MODULE_JUMP_DRIVE_II extends ShipModuleSymbol

    case object MODULE_JUMP_DRIVE_III extends ShipModuleSymbol

    case object MODULE_WARP_DRIVE_I extends ShipModuleSymbol

    case object MODULE_WARP_DRIVE_II extends ShipModuleSymbol

    case object MODULE_WARP_DRIVE_III extends ShipModuleSymbol

    case object MODULE_SHIELD_GENERATOR_I extends ShipModuleSymbol

    case object MODULE_SHIELD_GENERATOR_II extends ShipModuleSymbol

    val values = findValues
  }

  /**
   * A mount is installed on the exterier of a ship.
   *
   * @param symbol Symbol of this mount.
   * @param name Name of this mount.
   * @param description Description of this mount.
   * @param strenght Mounts that have this value, such as mining lasers, denote how powerful this mount's capabilities are.
   * @param deposits Mounts that have this value denote what goods can be produced from using the mount.
   * @param requirements The requirements for installation on a ship
   */
  case class ShipMount(symbol: ShipMountSymbol,
                       name: String,
                       description: Option[String],
                       strength: Option[Int],
                       deposits: Option[List[ShipMountDeposit]],
                       requirements: ShipRequirements)

  /**
   * Symbol of this mount.
   */
  sealed trait ShipMountSymbol extends EnumEntry

  //noinspection ScalaUnusedSymbol
  case object ShipMountSymbol extends Enum[ShipMountSymbol] with CirceEnum[ShipMountSymbol] {
    case object MOUNT_GAS_SIPHON_I extends ShipMountSymbol

    case object MOUNT_GAS_SIPHON_II extends ShipMountSymbol

    case object MOUNT_GAS_SIPHON_III extends ShipMountSymbol

    case object MOUNT_SURVEYOR_I extends ShipMountSymbol

    case object MOUNT_SURVEYOR_II extends ShipMountSymbol

    case object MOUNT_SURVEYOR_III extends ShipMountSymbol

    case object MOUNT_SENSOR_ARRAY_I extends ShipMountSymbol

    case object MOUNT_SENSOR_ARRAY_II extends ShipMountSymbol

    case object MOUNT_SENSOR_ARRAY_III extends ShipMountSymbol

    case object MOUNT_MINING_LASER_I extends ShipMountSymbol

    case object MOUNT_MINING_LASER_II extends ShipMountSymbol

    case object MOUNT_MINING_LASER_III extends ShipMountSymbol

    case object MOUNT_LASER_CANNON_I extends ShipMountSymbol

    case object MOUNT_MISSILE_LAUNCHER_I extends ShipMountSymbol

    case object MOUNT_TURRET_I extends ShipMountSymbol

    val values = findValues
  }

  /**
   * Mounts that have this value denote what goods can be produced from using the mount.
   */
  sealed trait ShipMountDeposit extends EnumEntry

  //noinspection ScalaUnusedSymbol
  case object ShipMountDeposit extends Enum[ShipMountDeposit] with CirceEnum[ShipMountDeposit] {
    case object QUARTZ_SAND extends ShipMountDeposit

    case object SILICON_CRYSTALS extends ShipMountDeposit

    case object PRECIOUS_STONES extends ShipMountDeposit

    case object ICE_WATER extends ShipMountDeposit

    case object AMMONIA_ICE extends ShipMountDeposit

    case object IRON_ORE extends ShipMountDeposit

    case object COPPER_ORE extends ShipMountDeposit

    case object SILVER_ORE extends ShipMountDeposit

    case object ALUMINUM_ORE extends ShipMountDeposit

    case object GOLD_ORE extends ShipMountDeposit

    case object PLATINUM_ORE extends ShipMountDeposit

    case object DIAMONDS extends ShipMountDeposit

    case object URANITE_ORE extends ShipMountDeposit

    case object MERITIUM_ORE extends ShipMountDeposit

    val values = findValues
  }

  /**
   * Ship cargo details.
   *
   * @param capacity The max number of items that can be stored in the cargo hold.
   * @param units The number of items currently stored in the cargo hold.
   * @param inventory The items currently in the cargo hold.
   */
  case class ShipCargo(capacity: Int,
                       units: Int,
                       inventory: List[ShipCargoItem])

  /**
   * The type of cargo item and the number of units.
   *
   * @param symbol The good's symbol.
   * @param name The name of the cargo item type.
   * @param description The description of the cargo item type.
   * @param units The number of units of the cargo item.
   */
  case class ShipCargoItem(symbol: ShipCargoItemSymbol,
                           name: String,
                           description: String,
                           units: Int)

  /**
   * The good's symbol.
   */
  sealed trait ShipCargoItemSymbol extends EnumEntry

  //noinspection ScalaUnusedSymbol
  case object ShipCargoItemSymbol extends Enum[ShipCargoItemSymbol] with CirceEnum[ShipCargoItemSymbol] {
    case object PRECIOUS_STONES extends ShipCargoItemSymbol

    case object QUARTZ_SAND extends ShipCargoItemSymbol

    case object SILICON_CRYSTALS extends ShipCargoItemSymbol

    case object AMMONIA_ICE extends ShipCargoItemSymbol

    case object LIQUID_HYDROGEN extends ShipCargoItemSymbol

    case object LIQUID_NITROGEN extends ShipCargoItemSymbol

    case object ICE_WATER extends ShipCargoItemSymbol

    case object EXOTIC_MATTER extends ShipCargoItemSymbol

    case object ADVANCED_CIRCUITRY extends ShipCargoItemSymbol

    case object GRAVITON_EMITTERS extends ShipCargoItemSymbol

    case object IRON extends ShipCargoItemSymbol

    case object IRON_ORE extends ShipCargoItemSymbol

    case object COPPER extends ShipCargoItemSymbol

    case object COPPER_ORE extends ShipCargoItemSymbol

    case object ALUMINUM extends ShipCargoItemSymbol

    case object ALUMINUM_ORE extends ShipCargoItemSymbol

    case object SILVER extends ShipCargoItemSymbol

    case object SILVER_ORE extends ShipCargoItemSymbol

    case object GOLD extends ShipCargoItemSymbol

    case object GOLD_ORE extends ShipCargoItemSymbol

    case object PLATINUM extends ShipCargoItemSymbol

    case object PLATINUM_ORE extends ShipCargoItemSymbol

    case object DIAMONDS extends ShipCargoItemSymbol

    case object URANITE extends ShipCargoItemSymbol

    case object URANITE_ORE extends ShipCargoItemSymbol

    case object MERITIUM extends ShipCargoItemSymbol

    case object MERITIUM_ORE extends ShipCargoItemSymbol

    case object HYDROCARBON extends ShipCargoItemSymbol

    case object ANTIMATTER extends ShipCargoItemSymbol

    case object FAB_MATS extends ShipCargoItemSymbol

    case object FERTILIZERS extends ShipCargoItemSymbol

    case object FABRICS extends ShipCargoItemSymbol

    case object FOOD extends ShipCargoItemSymbol

    case object JEWELRY extends ShipCargoItemSymbol

    case object MACHINERY extends ShipCargoItemSymbol

    case object FIREARMS extends ShipCargoItemSymbol

    case object ASSAULT_RIFLES extends ShipCargoItemSymbol

    case object MILITARY_EQUIPMENT extends ShipCargoItemSymbol

    case object EXPLOSIVES extends ShipCargoItemSymbol

    case object LAB_INSTRUMENTS extends ShipCargoItemSymbol

    case object AMMUNITION extends ShipCargoItemSymbol

    case object ELECTRONICS extends ShipCargoItemSymbol

    case object SHIP_PLATING extends ShipCargoItemSymbol

    case object SHIP_PARTS extends ShipCargoItemSymbol

    case object EQUIPMENT extends ShipCargoItemSymbol

    case object FUEL extends ShipCargoItemSymbol

    case object MEDICINE extends ShipCargoItemSymbol

    case object DRUGS extends ShipCargoItemSymbol

    case object CLOTHING extends ShipCargoItemSymbol

    case object MICROPROCESSORS extends ShipCargoItemSymbol

    case object PLASTICS extends ShipCargoItemSymbol

    case object POLYNUCLEOTIDES extends ShipCargoItemSymbol

    case object BIOCOMPOSITES extends ShipCargoItemSymbol

    case object QUANTUM_STABILIZERS extends ShipCargoItemSymbol

    case object NANOBOTS extends ShipCargoItemSymbol

    case object AI_MAINFRAMES extends ShipCargoItemSymbol

    case object QUANTUM_DRIVES extends ShipCargoItemSymbol

    case object ROBOTIC_DRONES extends ShipCargoItemSymbol

    case object CYBER_IMPLANTS extends ShipCargoItemSymbol

    case object GENE_THERAPEUTICS extends ShipCargoItemSymbol

    case object NEURAL_CHIPS extends ShipCargoItemSymbol

    case object MOOD_REGULATORS extends ShipCargoItemSymbol

    case object VIRAL_AGENTS extends ShipCargoItemSymbol

    case object MICRO_FUSION_GENERATORS extends ShipCargoItemSymbol

    case object SUPERGRAINS extends ShipCargoItemSymbol

    case object LASER_RIFLES extends ShipCargoItemSymbol

    case object HOLOGRAPHICS extends ShipCargoItemSymbol

    case object SHIP_SALVAGE extends ShipCargoItemSymbol

    case object RELIC_TECH extends ShipCargoItemSymbol

    case object NOVEL_LIFEFORMS extends ShipCargoItemSymbol

    case object BOTANICAL_SPECIMENS extends ShipCargoItemSymbol

    case object CULTURAL_ARTIFACTS extends ShipCargoItemSymbol

    case object FRAME_PROBE extends ShipCargoItemSymbol

    case object FRAME_DRONE extends ShipCargoItemSymbol

    case object FRAME_INTERCEPTOR extends ShipCargoItemSymbol

    case object FRAME_RACER extends ShipCargoItemSymbol

    case object FRAME_FIGHTER extends ShipCargoItemSymbol

    case object FRAME_FRIGATE extends ShipCargoItemSymbol

    case object FRAME_SHUTTLE extends ShipCargoItemSymbol

    case object FRAME_EXPLORER extends ShipCargoItemSymbol

    case object FRAME_MINER extends ShipCargoItemSymbol

    case object FRAME_LIGHT_FREIGHTER extends ShipCargoItemSymbol

    case object FRAME_HEAVY_FREIGHTER extends ShipCargoItemSymbol

    case object FRAME_TRANSPORT extends ShipCargoItemSymbol

    case object FRAME_DESTROYER extends ShipCargoItemSymbol

    case object FRAME_CRUISER extends ShipCargoItemSymbol

    case object FRAME_CARRIER extends ShipCargoItemSymbol

    case object REACTOR_SOLAR_I extends ShipCargoItemSymbol

    case object REACTOR_FUSION_I extends ShipCargoItemSymbol

    case object REACTOR_FISSION_I extends ShipCargoItemSymbol

    case object REACTOR_CHEMICAL_I extends ShipCargoItemSymbol

    case object REACTOR_ANTIMATTER_I extends ShipCargoItemSymbol

    case object ENGINE_IMPULSE_DRIVE_I extends ShipCargoItemSymbol

    case object ENGINE_ION_DRIVE_I extends ShipCargoItemSymbol

    case object ENGINE_ION_DRIVE_II extends ShipCargoItemSymbol

    case object ENGINE_HYPER_DRIVE_I extends ShipCargoItemSymbol

    case object MODULE_MINERAL_PROCESSOR_I extends ShipCargoItemSymbol

    case object MODULE_GAS_PROCESSOR_I extends ShipCargoItemSymbol

    case object MODULE_CARGO_HOLD_I extends ShipCargoItemSymbol

    case object MODULE_CARGO_HOLD_II extends ShipCargoItemSymbol

    case object MODULE_CARGO_HOLD_III extends ShipCargoItemSymbol

    case object MODULE_CREW_QUARTERS_I extends ShipCargoItemSymbol

    case object MODULE_ENVOY_QUARTERS_I extends ShipCargoItemSymbol

    case object MODULE_PASSENGER_CABIN_I extends ShipCargoItemSymbol

    case object MODULE_MICRO_REFINERY_I extends ShipCargoItemSymbol

    case object MODULE_SCIENCE_LAB_I extends ShipCargoItemSymbol

    case object MODULE_JUMP_DRIVE_I extends ShipCargoItemSymbol

    case object MODULE_JUMP_DRIVE_II extends ShipCargoItemSymbol

    case object MODULE_JUMP_DRIVE_III extends ShipCargoItemSymbol

    case object MODULE_WARP_DRIVE_I extends ShipCargoItemSymbol

    case object MODULE_WARP_DRIVE_II extends ShipCargoItemSymbol

    case object MODULE_WARP_DRIVE_III extends ShipCargoItemSymbol

    case object MODULE_SHIELD_GENERATOR_I extends ShipCargoItemSymbol

    case object MODULE_SHIELD_GENERATOR_II extends ShipCargoItemSymbol

    case object MODULE_ORE_REFINERY_I extends ShipCargoItemSymbol

    case object MODULE_FUEL_REFINERY_I extends ShipCargoItemSymbol

    case object MOUNT_GAS_SIPHON_I extends ShipCargoItemSymbol

    case object MOUNT_GAS_SIPHON_II extends ShipCargoItemSymbol

    case object MOUNT_GAS_SIPHON_III extends ShipCargoItemSymbol

    case object MOUNT_SURVEYOR_I extends ShipCargoItemSymbol

    case object MOUNT_SURVEYOR_II extends ShipCargoItemSymbol

    case object MOUNT_SURVEYOR_III extends ShipCargoItemSymbol

    case object MOUNT_SENSOR_ARRAY_I extends ShipCargoItemSymbol

    case object MOUNT_SENSOR_ARRAY_II extends ShipCargoItemSymbol

    case object MOUNT_SENSOR_ARRAY_III extends ShipCargoItemSymbol

    case object MOUNT_MINING_LASER_I extends ShipCargoItemSymbol

    case object MOUNT_MINING_LASER_II extends ShipCargoItemSymbol

    case object MOUNT_MINING_LASER_III extends ShipCargoItemSymbol

    case object MOUNT_LASER_CANNON_I extends ShipCargoItemSymbol

    case object MOUNT_MISSILE_LAUNCHER_I extends ShipCargoItemSymbol

    case object MOUNT_TURRET_I extends ShipCargoItemSymbol

    case object SHIP_PROBE extends ShipCargoItemSymbol

    case object SHIP_MINING_DRONE extends ShipCargoItemSymbol

    case object SHIP_SIPHON_DRONE extends ShipCargoItemSymbol

    case object SHIP_INTERCEPTOR extends ShipCargoItemSymbol

    case object SHIP_LIGHT_HAULER extends ShipCargoItemSymbol

    case object SHIP_COMMAND_FRIGATE extends ShipCargoItemSymbol

    case object SHIP_EXPLORER extends ShipCargoItemSymbol

    case object SHIP_HEAVY_FREIGHTER extends ShipCargoItemSymbol

    case object SHIP_LIGHT_SHUTTLE extends ShipCargoItemSymbol

    case object SHIP_ORE_HOUND extends ShipCargoItemSymbol

    case object SHIP_REFINING_FREIGHTER extends ShipCargoItemSymbol

    case object SHIP_SURVEYOR extends ShipCargoItemSymbol

    val values = findValues
  }

  /**
   * Details of the ship's fuel tanks including how much fuel was consumed during the last transit or action.
   *
   * @param current The current amount of fuel in the ship's tanks.
   * @param capacity The maximum amount of fuel the ship's tanks can hold.
   * @param consumed An object that only shows up when an action has consumed fuel in the process. Shows the fuel consumption data.
   */
  case class ShipFuel(current: Int,
                      capacity: Int,
                      consumed: Option[ShipFuelConsumed])

  /**
   * An object that only shows up when an action has consumed fuel in the process. Shows the fuel consumption data.
   *
   * @param amount The amount of fuel consumed by the most recent transit or action.
   * @param timestamp The time at which the fuel was consumed.
   */
  case class ShipFuelConsumed(amount: Int,
                              timestamp: Instant)
}
