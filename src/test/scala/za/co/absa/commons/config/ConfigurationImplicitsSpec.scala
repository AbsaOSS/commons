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

  behavior of "ConfigurationImplicits"

  it should "implement getRequiredString()" in {
    val configuration = new MapConfiguration(Map("foo" -> "bar").asJava)
    configuration getRequiredString "foo" should be("bar")
    intercept[IllegalArgumentException](configuration getRequiredString "oops").getMessage should include("oops")
  }

  it should "implement getRequiredStringArray()" in {
    val configuration = new MapConfiguration(Map("foo" -> "bar").asJava)
    configuration getRequiredStringArray "foo" should be(Array("bar"))
    intercept[IllegalArgumentException](configuration getRequiredStringArray "oops").getMessage should include("oops")
  }

  it should "implement getRequiredBoolean()" in {
    val configuration = new MapConfiguration(Map("foo" -> "true").asJava)
    configuration getRequiredBoolean "foo" should be(true)
    intercept[IllegalArgumentException](configuration getRequiredBoolean "oops").getMessage should include("oops")
  }

  it should "implement getRequiredBigDecimal()" in {
    val configuration = new MapConfiguration(Map("foo" -> "4.2").asJava)
    configuration getRequiredBigDecimal "foo" should be(BigDecimal(4.2))
    intercept[IllegalArgumentException](configuration getRequiredBigDecimal "oops").getMessage should include("oops")
  }

  it should "implement getRequiredByte()" in {
    val configuration = new MapConfiguration(Map("foo" -> "42").asJava)
    configuration getRequiredByte "foo" should be(42.byteValue)
    intercept[IllegalArgumentException](configuration getRequiredByte "oops").getMessage should include("oops")
  }

  it should "implement getRequiredShort()" in {
    val configuration = new MapConfiguration(Map("foo" -> "42").asJava)
    configuration getRequiredShort "foo" should be(42.shortValue)
    intercept[IllegalArgumentException](configuration getRequiredShort "oops").getMessage should include("oops")
  }

  it should "implement getRequiredInt()" in {
    val configuration = new MapConfiguration(Map("foo" -> "42").asJava)
    configuration getRequiredInt "foo" should be(42.intValue)
    intercept[IllegalArgumentException](configuration getRequiredInt "oops").getMessage should include("oops")
  }

  it should "implement getRequiredLong()" in {
    val configuration = new MapConfiguration(Map("foo" -> "42").asJava)
    configuration getRequiredLong "foo" should be(42.longValue)
    intercept[IllegalArgumentException](configuration getRequiredLong "oops").getMessage should include("oops")
  }

  it should "implement getRequiredFloat()" in {
    val configuration = new MapConfiguration(Map("foo" -> "4.2").asJava)
    configuration getRequiredFloat "foo" should be(4.2.floatValue)
    intercept[IllegalArgumentException](configuration getRequiredFloat "oops").getMessage should include("oops")
  }

  it should "implement getRequiredDouble()" in {
    val configuration = new MapConfiguration(Map("foo" -> "4.2").asJava)
    configuration getRequiredDouble "foo" should be(4.2.doubleValue)
    intercept[IllegalArgumentException](configuration getRequiredDouble "oops").getMessage should include("oops")
  }

  it should "behave the same way regardless of `throwExceptionOnMissing` property settings" in {
    Seq(false, true) foreach { b =>
      val conf = new BaseConfiguration {
        setThrowExceptionOnMissing(b)
      }
      intercept[IllegalArgumentException](conf getRequiredString "oops").getMessage should include("oops")
    }
  }
}
