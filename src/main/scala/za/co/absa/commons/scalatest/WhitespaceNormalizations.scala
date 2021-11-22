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

import org.scalactic.{AbstractStringUniformity, Uniformity}
import za.co.absa.commons.scalatest.WhitespaceNormalizations.WhiteSpaceRegex

object WhitespaceNormalizations extends WhitespaceNormalizations {
  val WhiteSpaceRegex = "[\\s\\h]+"
}

trait WhitespaceNormalizations {
  val whiteSpaceNormalised: Uniformity[String] = new AbstractStringUniformity {
    override def normalized(s: String): String = s.replaceAll(WhiteSpaceRegex, " ")

    override def toString: String = "whiteSpaceNormalised"
  }

  val whiteSpaceRemoved: Uniformity[String] = new AbstractStringUniformity {
    override def normalized(s: String): String = s.replaceAll(WhiteSpaceRegex, "")

    override def toString: String = "whiteSpaceRemoved"
  }

  val lineWhiteSpaceRemoved: Uniformity[String] = new AbstractStringUniformity {
    override def normalized(s: String): String = {
      // method `linesIterator` is un-deprecated in Scala 2.13 due to collision with the JDK 11's `lines` method.
      // noinspection ScalaDeprecation
      s
        .linesIterator
        .map(_.replaceAll(WhiteSpaceRegex, ""))
        .filter(_.nonEmpty)
        .toArray
        .mkString("\n")
    }

    override def toString: String = "lineWhiteSpaceRemoved"
  }
}
