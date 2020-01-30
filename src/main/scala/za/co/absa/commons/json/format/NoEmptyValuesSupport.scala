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

package za.co.absa.commons.json.format

import org.json4s.JsonAST.JString
import org.json4s.prefs.EmptyValueStrategy
import org.json4s.{Formats, JArray, JField, JNothing, JNull, JObject, JValue}

/**
 * Removes all empty values from the AST.
 * By `empty value` we mean `null`, empty string, empty array/object or array/object that only consists of empty items.
 *
 * In order to preserve the length and offsets in the arrays that have at least one non-empty item,
 * the rest empty items are not removed but replaced with `null`.
 */
trait NoEmptyValuesSupport extends FormatsBuilder {
  abstract override protected def formats: Formats = super.formats.withEmptyValueStrategy(new EmptyValueStrategy {

    private val replaceEmptyLeaf: PartialFunction[JValue, JNothing.type] = {
      case JNull => JNothing
      case JString("") => JNothing
    }

    private val replaceEmptyTree: PartialFunction[JValue, JValue] = {
      case JObject(emptyFields) if emptyFields forall { case (_, v) => v == JNothing } => JNothing
      case JArray(emptyItems) if emptyItems forall JNothing.== => JNothing
      case JArray(items) => JArray(items map {
        case JNothing => JNull // to preserve array length and non-empty items offsets
        case jValue => jValue
      })
    }

    private def recursively(fn: JValue => JValue): PartialFunction[JValue, JValue] = {
      case JArray(items) => JArray(items map fn)
      case JObject(fields) => JObject(fields map {
        case JField(name, v) => JField(name, fn(v))
      })
    }

    private val recursivelyReplaceEmpty: JValue => JValue =
      replaceEmptyLeaf
        .orElse(recursively(replaceEmpty) andThen (replaceEmptyTree orElse { case x => x }))
        .orElse({ case x => x })

    override def replaceEmpty(value: JValue): JValue = recursivelyReplaceEmpty(value)

    override def noneValReplacement: Option[AnyRef] = None
  })
}
