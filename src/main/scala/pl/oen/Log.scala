package pl.oen

import org.apache.log4j.Logger

trait Log {
  val LOGGER = Logger.getLogger(getClass)
}
