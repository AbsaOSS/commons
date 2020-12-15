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

package za.co.absa.commons.json

import za.co.absa.commons.json.AbstractJsonSerDeSpec.{Bar, Foo, Qux}
import za.co.absa.commons.json.format.{JavaTypesSupport, NoEmptyValuesSupport}
import za.co.absa.commons.scalatest.WhitespaceNormalizations._

import java.util.UUID

class DefaultJacksonJsonSerDeSpec
  extends AbstractJsonSerDeSpec(new DefaultJacksonJsonSerDe {}) {

  // Jackson impl specific behaviour

  behavior of "deserialization"

  it should "deserialize simple types" in new DefaultJacksonJsonSerDe {
    "\"s\"".fromJson[String] should equal("s")
    "12345".fromJson[Int] should equal(12345)
    "false".fromJson[Boolean] should be(false)
  }

  behavior of "NoEmptyValuesSupport"

  it should "omit empty values in `toJson`" in new DefaultJacksonJsonSerDe with NoEmptyValuesSupport {
    Foo().toJson should be(empty)
    Foo(Some(42)).toJson should equal("""{"any":42}""")
    Foo(Some(Bar)).toJson should be(empty)
    Foo(Some(Foo(Some(7)))).toJson should equal("""{"any":{"any":7}}""")
    Foo(anySeq = Seq(Map("a" -> 1, "b" -> 2), Map("c" -> 3))).toJson should equal("""{"anySeq":[{"a":1,"b":2},{"c":3}]}""")
    Foo(quxSeq = Seq(Qux(1), Qux(2))).toJson should equal("""{"quxSeq":[{"z":1},{"z":2}]}""")
    Bar(None, map = Map(
      "00" -> Seq(),
      "01" -> Seq(Seq.empty, Map.empty, null, ""),
      "02" -> Seq(Seq(Map("x" -> Map("y" -> Seq.empty, "z" -> Map.empty)))),
      "a" -> null,
      "b" -> None,
      "c" -> Nil,
      "d" -> "",
      "e" -> new AnyRef)
    ).toJson should be(empty)
  }

  it should "omit empty values in `toPrettyJson`" in new DefaultJacksonJsonSerDe with NoEmptyValuesSupport {
    Foo()
      .toPrettyJson should be(empty)
    Foo(Some(42))
      .toPrettyJson should equal("""{"any":42}""")(after being whiteSpaceRemoved)
    Foo(Some(Bar))
      .toPrettyJson should be(empty)
    Foo(Some(Foo(Some(7))))
      .toPrettyJson should equal("""{"any":{"any":7}}""")(after being whiteSpaceRemoved)
    Foo(anySeq = Seq(Map("a" -> 1, "b" -> 2), Map("c" -> 3)))
      .toPrettyJson should equal("""{"anySeq":[{"a":1,"b":2},{"c":3}]}""")(after being whiteSpaceRemoved)
    Foo(quxSeq = Seq(Qux(1), Qux(2)))
      .toPrettyJson should equal("""{"quxSeq":[{"z":1},{"z":2}]}""")(after being whiteSpaceRemoved)
    Bar(None, map = Map(
      "00" -> Seq(),
      "01" -> Seq(Seq.empty, Map.empty, null, ""),
      "02" -> Seq(Seq(Map("x" -> Map("y" -> Seq.empty, "z" -> Map.empty)))),
      "a" -> null,
      "b" -> None,
      "c" -> Nil,
      "d" -> "",
      "e" -> new AnyRef)
    ).toPrettyJson should be(empty)
  }

  it should "preserve sparse arrays length and order" in new DefaultJacksonJsonSerDe with NoEmptyValuesSupport {
    Foo(anySeq = Seq(null, "", 42, Seq.empty, Map.empty)).toJson should equal("""{"anySeq":[null,null,42,null,null]}""")
    Foo(anySeq = Seq(Seq(Seq.empty, ""), 42, Map("zz" -> ""))).toJson should equal("""{"anySeq":[null,42,null]}""")
  }

  behavior of "JavaTypesSupport"

  it should "support UUID" in new DefaultJacksonJsonSerDe with JavaTypesSupport {
    UUID.fromString("8460b4a5-fcb9-4ad1-845d-a417b300f33a").toJson should equal(""""8460b4a5-fcb9-4ad1-845d-a417b300f33a"""")
    """["8460b4a5-fcb9-4ad1-845d-a417b300f33a"]""".fromJson[Seq[UUID]] should equal(Seq(UUID.fromString("8460b4a5-fcb9-4ad1-845d-a417b300f33a")))
  }
}
