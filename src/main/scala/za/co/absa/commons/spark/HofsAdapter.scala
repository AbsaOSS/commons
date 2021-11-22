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

import org.apache.spark.SPARK_VERSION
import org.apache.spark.sql.Column
import za.co.absa.commons.reflect.ReflectionUtils
import za.co.absa.commons.version.Version
import za.co.absa.commons.version.Version.VersionStringInterpolator

import scala.reflect.runtime.universe._

trait HofsAdapter {

  /**
    * For Spark versions prior 3.0.0, delegates to {{{hofs.transform()}}}
    * Otherwise delegates to the native Spark method.
    */
  val transform: (Column, Column => Column) => Column = {
    val fnRefAST =
      if (Version.asSemVer(SPARK_VERSION) < semver"3.0.0")
        q"za.co.absa.spark.hofs.transform(_: Column, _: Column => Column)"
      else
        q"org.apache.spark.sql.functions.transform(_: Column, _: Column => Column)"
    ReflectionUtils.compile(
      q"""
          import org.apache.spark.sql.Column
          $fnRefAST
          """
    )(Map.empty)
  }

}
