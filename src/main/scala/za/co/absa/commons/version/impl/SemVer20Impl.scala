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

package za.co.absa.commons.version.impl

import za.co.absa.commons.version._
import za.co.absa.commons.version.impl.SemVer20Impl._

import scala.util.matching.Regex

/**
  * Semantic Versioning 2.0 implementation
  *
  * @see https://semver.org/spec/v2.0.0.html
  */
trait SemVer20Impl {

  def asSemVer(verStr: String): Version = verStr match {
    case SemVerRegexp(major, minor, patch, preRelease, buildMeta) =>
      val mainComponents = Seq(
        NumericComponent(major.toInt),
        NumericComponent(minor.toInt),
        NumericComponent(patch.toInt))

      val optionalComponents = Seq(
        Option(preRelease).map(s => PreReleaseComponent(parseIdentifiers(s): _*)),
        Option(buildMeta).map(s => BuildMetadataComponent(parseIdentifiers(s): _*))
      ).flatten

      Version(mainComponents ++ optionalComponents: _*)

    case _ => throw new IllegalArgumentException(s"$verStr does not correspond to the SemVer 2.0 spec")
  }

  private def parseIdentifiers(str: String): Seq[Component] =
    str.split('.') map Component.apply
}

object SemVer20Impl {
  private val SemVerRegexp: Regex = ("^" +
    "(0|[1-9]\\d*)\\." +
    "(0|[1-9]\\d*)\\." +
    "(0|[1-9]\\d*)" +
    "(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?" +
    "(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?" +
    "$").r
}
