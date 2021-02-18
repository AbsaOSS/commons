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

import org.apache.commons.configuration.{BaseConfiguration, MapConfiguration}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ConfigurationImplicitsSpec extends AnyFlatSpec with Matchers {

  import ConfigurationImplicits._

  import scala.collection.JavaConverters._

  behavior of "ConfigurationRequiredWrapper"

  it should "implement getRequiredString()" in {
    val configuration = new MapConfiguration(Map("foo" -> "bar", "bla" -> "").asJava)
    configuration getRequiredString "foo" should be("bar")
    intercept[NoSuchElementException](configuration getRequiredString "bla").getMessage should include("bla")
    intercept[NoSuchElementException](configuration getRequiredString "oops").getMessage should include("oops")
  }

  it should "implement getRequiredStringArray()" in {
    val configuration = new MapConfiguration(Map("foo" -> "bar", "bla" -> "").asJava)
    configuration getRequiredStringArray "foo" should be(Array("bar"))
    intercept[NoSuchElementException](configuration getRequiredStringArray "bla").getMessage should include("bla")
    intercept[NoSuchElementException](configuration getRequiredStringArray "oops").getMessage should include("oops")
  }

  it should "implement getRequiredBoolean()" in {
    val configuration = new MapConfiguration(Map("foo" -> "true").asJava)
    configuration getRequiredBoolean "foo" should be(true)
    intercept[NoSuchElementException](configuration getRequiredBoolean "oops").getMessage should include("oops")
  }

  it should "implement getRequiredBigDecimal()" in {
    val configuration = new MapConfiguration(Map("foo" -> "4.2").asJava)
    configuration getRequiredBigDecimal "foo" should be(BigDecimal(4.2))
    intercept[NoSuchElementException](configuration getRequiredBigDecimal "oops").getMessage should include("oops")
  }

  it should "implement getRequiredByte()" in {
    val configuration = new MapConfiguration(Map("foo" -> "42").asJava)
    configuration getRequiredByte "foo" should be(42.byteValue)
    intercept[NoSuchElementException](configuration getRequiredByte "oops").getMessage should include("oops")
  }

  it should "implement getRequiredShort()" in {
    val configuration = new MapConfiguration(Map("foo" -> "42").asJava)
    configuration getRequiredShort "foo" should be(42.shortValue)
    intercept[NoSuchElementException](configuration getRequiredShort "oops").getMessage should include("oops")
  }

  it should "implement getRequiredInt()" in {
    val configuration = new MapConfiguration(Map("foo" -> "42").asJava)
    configuration getRequiredInt "foo" should be(42.intValue)
    intercept[NoSuchElementException](configuration getRequiredInt "oops").getMessage should include("oops")
  }

  it should "implement getRequiredLong()" in {
    val configuration = new MapConfiguration(Map("foo" -> "42").asJava)
    configuration getRequiredLong "foo" should be(42.longValue)
    intercept[NoSuchElementException](configuration getRequiredLong "oops").getMessage should include("oops")
  }

  it should "implement getRequiredFloat()" in {
    val configuration = new MapConfiguration(Map("foo" -> "4.2").asJava)
    configuration getRequiredFloat "foo" should be(4.2.floatValue)
    intercept[NoSuchElementException](configuration getRequiredFloat "oops").getMessage should include("oops")
  }

  it should "implement getRequiredDouble()" in {
    val configuration = new MapConfiguration(Map("foo" -> "4.2").asJava)
    configuration getRequiredDouble "foo" should be(4.2.doubleValue)
    intercept[NoSuchElementException](configuration getRequiredDouble "oops").getMessage should include("oops")
  }

  it should "behave the same way regardless of `throwExceptionOnMissing` property settings" in {
    Seq(false, true) foreach { b =>
      val conf = new BaseConfiguration {
        setThrowExceptionOnMissing(b)
      }
      intercept[NoSuchElementException](conf getRequiredString "oops").getMessage should include("oops")
    }
  }

  it should "report the full missing property name in case of SubsetConfiguration" in {
    val fooBarConf = new BaseConfiguration() subset "foo" subset "bar"
    intercept[NoSuchElementException](fooBarConf getRequiredString "baz").getMessage should include("foo.bar.baz")
  }

  it should "propagate exceptions other than missing property" in {
    object TestException extends Exception

    val conf = new BaseConfiguration {
      override def getProperty(key: String): AnyRef = throw TestException
    }

    the[RuntimeException] thrownBy conf.getRequiredInt("foo") should have(
      'message("Error in retrieving configuration property foo"),
      'cause(TestException))

    the[RuntimeException] thrownBy conf.getRequiredBoolean("foo") should have(
      'message("Error in retrieving configuration property foo"),
      'cause(TestException))

    the[RuntimeException] thrownBy conf.getRequiredString("foo") should have(
      'message("Error in retrieving configuration property foo"),
      'cause(TestException))

    the[RuntimeException] thrownBy conf.getRequiredStringArray("foo") should have(
      'message("Error in retrieving configuration property foo"),
      'cause(TestException))
  }

  behavior of "ConfigurationOptionalWrapper"

  it should "implement getOptionalString()" in {
    val configuration = new MapConfiguration(Map("foo" -> "bar", "bla" -> "").asJava)
    configuration getOptionalString "foo" should be(Some("bar"))
    configuration getOptionalString "bla" should be(None)
    configuration getOptionalString "oops" should be(None)
  }

  it should "implement getOptionalStringArray()" in {
    val configuration = new MapConfiguration(Map("foo" -> "bar", "bla" -> "").asJava)
    configuration.getOptionalStringArray("foo").get should be(Array("bar"))
    configuration getOptionalString "bla" should be(None)
    configuration getOptionalStringArray "oops" should be(None)
  }

  it should "implement getOptionalBoolean()" in {
    val configuration = new MapConfiguration(Map("foo" -> "true").asJava)
    configuration getOptionalBoolean "foo" should be(Some(true))
    configuration getOptionalBoolean "oops" should be(None)
  }

  it should "implement getOptionalBigDecimal()" in {
    val configuration = new MapConfiguration(Map("foo" -> "4.2").asJava)
    configuration getOptionalBigDecimal "foo" should be(Some(BigDecimal(4.2)))
    configuration getOptionalBigDecimal "oops" should be(None)
  }

  it should "implement getOptionalByte()" in {
    val configuration = new MapConfiguration(Map("foo" -> "42").asJava)
    configuration getOptionalByte "foo" should be(Some(42.byteValue))
    configuration getOptionalByte "oops" should be(None)
  }

  it should "implement getOptionalShort()" in {
    val configuration = new MapConfiguration(Map("foo" -> "42").asJava)
    configuration getOptionalShort "foo" should be(Some(42.shortValue))
    configuration getOptionalShort "oops" should be(None)
  }

  it should "implement getOptionalInt()" in {
    val configuration = new MapConfiguration(Map("foo" -> "42").asJava)
    configuration getOptionalInt "foo" should be(Some(42.intValue))
    configuration getOptionalInt "oops" should be(None)
  }

  it should "implement getOptionalLong()" in {
    val configuration = new MapConfiguration(Map("foo" -> "42").asJava)
    configuration getOptionalLong "foo" should be(Some(42.longValue))
    configuration getOptionalLong "oops" should be(None)
  }

  it should "implement getOptionalFloat()" in {
    val configuration = new MapConfiguration(Map("foo" -> "4.2").asJava)
    configuration getOptionalFloat "foo" should be(Some(4.2.floatValue))
    configuration getOptionalFloat "oops" should be(None)
  }

  it should "implement getOptionalDouble()" in {
    val configuration = new MapConfiguration(Map("foo" -> "4.2").asJava)
    configuration getOptionalDouble "foo" should be(Some(4.2.doubleValue))
    configuration getOptionalDouble "oops" should be(None)
  }

  it should "behave the same way regardless of `throwExceptionOnMissing` property settings" in {
    Seq(false, true) foreach { b =>
      val conf = new BaseConfiguration {
        setThrowExceptionOnMissing(b)
      }
      conf getOptionalString "oops" should be(None)
    }
  }
}
