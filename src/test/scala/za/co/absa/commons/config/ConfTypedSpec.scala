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

package za.co.absa.commons.config

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class ConfTypedSpec extends AnyFlatSpec with Matchers {

  behavior of "`Prop`"

  it should "support simple property" in new ConfTyped {
    Prop("x") shouldEqual "x"
  }

  it should "support hierarchical properties" in new ConfTyped {

    object Foo extends Conf("foo") {

      object Bar extends Conf("bar") {
        val innerProp: String = Prop("inner")
      }

    }

    Foo.Bar.toString shouldEqual "foo.bar"
    Foo.Bar.innerProp shouldEqual "foo.bar.inner"
  }

  it should "support custom root prefix" in new ConfTyped {

    override val rootPrefix: String = "my"

    object Foo extends Conf("foo") {

      object Bar extends Conf("bar") {
        val x: String = Prop("x")
      }

    }

    Foo.Bar.x shouldEqual "my.foo.bar.x"
    Prop("y") shouldEqual "my.y"
  }

  it should "support partially flattened definitions" in new ConfTyped {

    object FooBar extends Conf("foo.bar") {
      val x: String = Prop("x")
    }

    FooBar.x shouldEqual "foo.bar.x"
  }

  it should "support mixin mode" in {
    val testProps = new java.util.Properties with ConfTyped {
      val foo = new Conf("foo") {
        val bar = new Conf("bar") {
          lazy val baz: String = getProperty(Prop("baz"))
        }
      }
    }

    testProps.put("foo.bar.baz", "42")

    testProps.foo.bar.baz should be("42")
  }
}
