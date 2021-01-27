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

import scala.util.control.NonFatal

/**
  * Since Spark 2.4.6 a passed exception can be a wrapper on top of an Error.
  * This is a controversial decision that this trait is trying to revert
  *
  * @see https://issues.apache.org/jira/browse/SPARK-31144
  */
trait NonFatalQueryExecutionListenerAdapter extends QueryExecutionListener {

  abstract override def onFailure(funcName: String, qe: QueryExecution, exception: Exception): Unit = {
    val causeOrItself = Option(exception.getCause).getOrElse(exception)

    if (NonFatal(causeOrItself))
      super.onFailure(funcName, qe, exception)
  }
}
