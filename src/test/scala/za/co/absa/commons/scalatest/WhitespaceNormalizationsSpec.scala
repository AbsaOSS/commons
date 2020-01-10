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

class WhitespaceNormalizationsSpec
  extends AnyFlatSpec
    with Matchers
    with WhitespaceNormalizations {

  it should "normalize whitespaces" in {
    "  foo   bar \n  42  \t\t  " should equal(" foo bar 42 ")(after being whiteSpaceNormalised)
  }

  it should "remove whitespaces" in {
    "  foo   bar \n  42  \t\t  " should equal("foobar42")(after being whiteSpaceRemoved)
  }
}
