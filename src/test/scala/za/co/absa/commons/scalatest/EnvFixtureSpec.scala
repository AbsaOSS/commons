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

class EnvFixtureSpec extends AnyFlatSpec with Matchers with EnvFixture {

  it should "set environment variable" in {
    System.getenv("FOO") should be(null) // check the testing env doesn't exist
    System.getenv("PATH") should not be empty // check the reference env exists

    setEnv("FOO", "42") // set testing env

    // check testing env is visible through the standard Java API
    System.getenv("FOO") should equal("42")
    // ... as well as the reference env
    System.getenv("PATH") should not be empty
  }

  it should "clean testing environment variables after the test" in {
    System.getenv("FOO") should be(null) // check the testing env no longer exists
    System.getenv("PATH") should not be empty // check the reference env still exists
  }
}
