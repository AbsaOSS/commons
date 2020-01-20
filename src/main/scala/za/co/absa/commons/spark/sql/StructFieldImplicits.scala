package za.co.absa.commons.spark.sql

import org.apache.spark.sql.types._

import scala.util.Try

object StructFieldImplicits {
  implicit class StructFieldEnhancements(val structField: StructField) {
    def getMetadataString(key: String): Option[String] = {
      Try(structField.metadata.getString(key)).toOption
    }

    def getMetadataChar(key: String): Option[Char] = {
      val resultString = Try(structField.metadata.getString(key)).toOption
      resultString.flatMap { s =>
        if (s.length == 1) {
          Option(s(0))
        } else {
          None
        }
      }
    }

    def getMetadataStringAsBoolean(key: String): Option[Boolean] = {
      Try(structField.metadata.getString(key).toBoolean).toOption
    }


    def hasMetadataKey(key: String): Boolean = {
      structField.metadata.contains(key)
    }
  }
}
