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

package za.co.absa.commons.scalatest

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfter, Entry}
import za.co.absa.commons.scalatest.EnvFixtureSpec.{ExistingEnvVarName, ExistingEnvVarValue}

import scala.collection.JavaConverters._

object EnvFixtureSpec {
  // defined in scalatest plugin in pom.xml
  private val ExistingEnvVarName = "TEST_ENV_VAR_EnvFixtureSpec"
  private val ExistingEnvVarValue = "some_initial_value"
}

class EnvFixtureSpec extends AnyFlatSpec with Matchers with EnvFixture with BeforeAndAfter {

  before {
    // warm up caches
    System.getenv.entrySet()
    System.getenv.values()
    System.getenv.keySet()
  }

  it should "set environment variable" in {
    System.getenv("FOO") should be(null) // check the testing env doesn't exist
    System.getenv(ExistingEnvVarName) should equal(ExistingEnvVarValue) // check the reference env exists

    setEnv("FOO", "42") // set testing env

    // check testing env is visible through the standard Java API
    System.getenv("FOO") should equal("42")
    // ... as well as the reference env
    System.getenv(ExistingEnvVarName) should equal(ExistingEnvVarValue)

    // System.getenv(String) should be consistent with System.getenv.xxx()
    Seq("FOO", ExistingEnvVarName).map { k =>
      // on Windows, `System.getenv("pAth")` works fine, whereas `System.getenv.get(path)` requires correct case.
      System.getenv.get(k) should equal(System.getenv(k))
      System.getenv.keySet should contain(k)
      System.getenv.values should contain(System.getenv(k))
      System.getenv.entrySet should contain(Entry(k, System.getenv(k)))
      System.getenv.values should have size System.getenv.keySet.size.toLong
      System.getenv.entrySet should have size System.getenv.keySet.size.toLong
    }
  }

  it should "clean testing environment variables after the test" in {
    System.getenv("FOO") should be(null) // check the testing env no longer exists
    System.getenv(ExistingEnvVarName) should not be empty // check the reference env still exists

    // check consistency
    System.getenv.get("FOO") should be(null)
    System.getenv.keySet should not contain "FOO"
    System.getenv.entrySet.asScala.map(_.getKey) should not contain "FOO"
    System.getenv.values should have size System.getenv.keySet.size.toLong
    System.getenv.entrySet should have size System.getenv.keySet.size.toLong

    System.getenv(ExistingEnvVarName) should equal(System.getenv.get(ExistingEnvVarName))
    System.getenv.keySet should contain(ExistingEnvVarName)
    System.getenv.values should contain(System.getenv(ExistingEnvVarName))
    System.getenv.entrySet should contain(Entry(ExistingEnvVarName, System.getenv(ExistingEnvVarName)))
    System.getenv.values should have size System.getenv.keySet.size.toLong
    System.getenv.entrySet should have size System.getenv.keySet.size.toLong
  }
}
