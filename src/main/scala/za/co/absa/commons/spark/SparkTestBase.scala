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

import org.apache.spark.sql.{DataFrame, SparkSession}

trait SparkTestBase { self =>
  implicit val spark: SparkSession = SparkSession.builder()
    .master("local[*]")
    .appName(s"Commons unit testing SchemaUtils")
    .config("spark.ui.enabled", "false")
    .config("spark.debug.maxToStringFields", 100)
    .config("spark.driver.bindAddress", "127.0.0.1")
    .config("spark.driver.host", "127.0.0.1")
    .config("spark.sql.hive.convertMetastoreParquet", false)
    .config("fs.defaultFS","file:/")
    .getOrCreate()

  /**
   * Creates a Spark DataFrame from a JSON document(s).
   *
   * @param json A json string to convert to a DataFrame
   * @return A data frame
   */
  def getDataFrameFromJson(spark: SparkSession, json: Seq[String]): DataFrame = {
    import spark.implicits._
    spark.read.json(json.toDS)
  }
}

