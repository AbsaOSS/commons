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

package za.co.absa.commons.scalatest

import org.scalatest.{BeforeAndAfterEach, Suite}
import za.co.absa.commons.reflect.ReflectionUtils.extractFieldValue
import za.co.absa.commons.scalatest.EnvFixture.EnvMapHandlers

import scala.collection.JavaConverters._
import scala.reflect.ClassTag
import scala.util.Try

trait EnvFixture extends BeforeAndAfterEach {
  this: Suite =>

  private var keysBefore: Set[String] = _

  override protected def beforeEach() {
    this.keysBefore = EnvMapHandlers.head.keys
    super.beforeEach()
  }

  override protected def afterEach() {
    try super.afterEach()
    finally {
      val keysAfter = EnvMapHandlers.head.keys
      val keysToRemove = keysAfter -- keysBefore
      for {
        k <- keysToRemove
        h <- EnvMapHandlers
      } h.remove(k)
    }
  }

  def setEnv(key: String, value: String): Unit =
    EnvMapHandlers.foreach(_.put(key, value))
}

object EnvFixture {

  private type EnvMap = java.util.Map[String, String]

  private val EnvMapHandlers: Seq[MapHandler] = Seq(
    Some(new TheUnmodifiableEnvironment),
    Try(new TheCaseInsensitiveEnvHandler).toOption
  ).flatten

  private abstract class MapHandler {
    protected def m: EnvMap

    final def keys: Set[String] = m.keySet.asScala.toSet

    final def put(k: String, v: String): Unit = m.put(k, v)

    final def remove(k: String): Unit = m.remove(k)
  }

  private class TheUnmodifiableEnvironment extends MapHandler {
    override protected val m: EnvMap = extractFieldValue[EnvMap](System.getenv(), "m")
  }

  // Windows JDK has this
  private class TheCaseInsensitiveEnvHandler extends MapHandler {
    override protected val m: EnvMap = {
      val pe: Class[_] = Class.forName("java.lang.ProcessEnvironment")
      extractFieldValue[Class[_], EnvMap](pe, "theCaseInsensitiveEnvironment")(ClassTag(pe))
    }
  }

}
