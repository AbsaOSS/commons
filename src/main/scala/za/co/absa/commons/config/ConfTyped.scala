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

package za.co.absa.commons.config

import scala.language.implicitConversions

/**
  * Trait that provides a DSL for creating a typed hierarchical configuration object.
  * <p>
  * It provides access to two main abstractions: <code>Conf</code> and <code>Prop</code>
  * </p>
  *
  * <p>
  * Example:
  *
  * <pre>
  * import za.co.absa.commons.config._
  *
  * object MyAppConfig extends ConfTyped {
  * val confSource: java.util.Properties = ???
  *
  * override val rootPrefix = "com.example"
  *
  * object Foo extends Conf("foo") {
  * object Bar extends Conf("bar") {
  * val baz: String = confSource getProperty Prop("baz")
  * val qux: String = confSource getProperty Prop("qux")
  * }
  * }
  * }
  *
  * // somewhere in your application
  *
  * import MyAppConfig._
  *
  * val baz = Foo.Bar.baz // mapped to the key "com.example.foo.bar.baz" in the <code>confSource</code>
  * val baz = Foo.Bar.qux // mapped to the key "com.example.foo.bar.qux" in the <code>confSource</code>
  * </pre>
  * </p>
  *
  * Note that [[ConfTyped]] doesn't impose or depend on the way how the configuration values are loaded.
  * It only provides a convenient way to implicitly construct the configuration key names from the nested object structure.
  *
  * <p>
  * The key names are obtained by calling <code>Prop("...")</code> method.
  * It returns a full property key name that reflects the nesting structure of the <code>Conf</code> instances' names,
  * concatenated with dot (.) and prefixed with the <code>rootPrefix</code> if one is provided.
  * </p>
  * <p/>
  * <p>
  * Another example of usage [[ConfTyped]]:
  * <pre>
  * val props = new java.util.Properties with ConfTyped {
  * val foo = new Conf("foo") {
  * val bar = new Conf("bar") {
  * lazy val baz = getProperty(Prop("baz"))
  * }
  * }
  * }
  *
  *     props.put("foo.bar.baz", "42")
  *
  * println(props.foo.bar.baz) // prints 42
  * </pre>
  * </p>
  **/
trait ConfTyped {
  protected def rootPrefix: String = null

  private def rootPrefixOpt: Option[Conf] = Option(rootPrefix).map(new Conf(_)(None))

  protected class Conf(name: String)(implicit prefixOpt: Option[Conf] = rootPrefixOpt) {

    protected implicit def asOption: Option[Conf] = Some(this)

    override def toString: String = prefixOpt.toSeq :+ name mkString "."
  }

  protected object Prop {
    def apply(name: String)(implicit prefix: Option[Conf] = rootPrefixOpt): String = new Conf(name)(prefix).toString
  }

}
