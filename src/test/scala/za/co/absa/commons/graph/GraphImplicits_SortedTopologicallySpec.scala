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

import za.co.absa.commons.graph.AbstractGraphImplicits_SortedTopologicallySpec.{TestNode, TestNodeId, TestRefNodeIds}
import za.co.absa.commons.graph.GraphImplicits._
import za.co.absa.commons.graph.GraphImplicits_SortedTopologicallySpec._


class GraphImplicits_SortedTopologicallySpec
  extends AbstractGraphImplicits_SortedTopologicallySpec(
    "sortedTopologically",
    (xs: Seq[(TestNodeId, TestRefNodeIds)]) => {
      implicit val nodeIdMapping: DAGNodeIdMapping[TestNode, TestNodeId] = TestNodeIdMapping
      xs.sortedTopologically()
    }
  )

object GraphImplicits_SortedTopologicallySpec {

  implicit object TestNodeIdMapping extends DAGNodeIdMapping[TestNode, TestNodeId] {

    override def currentId(n: TestNode): TestNodeId = n._1

    override def outboundIds(n: TestNode): Seq[TestNodeId] = n._2
  }

}
