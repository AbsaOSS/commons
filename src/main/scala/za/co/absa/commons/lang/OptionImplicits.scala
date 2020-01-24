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

import za.co.absa.commons.lang.TypeConstraints.not

object OptionImplicits {

  implicit class StringWrapper(val s: String) extends AnyVal {
    def nonBlankOption: Option[String] =
      if (s == null) None
      else if (s.trim.isEmpty) None
      else Some(s)
  }

  implicit class TraversableWrapper[A <: Traversable[_]](val xs: A) extends AnyVal {
    def asOption: Option[A] = if (xs.isEmpty) None else Some(xs)
  }

  implicit class NonOptionWrapper[A <: Any : not[Option[_]]#λ](a: A) {
    def asOption: Option[A] = Option(a)
  }

  implicit class AnyWrapper[A <: Any](a: A) {
    def optionally[B](applyFn: (A, B) => A, maybeArg: Option[B]): A = maybeArg.map(applyFn(a, _)).getOrElse(a)
  }

}
