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

import scalax.collection.Graph
import scalax.collection.GraphEdge.DiEdge
import scalax.collection.GraphPredef.EdgeAssoc

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

object GraphImplicits {

  trait DAGNodeIdMapping[Node, Id] {
    def currentId(n: Node): Id
    def outboundIds(n: Node): Traversable[Id]
  }

  implicit class DAGNodeTraversableOps[Node, M[X] <: Seq[X]](val xs: M[Node]) extends AnyVal {

    def sortedTopologicallyBy[Id](
      currentIdFn: Node => Id,
      outboundIdsFn: Node => Traversable[Id],
      reverse: Boolean = false
    )(implicit cbf: CanBuildFrom[M[Node], Node, M[Node]]): M[Node] = {
      implicit val nav: DAGNodeIdMapping[Node, Id] = new DAGNodeIdMapping[Node, Id] {

        override def currentId(n: Node): Id = currentIdFn(n)

        override def outboundIds(n: Node): Traversable[Id] = outboundIdsFn(n)
      }

      sortedTopologically(reverse)
    }

    def sortedTopologically[Id](reverse: Boolean = false)(implicit nav: DAGNodeIdMapping[Node, Id], cbf: CanBuildFrom[M[Node], Node, M[Node]]): M[Node] =
      if (xs.size < 2) xs
      else {
        val itemById = xs.map(op => nav.currentId(op) -> op).toMap

        val createEdge: (Node, Id) => DiEdge[Node] =
          if (reverse)
            (item, nextId) => itemById(nextId) ~> item
          else
            (item, nextId) => item ~> itemById(nextId)

        val edges: Traversable[DiEdge[Node]] =
          for {
            item <- xs
            nextId <- nav.outboundIds(item)
          } yield createEdge(item, nextId)

        val sortResult = Graph
          .from(
            edges = edges,
            nodes = xs)
          .topologicalSort

        sortResult match {
          case Right(res) =>
            val b = cbf(xs)
            b ++= res.toOuter
            b.result()
          case Left(cycleNode) =>
            throw new IllegalArgumentException(s"Expected DAG but a cycle was detected on the node: $cycleNode")
        }
      }
  }

}
