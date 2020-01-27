/*
 * Copyright 2019 ABSA Group Limited
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

package za.co.absa.commons.json

import org.json4s.Extraction.decompose
import org.json4s.{Formats, JValue, JsonMethods, StringInput}
import za.co.absa.commons.json.AbstractJsonSerDe.{WantsBigIntExtractor, _}
import za.co.absa.commons.json.format.FormatsBuilder
import za.co.absa.commons.reflect.ReflectionUtils.compile
import za.co.absa.commons.reflect.extractors.AccessorMethodValueExtractor

import scala.reflect.Manifest
import scala.reflect.runtime.universe._

trait AbstractJsonSerDe {
  this: FormatsBuilder with JsonMethods[JValue] =>

  private[this] implicit val _formats: Formats = formats

  implicit class EntityToJson[A <: AnyRef](entity: A) {
    def toJson: String = compact(render(decompose(entity)))

    def toJsonAs[B: Manifest]: B = render(decompose(entity)).extract[B]
  }

  implicit class JsonToEntity(json: String) {
    def fromJson[A: Manifest]: A = {
      val args = Map(
        "this" -> AbstractJsonSerDe.this,
        "json" -> StringInput(json),
        "wbd" -> formats.wantsBigDecimal
      )
      val jValue = formats match {
        case WantsBigIntExtractor(wantsBigInt) =>
          parse_json4s_33(args + ("wbi" -> wantsBigInt))
        case _ =>
          parse_json4s_32(args)
      }
      jValue.extract(formats, implicitly[Manifest[A]])
    }
  }

}

object AbstractJsonSerDe {
  private lazy val jsonMethods =
    q"""args[org.json4s.JsonMethods[_]]("this")"""

  private lazy val parse_json4s_32 = compile[JValue](
    q"""$jsonMethods.parse(args("json"), args("wbd"))""")

  private lazy val parse_json4s_33 = compile[JValue](
    q"""$jsonMethods.parse(args("json"), args("wbd"), args("wbi"))""")

  private object WantsBigIntExtractor extends AccessorMethodValueExtractor[Boolean]("wantsBigInt")

}
