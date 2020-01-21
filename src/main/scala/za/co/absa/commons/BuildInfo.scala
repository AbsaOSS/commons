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

package za.co.absa.commons

import java.util.{MissingResourceException, Properties}

import za.co.absa.commons.lang.ARM.using
import za.co.absa.commons.lang.ImmutableProperties
import za.co.absa.commons.lang.OptionImplicits._

/**
  * Returns the project build version taken from the build.properties file.
  * It's particularly convenient for Maven-based project. Just copy 'build.properties.template'
  * file to your classpath (without 'template'-suffix) and enable Maven resource filtering.
  */
object BuildInfo {
  private val resourceName = "build.properties"

  val BuildProps: ImmutableProperties = {
    val stream =
      this.getClass.getResource(s"/$resourceName")
        .asOption
        .map(_.openStream)
        .getOrElse(throw new MissingResourceException(resourceName, classOf[Properties].getName, resourceName))

    using(stream)(ImmutableProperties.fromStream)
  }

  val Version: String = BuildProps.getProperty("build.version")
  val Timestamp: String = BuildProps.getProperty("build.timestamp")
}
