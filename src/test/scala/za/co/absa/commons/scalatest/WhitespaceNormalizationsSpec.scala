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
import za.co.absa.commons.scalatest.WhitespaceNormalizationsSpec._

class WhitespaceNormalizationsSpec
  extends AnyFlatSpec
    with Matchers
    with WhitespaceNormalizations {

  it should "normalize whitespaces" in {
    "  foo   \rbar \n  42  \t\t  " should equal(" foo bar 42 ")(after being whiteSpaceNormalised)
  }

  it should "remove whitespaces" in {
    "  foo   \rbar \n  42  \t\t  " should equal("foobar42")(after being whiteSpaceRemoved)
  }

  it should "remove whitespaces in lines" in {
    "  foo  \n \rbar \n \n  \t\t   \n \n 42  \t\t  " should (
      (equal(
        """foo
          |bar
          |42""".stripMargin)
        and (not equal "foobar42"))
        (after being lineWhiteSpaceRemoved)
      )
  }

  it should "support unicode separators" in {
    val allSeparators = UnicodeTabulationSymbols ++ UnicodeTabulationSymbols
    whiteSpaceRemoved.normalized(allSeparators.mkString) should equal("")
    whiteSpaceNormalised.normalized(allSeparators.mkString) should equal(" ")
  }
}

object WhitespaceNormalizationsSpec {
  /**
    * List of Unicode Characters of Category “Space Separator”
    *
    * @see [[https://www.compart.com/en/unicode/category/Zs]]
    */
  val UnicodeSpaceSeparators: Seq[Char] = Seq(
    '\u0020', '\u00A0', '\u1680', '\u2000',
    '\u2001', '\u2002', '\u2003', '\u2004',
    '\u2005', '\u2006', '\u2007', '\u2008',
    '\u2009', '\u200A', '\u202F', '\u205F',
    '\u3000'
  )

  val UnicodeTabulationSymbols: Seq[Char] = Seq(
    '\u0009', '\u000B'
  )

}
