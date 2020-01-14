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

package za.co.absa.commons

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BuildInfoSpec extends AnyFlatSpec with Matchers {

  "Version" should "return build version" in {
    BuildInfo.Version should equal("4.2.42-TEST")
  }

  "Timestamp" should "return build timestamp" in {
    BuildInfo.Timestamp should equal("1234567890")
  }

  "BuildProps" should "return build info as Java properties" in {
    BuildInfo.BuildProps.getProperty("build.version") should equal("4.2.42-TEST")
    BuildInfo.BuildProps.getProperty("build.timestamp") should equal("1234567890")
  }

}
