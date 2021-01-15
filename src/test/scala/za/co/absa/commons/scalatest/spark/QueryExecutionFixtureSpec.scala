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

package za.co.absa.commons.scalatest.spark

import org.apache.spark.sql.execution.QueryExecution
import org.apache.spark.sql.util.QueryExecutionListener
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

class QueryExecutionFixtureSpec
  extends AsyncFlatSpec
    with Matchers
    with SparkFixture {

  "withQueryExecutionEventCapturing" should "capture query execution event" in {
    withNewSparkSession { implicit spark =>
      import spark.implicits._

      spark.listenerManager.register(new QueryExecutionListener {
        override def onFailure(funcName: String, qe: QueryExecution, exception: Exception): Unit = {}

        override def onSuccess(funcName: String, qe: QueryExecution, durationNs: Long): Unit = {
          print(s">>>> $funcName ...")
          Thread.sleep(3000)
          println(s"OK")
        }
      })

      val eventualRes1 = QueryExecutionCaptor.firstEventOf {
        Seq(1, 2, 3).toDS().show()
      }

      withNewSparkSession { spark =>
        import spark.implicits._
        println(s"#2: count = ${Seq(1, 2, 3).toDS().count()}")
      }

      val eventualRes3 = QueryExecutionCaptor.firstEventOf {
        println(s"#3: list = ${Seq(1, 2, 3).toDS().collect().toList}")
      }

      for {
        e1 <- eventualRes1
        e3 <- eventualRes3
      } yield {
        e1 should not be null
        e3 should not be null
        e1.queryExecution should not be null
        e3.queryExecution should not be null
        e1.queryExecution should not be theSameInstanceAs(e3.queryExecution)
        e1.funcName should equal("head")
        e3.funcName should equal("collect")
      }
    }
  }
}
