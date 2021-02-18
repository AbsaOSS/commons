/*
 * Copyright 2020 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.commons.config

import org.apache.commons.configuration.SubsetConfigurationMethods._
import org.apache.commons.configuration.{Configuration, SubsetConfiguration}
import org.apache.commons.lang.StringUtils._

import java.util.NoSuchElementException
import scala.util.Try

/**
  * The object contains extension methods for the [[org.apache.commons.configuration.Configuration Configuration]] interface.
  */
object ConfigurationImplicits {

  /**
    * The class wraps the [[org.apache.commons.configuration.Configuration Configuration]] interface in order to provide extension methods.
    *
    * @param conf A configuration instance
    * @tparam T A specific type implementing the [[org.apache.commons.configuration.Configuration Configuration]] interface
    */
  implicit class ConfigurationRequiredWrapper[T <: Configuration](val conf: T) extends AnyVal {

    /**
      * Gets a value of string configuration property and checks whether property exists.
      *
      * @return A value of string configuration property if exists, otherwise throws an exception.
      */
    def getRequiredString: String => String = getRequired(conf.getString, isNotBlank)

    /**
      * Gets a value of string array configuration property and checks whether the array is not empty.
      *
      * @return A value of string array configuration property if not empty, otherwise throws an exception.
      */
    def getRequiredStringArray: String => Array[String] = getRequired(conf.getStringArray, (arr: Array[String]) => !arr.forall(isBlank))

    /**
      * Gets a value of boolean configuration property and checks whether property exists.
      *
      * @return A value of boolean configuration property if exists, otherwise throws an exception.
      */
    def getRequiredBoolean: String => Boolean = getRequired(conf.getBoolean(_, null), null.!=) //NOSONAR

    /**
      * Gets a value of big decimal configuration property and checks whether property exists.
      *
      * @return A value of big decimal configuration property if exists, otherwise throws an exception.
      */
    def getRequiredBigDecimal: String => BigDecimal = getRequired(conf.getBigDecimal(_, null), null.!=) //NOSONAR

    /**
      * Gets a value of byte configuration property and checks whether property exists.
      *
      * @return A value of byte configuration property if exists, otherwise throws an exception.
      */
    def getRequiredByte: String => Byte = getRequired(conf.getByte(_, null), null.!=) //NOSONAR

    /**
      * Gets a value of short configuration property and checks whether property exists.
      *
      * @return A value of short configuration property if exists, otherwise throws an exception.
      */
    def getRequiredShort: String => Short = getRequired(conf.getShort(_, null), null.!=) //NOSONAR

    /**
      * Gets a value of int configuration property and checks whether property exists.
      *
      * @return A value of int configuration property if exists, otherwise throws an exception.
      */
    def getRequiredInt: String => Int = getRequired(conf.getInteger(_, null), null.!=) //NOSONAR

    /**
      * Gets a value of long configuration property and checks whether property exists.
      *
      * @return A value of long configuration property if exists, otherwise throws an exception.
      */
    def getRequiredLong: String => Long = getRequired(conf.getLong(_, null), null.!=) //NOSONAR

    /**
      * Gets a value of float configuration property and checks whether property exists.
      *
      * @return A value of float configuration property if exists, otherwise throws an exception.
      */
    def getRequiredFloat: String => Float = getRequired(conf.getFloat(_, null), null.!=) //NOSONAR

    /**
      * Gets a value of double configuration property and checks whether property exists.
      *
      * @return A value of double configuration property if exists, otherwise throws an exception.
      */
    def getRequiredDouble: String => Double = getRequired(conf.getDouble(_, null), null.!=) //NOSONAR

    private def getRequired[V](get: String => V, check: V => Boolean)(key: String): V = {
      Try(get(key))
        .filter(check)
        .recover {
          case _: NoSuchElementException =>
            // rewrite exception message for clarity
            throw new NoSuchElementException(s"Missing configuration property ${getFullPropName(key)}")
          case e: Exception =>
            throw new RuntimeException(s"Error in retrieving configuration property ${getFullPropName(key)}", e)
        }
        .get
    }

    private def getFullPropName(key: String) = conf match {
      case sc: SubsetConfiguration => sc.getParentKey(key)
      case _ => key
    }
  }

  /**
    * The class wraps the [[org.apache.commons.configuration.Configuration Configuration]] interface in order to provide extension methods.
    *
    * @param conf A configuration instance
    * @tparam T A specific type implementing the [[org.apache.commons.configuration.Configuration Configuration]] interface
    */
  implicit class ConfigurationOptionalWrapper[T <: Configuration](val conf: T) extends AnyVal {

    /**
      * Gets a value of string configuration property.
      *
      * @return A Some wrapped value of string configuration property if exists, otherwise None.
      */
    def getOptionalString: String => Option[String] = getOptional(conf.getString)(_).filter(isNotBlank)


    /**
      * Gets a value of string array configuration property and checks whether the array is not empty.
      *
      * @return A Some wrapped value of string array configuration property if not empty, otherwise None.
      */
    def getOptionalStringArray: String => Option[Array[String]] = getOptional(conf.getStringArray)(_).filter(_.nonEmpty)

    /**
      * Gets a value of boolean configuration property.
      *
      * @return A Some wrapped value of boolean configuration property if exists, otherwise None.
      */
    def getOptionalBoolean: String => Option[Boolean] = getOptional(conf.getBoolean)

    /**
      * Gets a value of big decimal configuration property.
      *
      * @return A Some wrapped value of big decimal configuration property if exists, otherwise None.
      */
    def getOptionalBigDecimal: String => Option[BigDecimal] = getOptional(conf.getBigDecimal(_))

    /**
      * Gets a value of byte configuration property.
      *
      * @return A Some wrapped value of byte configuration property if exists, otherwise None.
      */
    def getOptionalByte: String => Option[Byte] = getOptional(conf.getByte)

    /**
      * Gets a value of short configuration property.
      *
      * @return A Some wrapped value of short configuration property if exists, otherwise None.
      */
    def getOptionalShort: String => Option[Short] = getOptional(conf.getShort)

    /**
      * Gets a value of int configuration property.
      *
      * @return A Some wrapped value of int configuration property if exists, otherwise None.
      */
    def getOptionalInt: String => Option[Int] = getOptional(conf.getInt)

    /**
      * Gets a value of long configuration property.
      *
      * @return A Some wrapped value of long configuration property if exists, otherwise None.
      */
    def getOptionalLong: String => Option[Long] = getOptional(conf.getLong)

    /**
      * Gets a value of float configuration property.
      *
      * @return A Some wrapped value of float configuration property if exists, otherwise None.
      */
    def getOptionalFloat: String => Option[Float] = getOptional(conf.getFloat)

    /**
      * Gets a value of double configuration property.
      *
      * @return A Some wrapped value of double configuration property if exists, otherwise None.
      */
    def getOptionalDouble: String => Option[Double] = getOptional(conf.getDouble)

    private def getOptional[V](get: String => V)(key: String): Option[V] = {
      if (conf.containsKey(key))
        Option(get(key))
      else
        None
    }

  }

}
