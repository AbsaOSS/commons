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

import org.scalatest.Ignore
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ConditionalTestTagsSpec extends AnyFlatSpec with Matchers {

  behavior of "ignoreIf"

  it should "return @Ignore tag" in {
    ConditionalTestTags.ignoreIf(condition = true).name shouldBe classOf[Ignore].getName
  }

  it should "return empty tag" in {
    ConditionalTestTags.ignoreIf(condition = false).name shouldBe empty
  }
}
