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

package za.co.absa.commons.reflect

import scala.annotation.tailrec
import scala.collection.concurrent.TrieMap
import scala.reflect.ClassTag
import scala.reflect.internal.Symbols
import scala.reflect.runtime.universe._
import scala.tools.reflect.ToolBox

/**
  * Reflection utils
  */
object ReflectionUtils {

  private val mirror: Mirror = runtimeMirror(getClass.getClassLoader)
  private val gettersCache = TrieMap.empty[ClassSymbol, Iterable[Symbol]]

  object ModuleClassSymbolExtractor {
    def unapply(o: Any): Option[ClassSymbol] = {
      if (o == null || o.getClass.isSynthetic) None
      else {
        val symbol = mirror.classSymbol(o.getClass)
        if (symbol.isModuleClass) Some(symbol)
        else None
      }
    }
  }

  def compile[A](code: Tree): Map[String, Any] => A = {
    val tb = mirror.mkToolBox()
    val execFn = tb.compile(
      q"""
        (__args: Map[String, Any]) => {
          def args[T](k: String): T = __args(k).asInstanceOf[T]
          $code
        }
      """)()
    execFn.asInstanceOf[Map[String, Any] => A]
  }

  /**
    * Lists all direct sub-classes of the given sealed type T
    *
    * @tparam T sealed type
    * @return sequence of classes
    */
  def directSubClassesOf[T: TypeTag]: Seq[Class[_ <: T]] = {
    val clazz: ClassSymbol = typeOf[T].typeSymbol.asClass
    require(clazz.isSealed, s"$clazz must be sealed")
    clazz.knownDirectSubclasses.toSeq.map((s: Symbol) =>
      mirror.runtimeClass(s.asClass).asInstanceOf[Class[_ <: T]])
  }

  /**
    * Returns a sequence of all known objects that directly inherit from the given sealed type T
    *
    * @tparam T sealed type
    * @return sequence of object instances
    */
  def objectsOf[T <: AnyRef : TypeTag]: Seq[T] = {
    val clazz: ClassSymbol = typeOf[T].typeSymbol.asClass
    require(clazz.isSealed, s"$clazz must be sealed")
    clazz.knownDirectSubclasses.toSeq
      .filter(_.isModuleClass)
      .map((s: Symbol) => objectForName[T](s.fullName))
  }

  /**
    * Returns an object instance with the specified name.
    * Similar to as Class.forName() returns a Class instance by name, this method returns a Scala object instance by name.
    *
    * @param name fully qualified object instance name
    */
  def objectForName[T <: AnyRef](name: String): T = {
    val moduleSymbol = mirror.staticModule(name)
    mirror.reflectModule(moduleSymbol).instance.asInstanceOf[T]
  }

  /**
    * Extract a value from a given field regardless of its visibility.
    * This method utilizes a mix of Java and Scala reflection mechanisms,
    * and can extract from a compiler generated fields as well.
    * Note: if the field has an associated Scala accessor one will be called.
    * Consequently if the filed is lazy it will be initialized.
    *
    * @param o         target object
    * @param fieldName field name to extract value from
    * @tparam A type in which the given field is declared
    * @tparam B expected type of the field value to return
    * @return a field value
    */
  def extractFieldValue[A: ClassTag, B](o: AnyRef, fieldName: String): B = {
    @tailrec
    def reflectClassHierarchy(c: Class[_]): Option[_] =
      if (c == classOf[AnyRef]) None
      else {
        val maybeValue: Option[Any] = reflectClass(c)
        if (maybeValue.isDefined) maybeValue
        else {
          val superClass = c.getSuperclass
          if (superClass == null) None
          else reflectClassHierarchy(superClass)
        }
      }

    def reflectClass(c: Class[_]) = {
      val members =
        try mirror.classSymbol(c).toType.decls
        catch {
          // a workaround for Scala bug #12190
          case _: Symbols#CyclicReference => Nil
        }

      val maybeMember = members
        .filter(smb => (
          smb.toString.endsWith(s" $fieldName")
            && smb.isTerm
            && !smb.isConstructor
            && (!smb.isMethod || smb.asMethod.paramLists.forall(_.isEmpty))
          ))
        .toArray
        .sortBy(!_.isMethod) // method members first
        .headOption

      maybeMember
        .map(m => {
          val im = mirror.reflect(o)
          if (m.isMethod) im.reflectMethod(m.asMethod).apply()
          else im.reflectField(m.asTerm).get
        })
        .orElse {
          // Sometimes certain Scala compiler generated fields aren't return by `TypeApi.members`.
          // Trying Java reflection.
          c.getDeclaredFields.collectFirst {
            case f if f.getName == fieldName =>
              f.setAccessible(true)
              f.get(o)
          }
        }
    }

    def reflectInterfaces(c: Class[_]) = {
      val altNames = allInterfacesOf(c)
        .map(_.getName.replace('.', '$') + "$$" + fieldName)

      c.getDeclaredFields.collectFirst {
        case f if altNames contains f.getName =>
          f.setAccessible(true)
          f.get(o)
      }
    }

    val declaringClass = implicitly[ClassTag[A]].runtimeClass
    reflectClassHierarchy(declaringClass)
      .orElse {
        // The field might be declared in a trait.
        reflectInterfaces(declaringClass)
      }
      .getOrElse(
        throw new NoSuchFieldException(s"${declaringClass.getName}.$fieldName")
      )
      .asInstanceOf[B]
  }

  /**
    * A single type parameter alternative to {{{extractFieldValue[A, B](a, ...)}}} where {{{a.getClass == classOf[A]}}}
    */
  def extractFieldValue[T](o: AnyRef, fieldName: String): T = {
    extractFieldValue[AnyRef, T](o, fieldName)(ClassTag(o.getClass))
  }

  /**
    * Return all interfaces that the given class implements included inherited ones
    *
    * @param c a class
    * @return
    */
  def allInterfacesOf(c: Class[_]): Set[Class[_]] = {
    @tailrec
    def collect(ifs: Set[Class[_]], cs: Set[Class[_]]): Set[Class[_]] =
      if (cs.isEmpty) ifs
      else {
        val c0 = cs.head
        val cN = cs.tail
        val ifsUpd = if (c0.isInterface) ifs + c0 else ifs
        val csUpd = cN ++ (c0.getInterfaces filterNot ifsUpd) ++ Option(c0.getSuperclass)
        collect(ifsUpd, csUpd)
      }

    collect(Set.empty, Set(c))
  }

  /**
    * Same as {{{allInterfacesOf(aClass)}}}
    *
    * @tparam A a class type
    * @return
    */
  def allInterfacesOf[A <: AnyRef : ClassTag]: Set[Class[_]] =
    allInterfacesOf(implicitly[ClassTag[A]].runtimeClass)

  /**
    * Extract object properties as key-value pairs.
    * Here by `properties` we understand public accessors that match a primary constructor arguments.
    * As in a case class, for example.
    *
    * @param obj a target instance (in most cases an instance of a case class)
    * @return a map of property names to their values
    */
  def extractProperties(obj: AnyRef): Map[String, _] = {
    val pMirror = mirror.reflect(obj)
    constructorArgSymbols(pMirror.symbol)
      .map(argSymbol => {
        val name = argSymbol.name.toString
        val value = pMirror.reflectMethod(argSymbol.asMethod).apply()
        name -> value
      })
      .toMap
  }

  private def constructorArgSymbols(classSymbol: ClassSymbol) =
    gettersCache.getOrElseUpdate(classSymbol, {
      val primaryConstr = classSymbol.primaryConstructor

      val paramNames = (
        for {
          pList <- primaryConstr.typeSignature.paramLists
          pSymbol <- pList
        } yield
          pSymbol.name.toString
        )
        .toSet

      classSymbol.info.decls.filter(d =>
        d.isMethod
          && d.asMethod.isGetter
          && paramNames(d.name.toString))
    })

  def caseClassCtorArgDefaultValue[A](t: Class[_], name: String): Option[A] = {
    val classSymbol = mirror.classSymbol(t)
    val companionMirror = {
      val companionModule = classSymbol.companion.asModule
      mirror.reflect(mirror.reflectModule(companionModule).instance)
    }
    val signature = companionMirror.symbol.typeSignature
    val constructor = classSymbol.primaryConstructor.asMethod

    constructor
      .paramLists
      .flatten
      .view
      .zipWithIndex
      .collect({
        case (arg, i) if arg.name.toString == name =>
          signature.member(TermName(s"apply$$default$$${i + 1}"))
      })
      .collectFirst({
        case member if member != NoSymbol =>
          companionMirror.reflectMethod(member.asMethod)().asInstanceOf[A]
      })
  }
}
