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

package za.co.absa.commons.general

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AlgorithmsSpec extends AnyFlatSpec with Matchers {
  case class Person(firstName: String, lastName: String)

  private val people = Seq(
    Person("Andrew", "Mikels"), Person("Andrew", "Gross"),
    Person("Rosetta", "Best"), Person("Julieta", "Guess"),
    Person("Julieta", "Griffey"), Person("Kaitlin", "Griffey"),
    Person("Allison", "Griffey"), Person("Allison", "Brooks")
  )

  private val peopleExpectGroupByFirstNames = Seq(
    Seq(Person("Andrew", "Mikels"), Person("Andrew", "Gross")),
    Seq(Person("Rosetta", "Best")),
    Seq(Person("Julieta", "Guess"), Person("Julieta", "Griffey")),
    Seq(Person("Kaitlin", "Griffey")),
    Seq(Person("Allison", "Griffey"), Person("Allison", "Brooks"))
  )

  private val peopleExpectGroupByLastNames = Seq(
    Seq(Person("Andrew", "Mikels")),
    Seq(Person("Andrew", "Gross")),
    Seq(Person("Rosetta", "Best")),
    Seq(Person("Julieta", "Guess")),
    Seq(Person("Julieta", "Griffey"), Person("Kaitlin", "Griffey"),
      Person("Allison", "Griffey")), Seq(Person("Allison", "Brooks"))
  )

  behavior of "`stableGroupBy`"

  it should "group values in the middle" in {
    val numbers = Seq(1, 2, 2, 2, 1)
    val expected = Seq(Seq(1), Seq(2, 2, 2), Seq(1))

    val actual = Algorithms.stableGroupBy[Int, Int](numbers, a => a)

    actual should equal (expected)
  }

  it should "grouping several groups" in {
    val numbers = Seq(1, 1, 1, 2, 2, 3, 3, 1, 1, 2, 2, 1, 1)
    val expected = Seq(Seq(1, 1, 1), Seq(2, 2), Seq(3, 3), Seq(1, 1), Seq(2, 2), Seq(1, 1))

    val actual = Algorithms.stableGroupBy[Int, Int](numbers, a => a)

    actual should equal (expected)
  }

  it should "not group nulls" in {
    val numbers: Seq[Integer] = Seq(1, 1, 1, null, null, 3, 3, 1, 1, null, null, 1, 1) // scalastyle:ignore null
    val expected = Seq(Seq(1, 1, 1), Seq(null), Seq(null), Seq(3, 3), Seq(1, 1), Seq(null), Seq(null), Seq(1, 1)) // scalastyle:ignore null
    val actual = Algorithms.stableGroupBy[Integer, Integer](numbers, a => a)

    actual should equal (expected)
  }

  it should "handle non-primitive types" in {
    val actualFirstNames = Algorithms.stableGroupBy[Person, String](people, a => a.firstName)
    val actualLastNames = Algorithms.stableGroupBy[Person, String](people, a => a.lastName)

    actualFirstNames should equal (peopleExpectGroupByFirstNames)
    actualLastNames should equal (peopleExpectGroupByLastNames)
  }

  behavior of "`stableGroupByPredicate`"

  it should "not group individual values" in {
    val numbers = Seq(1, 2, 3, 1, 2, 3, 1)
    val expected = Seq(Seq(1), Seq(2), Seq(3), Seq(1), Seq(2), Seq(3), Seq(1))

    val actual = Algorithms.stableGroupByPredicate[Int](numbers, a => a == 1)

    actual should equal (expected)
  }

  it should "handle a group of strings" in {
    val strings = Seq("foo", "bar", "foo", "foo", "bar")
    val expected = Seq(Seq("foo"), Seq("bar"), Seq("foo", "foo"), Seq("bar"))

    val actual = Algorithms.stableGroupByPredicate[String](strings, a => a == "foo")

    actual should equal (expected)
  }

  it should "handle a non-primitive type" in {
    val actual = Algorithms.stableGroupByPredicate[Person](people, a => a.lastName == "Griffey")

    actual should equal (peopleExpectGroupByLastNames)
  }

  it should "group values in the beginning and at the end" in {
    val numbers = Seq(1, 1, 1, 2, 2, 1, 1, 3, 3, 1, 1)
    val expected = Seq(Seq(1, 1, 1), Seq(2), Seq(2), Seq(1, 1), Seq(3), Seq(3), Seq(1, 1))

    val actual = Algorithms.stableGroupByPredicate[Int](numbers, a => a == 1)

    actual should equal (expected)
  }

  behavior of "`stableGroupByOption`"

  it should "group values in the middle" in {
    val numbers = Seq(1, 2, 2, 2, 1)
    val expected = Seq(Seq(1), Seq(2, 2, 2), Seq(1))

    val actual = Algorithms.stableGroupByOption[Int, Int](numbers, a => Some(a))

    actual should equal (expected)
  }

  it should "group only Some(x)" in {
    val numbers = Seq(1, 1, 1, 2, 2, 3, 3, 1, 1, 2, 2, 1, 1)
    val expected = Seq(Seq(1, 1, 1), Seq(2), Seq(2), Seq(3), Seq(3), Seq(1, 1), Seq(2), Seq(2), Seq(1, 1))

    val actual = Algorithms.stableGroupByOption[Int, Int](numbers, a => if (a == 1) Some(a) else None)

    actual should equal (expected)
  }

  it should "handle non-primitive types" in {
    val actualFirstNames = Algorithms.stableGroupByOption[Person, String](people, a => Some(a.firstName))
    val actualLastNames = Algorithms.stableGroupByOption[Person, String](people, a => Some(a.lastName))

    actualFirstNames should equal (peopleExpectGroupByFirstNames)
    actualLastNames should equal (peopleExpectGroupByLastNames)
  }
}
