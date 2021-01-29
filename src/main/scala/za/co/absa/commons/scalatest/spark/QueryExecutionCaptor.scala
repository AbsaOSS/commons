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

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.execution.QueryExecution
import org.apache.spark.sql.util.QueryExecutionListener
import za.co.absa.commons.scalatest.captor.AsyncCaptor
import za.co.absa.commons.scalatest.captor.AsyncCaptor.ResultCallback
import za.co.absa.commons.scalatest.spark.QueryExecutionCaptor.{EventCapturingQueryExecutionListener, QueryExecutionQualifier}
import za.co.absa.commons.spark.NonFatalQueryExecutionListenerAdapter
import za.co.absa.commons.version.Version
import za.co.absa.commons.version.Version._

import scala.concurrent.Future
import scala.util.{Failure, Success}

class QueryExecutionCaptor private {

  def firstEventOf(action: => Unit)(implicit spark: SparkSession): Future[QueryExecutionEvent] =
    eventOf(_ => true)(action)

  def eventOf(eventPredicate: QueryExecutionQualifier => Boolean)
    (action: => Unit)
    (implicit sparkSession: SparkSession): Future[QueryExecutionEvent] = {
    AsyncCaptor.capture[QueryExecutionEvent] { complete =>
      lazy val listener: QueryExecutionListener = createListener(eventPredicate, result => {
        println(">>> Result: " + result.get.funcName)
        unregisterListener(listener)
        complete(result)
      })

      registerListener(listener)
      action
    }
  }

  private def createListener(
    eventPredicate: QueryExecutionQualifier => Boolean,
    resultCallback: ResultCallback[QueryExecutionEvent]) =
    new EventCapturingQueryExecutionListener(eventPredicate, resultCallback)
      with NonFatalQueryExecutionListenerAdapter

  private def registerListener(listener: => QueryExecutionListener)(implicit spark: SparkSession): Unit = {
    println("+++ REGISTER")
    spark.listenerManager.register(listener)
  }

  private def unregisterListener(listener: QueryExecutionListener)(implicit spark: SparkSession): Unit = {
    // temporary workaround: In Spark 2.x unregistering a listener in the event thread causes a deadlock.
    if (Version.asSemVer(spark.version) > ver"3") {
      println("--- UNREGISTER")
      spark.listenerManager.unregister(listener)
    }
  }
}

object QueryExecutionCaptor extends QueryExecutionCaptor {

  case class QueryExecutionQualifier(funcName: String, qe: QueryExecution)

  class EventCapturingQueryExecutionListener(
    eventPredicate: QueryExecutionQualifier => Boolean,
    resultCallback: AsyncCaptor.ResultCallback[QueryExecutionEvent])
    extends QueryExecutionListener {

    override def onSuccess(funcName: String, qe: QueryExecution, durationNs: Long): Unit = {
      if (eventPredicate(QueryExecutionQualifier(funcName, qe))) {
        resultCallback(Success(QueryExecutionEvent(funcName, qe, durationNs)))
      }
    }

    override def onFailure(funcName: String, qe: QueryExecution, exception: Exception): Unit = {
      if (eventPredicate(QueryExecutionQualifier(funcName, qe))) {
        resultCallback(Failure(exception))
      }
    }
  }

}
