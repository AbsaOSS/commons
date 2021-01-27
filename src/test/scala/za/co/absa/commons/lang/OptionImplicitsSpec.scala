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

package za.co.absa.commons.lang

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class OptionImplicitsSpec extends AnyFlatSpec with Matchers {

  import OptionImplicits._

  behavior of "StringWrapper"

  "nonBlankOption" should "return None for blank string" in {
    (null: String).nonBlankOption should be(None)
    "            ".nonBlankOption should be(None)
    " foo bar 42 ".nonBlankOption should be(Some(" foo bar 42 "))
  }

  behavior of "TraversableWrapper"

  "asOption" should "return None for empty collections" in {
    Seq.empty.asOption should be(None)
    Seq(1, 2).asOption should be(Some(Seq(1, 2)))
  }

  behavior of "AnyWrapper"

  "asOption" should "wrap it with an Option" in {
    (null: Any).asOption should be(None)
    "some text".asOption should be(Some("some text"))
  }

  "optionally()" should "call the supplied function with unwrapped value and return the result" in {
    42.optionally((_: Int) + (_: Int), Some(5)) should equal(47)
  }

  "optionally()" should "not call the supplied function and return original value" in {
    42.optionally((_: Int) + (_: Int), None) should equal(42)
  }

  "having()" should "behave the same as `optionally()`" in {
    42.having[Int](None)(_ + _) should equal(42)
    42.having(Option(5))(_ + _) should equal(47)
  }
}
