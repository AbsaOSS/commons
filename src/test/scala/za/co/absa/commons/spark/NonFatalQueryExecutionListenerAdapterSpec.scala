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

package za.co.absa.commons.spark

import org.apache.spark.sql.execution.QueryExecution
import org.apache.spark.sql.util.QueryExecutionListener
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import za.co.absa.commons.spark.NonFatalQueryExecutionListenerAdapterSpec.ExceptionCollectingQueryExecutionListener

class NonFatalQueryExecutionListenerAdapterSpec
  extends AnyFlatSpec
    with Matchers {

  it should "only allow non-fatal exceptions to pass through" in {
    val lsn = new ExceptionCollectingQueryExecutionListener with NonFatalQueryExecutionListenerAdapter

    val ex1 = new Exception("one")
    val ex2 = new Exception("two", new Exception("I'm non-fatal"))

    val threadDeath = new ThreadDeath
    val threadInterruption = new InterruptedException
    val oom = new OutOfMemoryError

    val evilEx1 = new Exception(threadDeath)
    val evilEx2 = new Exception(threadInterruption)
    val evilEx3 = new Exception(oom)

    lsn.onFailure(null, null, ex1)
    lsn.onFailure(null, null, evilEx1)
    lsn.onFailure(null, null, evilEx2)
    lsn.onFailure(null, null, evilEx3)
    lsn.onFailure(null, null, threadInterruption)
    lsn.onFailure(null, null, ex2)

    lsn.collectedExecutions should contain theSameElementsInOrderAs Seq(ex1, ex2)
  }

}

object NonFatalQueryExecutionListenerAdapterSpec {

  private class ExceptionCollectingQueryExecutionListener extends QueryExecutionListener {
    private var _exceptions: Vector[Exception] = Vector.empty

    def collectedExecutions: Seq[Exception] = _exceptions

    override def onFailure(funcName: String, qe: QueryExecution, exception: Exception): Unit = {
      this._exceptions :+= exception
    }

    override def onSuccess(funcName: String, qe: QueryExecution, durationNs: Long): Unit =
      throw new UnsupportedOperationException
  }

}
