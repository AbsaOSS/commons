package za.co.absa.commons.time

import java.util.TimeZone

import org.apache.log4j.{LogManager, Logger}
import org.apache.spark.sql.SparkSession
import za.co.absa.commons.config.ConfigReader

/**
 * Sets the system time zone per application configuration, recommended value being UTC
 */
object TimeZoneNormalizer {
  private val log: Logger = LogManager.getLogger(this.getClass)
  private val timeZone: String = ConfigReader.readStringConfigIfExist("timezone").getOrElse {
    val default = "UTC"
    log.warn(s"No time zone (timezone) setting found. Setting to default, which is $default.")
    default
  }

  def normalizeJVMTimeZone(): Unit = {
    TimeZone.setDefault(TimeZone.getTimeZone(timeZone))
    log.debug(s"JVM time zone set to $timeZone")
  }

  def normalizeSessionTimeZone(spark: SparkSession): Unit = {
    spark.conf.set("spark.sql.session.timeZone", timeZone)
    log.debug(s"Spark session ${spark.sparkContext.applicationId} time zone of name ${spark.sparkContext.appName} set to $timeZone")
  }

  def normalizeAll(spark: SparkSession): Unit = {
    normalizeJVMTimeZone()
    normalizeSessionTimeZone(spark)
  }

}
