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
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SparkFixtureSpec
  extends AnyFlatSpec
    with Matchers {

  "withNewSparkSession" should "provide a fresh spark session" in new SparkFixture {
    val session1: SparkSession = withNewSparkSession(identity)
    val session2: SparkSession = withNewSparkSession(identity)

    session1 should not be null
    session2 should not be null
    session1 should not(be theSameInstanceAs session2)
  }
}
