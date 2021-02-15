/*
 * Copyright 2021 ABSA Group Limited
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

package za.co.absa.commons.graph

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers._
import org.scalatest.matchers.should.Matchers
import za.co.absa.commons.graph.AbstractGraphImplicits_SortedTopologicallySpec._

import scala.collection.mutable.ListBuffer

abstract class AbstractGraphImplicits_SortedTopologicallySpec(
  topologicalSortMethodName: String,
  executeTopologicalSortMethod: Seq[TestNode] => Seq[TestNode]
) extends AnyFlatSpec
  with Matchers {

  behavior of "DAGNodeTraversableOps"

  behavior of topologicalSortMethodName

  it should "sort node collection in topological order" in {
    val originalCollection = Seq(
      4 -> Seq(5),
      2 -> Seq(4),
      3 -> Nil,
      1 -> Seq(2, 3, 5),
      5 -> Nil
    )

    val sortedCollection = executeTopologicalSortMethod(originalCollection)

    sortedCollection should contain theSameElementsAs originalCollection
    sortedCollection should be(topologicallySorted)
  }

  it should "support disconnected graphs" in {
    val disconnectedGraph = Seq(
      // graph A
      1 -> Seq(2, 3),
      3 -> Nil,
      2 -> Seq(3),
      // graph B
      10 -> Seq(20, 30),
      30 -> Nil,
      20 -> Seq(30)
    )

    val sortedGraph = executeTopologicalSortMethod(disconnectedGraph)

    sortedGraph should contain theSameElementsAs disconnectedGraph
    sortedGraph should be(topologicallySorted)
  }

  it should "not remove disconnected nodes" in {
    val col2 = Seq(1 -> Nil, 2 -> Nil, 3 -> Nil)
    executeTopologicalSortMethod(col2) should contain theSameElementsAs col2
  }

  it should "do nothing on empty collections" in {
    val col0 = Seq.empty
    executeTopologicalSortMethod(col0) should be theSameInstanceAs col0
  }

  it should "always return another instance of mutable collections" in {
    val arr0 = Array.empty[TestNode]
    val buf1 = ListBuffer(1 -> Nil)
    executeTopologicalSortMethod(arr0) should (have length 0 and not(be theSameInstanceAs arr0))
    executeTopologicalSortMethod(buf1) should (contain theSameElementsAs buf1 and not(be theSameInstanceAs buf1))
  }

  it should "fail due to a cycle in the graph" in {
    val cyclicGraph = Seq(
      1 -> Seq(2),
      2 -> Seq(3),
      3 -> Seq(1)
    )
    intercept[IllegalArgumentException] {
      executeTopologicalSortMethod(cyclicGraph)
    }
  }
}

object AbstractGraphImplicits_SortedTopologicallySpec {

  type TestNodeId = Int
  type TestRefNodeIds = Seq[TestNodeId]
  type TestNode = (TestNodeId, TestRefNodeIds)

  private def topologicallySorted: BeMatcher[Seq[TestNode]] = TopologicallySortedMatcher

  private object TopologicallySortedMatcher extends BeMatcher[Seq[TestNode]] {
    override def apply(xs: Seq[TestNode]): MatchResult = {
      val seen = xs.foldLeft(Option(Set.empty[Int])) {
        case (Some(seen), (id, ids)) if ids forall (!seen(_)) => Some(seen + id)
        case _ => None // mismatch found, skip the rest of nodes
      }
      MatchResult(
        seen.isDefined,
        s"${asString(xs)} was not sorted in topological order",
        s"${asString(xs)} was sorted in topological order"
      )
    }

    private def asString(xs: Seq[TestNode]): String =
      xs.map { case (id, ids) =>
        val sb = new StringBuilder
        sb ++= id.toString
        if (ids.nonEmpty) sb ++= s" -> ${ids mkString ","}"
        s"($sb)"
      }.toString
  }

}
