/*
 * Copyright 2019 ABSA Group Limited
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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SparkUtilsSpec extends AnyFlatSpec with Matchers with SparkTestBase {
  it should "test a dataframe created from a JSON" in {
    val inputJson = Seq("""{"value":1}""", """{"value":2}""")

    val df = SparkUtils.getDataFrameFromJson(spark, inputJson)

    val expectedSchema = """root
                           | |-- value: long (nullable = true)
                           |""".stripMargin.replace("\r\n", "\n")
    val actualSchema = df.schema.treeString

    actualSchema should equal (expectedSchema)
  }
}
