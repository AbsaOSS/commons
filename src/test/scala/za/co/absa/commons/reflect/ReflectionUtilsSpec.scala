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

package za.co.absa.commons.reflect

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import za.co.absa.commons.reflect.ReflectionUtils.ModuleClassSymbolExtractor
import za.co.absa.commons.reflect.ReflectionUtilsSpec._

import scala.reflect.runtime.universe._

class ReflectionUtilsSpec extends AnyFlatSpec with Matchers {

  behavior of "ReflectionUtils"

  "compile()" should "compile a given Scala code block and return an eval() function" in {
    val plus = ReflectionUtils.compile[Int](
      q"""
          val x: Int = args("x")
          val y: Int = args("y")
          x + y
        """)
    plus(Map("x" -> 2, "y" -> 40)) should be(42)
  }

  "extractProperties()" should "for given Product return a map of element names to their values" in {
    // case class
    ReflectionUtils.extractProperties(Foo("aaa")) should be(Map("x" -> "aaa", "y" -> 42))
    ReflectionUtils.extractProperties(Foo("aaa", 777)) should be(Map("x" -> "aaa", "y" -> 777))
    // normal class
    ReflectionUtils.extractProperties(new Bar("bbb", 42)) should be(Map("a" -> "bbb"))
  }

  "extractFieldValue()" should "return a value of a private field" in {
    ReflectionUtils.extractFieldValue[Int](Foo, "privateVal") should be(42)
  }

  "extractFieldValue()" should "return values of compiler generated private fields" in {
    val bar = new Bar("Pi", 3.14)
    ReflectionUtils.extractFieldValue[String](bar, "a") shouldEqual "Pi"
    ReflectionUtils.extractFieldValue[Double](bar, "b") shouldEqual 3.14
  }

  "extractFieldValue()" should "extract from a field declared in a superclass" in {
    val subBar = new Bar("Pi", 3.14) {}
    ReflectionUtils.extractFieldValue[Bar, String](subBar, "a") shouldEqual "Pi"
  }

  "ModuleClassSymbolExtractor" should "extract objects" in {
    ModuleClassSymbolExtractor.unapply(Foo) should be(defined)
    ModuleClassSymbolExtractor.unapply("Bar") should not be defined
    ModuleClassSymbolExtractor.unapply({}) should not be defined
    ModuleClassSymbolExtractor.unapply(Array()) should not be defined
  }

  it should "not blow up on nulls or instances of synthetic classes" in {
    ModuleClassSymbolExtractor.unapply(null)
    ModuleClassSymbolExtractor.unapply((x: Int) => x + 1)
  }

  "directSubClassesOf()" should "return direct subclasses of a sealed class/trait" in {
    ReflectionUtils.directSubClassesOf[MyTrait] should be(Seq(classOf[MyClass], MyObject.getClass))
  }

  it should "fail for non-sealed classes" in intercept[IllegalArgumentException] {
    ReflectionUtils.directSubClassesOf[MyClass]
  }

  "objectsOf()" should "return objects of a sealed class/trait" in {
    ReflectionUtils.objectsOf[MyTrait] should be(Seq(MyObject))
  }

  it should "fail for non-sealed classes" in intercept[IllegalArgumentException] {
    ReflectionUtils.objectsOf[MyClass]
  }

  "objectForName()" should "return an 'static' Scala object instance by a full qualified name" in {
    ReflectionUtils.objectForName[AnyRef](MyObject.getClass.getName) should be theSameInstanceAs MyObject
  }

  "caseClassCtorArgDefaultValue()" should "return a case class constructor argument default value if declared" in {
    ReflectionUtils.caseClassCtorArgDefaultValue[Int](classOf[Foo], "x") should be(None)
    ReflectionUtils.caseClassCtorArgDefaultValue[Int](classOf[Foo], "y") should be(Some(42))
  }
}

object ReflectionUtilsSpec {

  sealed trait MyTrait

  class MyClass extends MyTrait

  object MyObject extends MyTrait

  case class Foo(x: String, y: Int = 42)

  object Foo {
    //noinspection ScalaUnusedSymbol
    private[this] val privateVal = 42
  }

  class Bar(val a: String, b: Double) {
    def methodUsingFields: String = a + b.toString
  }

}
