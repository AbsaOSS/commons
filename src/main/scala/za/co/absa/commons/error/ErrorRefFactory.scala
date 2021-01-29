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

package za.co.absa.commons.error

import org.slf4j.Logger

import java.util.UUID.randomUUID

abstract class ErrorRefFactory(log: Logger) {

  /**
    * Create an `ErrorRef` instance and log the given exception with the unique identifier.
    *
    * @param e            an exception to be tagged and logged in the server logs.
    * @param maybeMessage optional client-facing error message.
    *                     If None is provided then the reference is created without any message, just an error ID.
    *                     The original exception message is not used for this purpose to avoid potential leaking of
    *                     server implementational details or a sensitive data.
    * @return a new instance of `ErrorRef`.
    */
  protected[error] def createRef(e: Throwable, maybeMessage: Option[String]): ErrorRef = {
    val id = randomUUID
    val time = System.currentTimeMillis
    val errRef = ErrorRef(id, time, maybeMessage)
    if (log.isErrorEnabled) {
      val logMsg = {
        val sb = new StringBuilder()
        sb ++= "ERROR_ID [" ++= errRef.errorId.toString ++= "]"
        maybeMessage.foreach(s => sb += ' ' ++= s)
        sb.result()
      }
      log.error(logMsg, e)
    }
    errRef
  }
}
