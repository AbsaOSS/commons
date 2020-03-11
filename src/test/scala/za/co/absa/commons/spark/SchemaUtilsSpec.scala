/*
 * Copyright 2020 ABSA Group Limited
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

import org.apache.spark.sql.AnalysisException
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SchemaUtilsSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll with SparkTestBase {
  import spark.implicits._

  val jsonA = """[{"id":1,"legs":[{"legid":100,"conditions":[{"checks":[{"checkNums":["1","2","3b","4","5c","6"]}],"amount":100}]}], "key" : {"alfa": "1", "beta": {"beta2": "2"}} }]"""
  val jsonB = """[{"id":1,"legs":[{"legid":100,"conditions":[{"checks":[{"checkNums":["1","2","3b","4","5c","6"]}],"amount":100,"price":10}]}]}]"""
  val jsonC = """[{"legs":[{"legid":100,"conditions":[{"amount":100,"checks":[{"checkNums":["1","2","3b","4","5c","6"]}]}]}],"id":1, "key" : {"beta": {"beta2": "2"}, "alfa": "1"} }]"""
  val jsonD = """[{"legs":[{"legid":100,"conditions":[{"amount":100,"checks":[{"checkNums":["1","2","3b","4","5c","6"]}]}]}],"id":1, "key" : {"beta": {"beta2": 2}, "alfa": 1} }]"""

  behavior of "isSameSchema"

  it should "say true for the same schemas" in {
    val dfA1 = spark.read.json(Seq(jsonA).toDS)
    val dfA2 = spark.read.json(Seq(jsonA).toDS)

    SchemaUtils.equivalentSchemas(dfA1.schema, dfA2.schema) should be(true)
  }

  it should "say false when first schema has an extra field" in {
    val dfA = spark.read.json(Seq(jsonA).toDS)
    val dfB = spark.read.json(Seq(jsonB).toDS)

    SchemaUtils.equivalentSchemas(dfA.schema, dfB.schema) should be(false)
  }

  it should "say false when second schema has an extra field" in {
    val dfA = spark.read.json(Seq(jsonA).toDS)
    val dfB = spark.read.json(Seq(jsonB).toDS)

    SchemaUtils.equivalentSchemas(dfB.schema, dfA.schema) should be(false)
  }

  behavior of "alignSchema"

  it should "order schemas for equal schemas" in {
    val dfA = spark.read.json(Seq(jsonA).toDS)
    val dfC = spark.read.json(Seq(jsonC).toDS).select("legs", "id", "key")

    val dfA2Aligned = SchemaUtils.alignSchema(dfC, dfA.schema)

    dfA.columns.toSeq should equal(dfA2Aligned.columns.toSeq)
    dfA.select("key").columns.toSeq should equal(dfA2Aligned.select("key").columns.toSeq)
  }

  it should "throw an error for DataFrames with different schemas" in {
    val dfA = spark.read.json(Seq(jsonA).toDS)
    val dfB = spark.read.json(Seq(jsonB).toDS)

    intercept[AnalysisException] {
      SchemaUtils.alignSchema(dfA, dfB.schema)
    }
  }

  behavior of "diffSchema"

  it should "produce a list of differences with path for schemas with different columns" in {
    val schemaA = spark.read.json(Seq(jsonA).toDS).schema
    val schemaB = spark.read.json(Seq(jsonB).toDS).schema

    SchemaUtils.diffSchema(schemaA, schemaB) should equal(List("key cannot be found in both schemas"))
    SchemaUtils.diffSchema(schemaB, schemaA) should equal(List("legs.conditions.price cannot be found in both schemas"))
  }

  it should "produce a list of differences with path for schemas with different column types" in {
    val schemaC = spark.read.json(Seq(jsonC).toDS).schema
    val schemaD = spark.read.json(Seq(jsonD).toDS).schema

    val result = List(
      "key.alfa data type doesn't match (string) vs (long)",
      "key.beta.beta2 data type doesn't match (string) vs (long)"
    )

    SchemaUtils.diffSchema(schemaC, schemaD) should equal(result)
  }

  it should "produce an empty list for identical schemas" in {
    val schemaA = spark.read.json(Seq(jsonA).toDS).schema
    val schemaB = spark.read.json(Seq(jsonA).toDS).schema

    SchemaUtils.diffSchema(schemaA, schemaB).isEmpty should be(true)
    SchemaUtils.diffSchema(schemaB, schemaA).isEmpty should be(true)
  }
}
