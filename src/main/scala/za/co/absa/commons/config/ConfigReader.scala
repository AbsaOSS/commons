package za.co.absa.commons.config

import com.typesafe.config.{Config, ConfigFactory}

object ConfigReader {
  private val config: Config = ConfigFactory.load()

  def readStringConfigIfExist(path: String): Option[String] = {
    if (config.hasPath(path)) {
      Option(config.getString(path))
    } else {
      None
    }
  }

  def readStringConfig(path: String, default: String): String = {
    readStringConfigIfExist(path).getOrElse(default)
  }
}
