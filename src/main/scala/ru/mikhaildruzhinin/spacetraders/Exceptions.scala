package ru.mikhaildruzhinin.spacetraders

object Exceptions {
  class WaypointSymbolParsingException(message: String) extends Exception(message)
}
