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

import org.mockito.Mockito._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.slf4j

class ErrorRefFactorySpec
  extends AnyFlatSpec
    with Matchers
    with MockitoSugar {

  "create()" should "create an ErrorRef with ID and log the exception" in {
    val logMock = mock[slf4j.Logger]
    when(logMock.isErrorEnabled) thenReturn true

    val dummyException = new Exception("Boom!")
    val errRefFactory = new ErrorRefFactory(logMock) {}

    val errRef = errRefFactory.createRef(dummyException, None)

    errRef.errorId should not be null
    errRef.timestamp should (be > 0L and be <= System.currentTimeMillis)
    errRef.message should be(None)
    verify(logMock).error(s"ERROR_ID [${errRef.errorId}]", dummyException)
  }

  it should "create an ErrorRef with ID and Message, and log the exception" in {
    val logMock = mock[slf4j.Logger]
    when(logMock.isErrorEnabled) thenReturn true

    val dummyException = new Exception("Boom!")
    val errRefFactory = new ErrorRefFactory(logMock) {}

    val errRef = errRefFactory.createRef(dummyException, Some("foo bar"))

    errRef.errorId should not be null
    errRef.timestamp should (be > 0L and be <= System.currentTimeMillis)
    errRef.message should be(Some("foo bar"))
    verify(logMock).error(s"ERROR_ID [${errRef.errorId}] foo bar", dummyException)
  }
}
