ABSA Commons
===
Selection of useful reusable components

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/za.co.absa.commons/commons_2.12/badge.svg)](https://search.maven.org/search?q=g:za.co.absa.commons)
[![TeamCity build](https://teamcity.jetbrains.com/app/rest/builds/aggregated/strob:%28locator:%28buildType:%28id:OpenSourceProjects_AbsaOSS_Commons_AutoBuildWithScala212%29,branch:master%29%29/statusIcon.svg)](https://teamcity.jetbrains.com/viewType.html?buildTypeId=OpenSourceProjects_AbsaOSSSpline_AutomaticBuildsWithTests_Spark24&branch=develop&tab=buildTypeStatusDiv)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=AbsaOSS_commons&metric=alert_status)](https://sonarcloud.io/dashboard?id=AbsaOSS_commons)
[![SonarCloud Maintainability](https://sonarcloud.io/api/project_badges/measure?project=AbsaOSS_commons&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=AbsaOSS_commons)
[![SonarCloud Reliability](https://sonarcloud.io/api/project_badges/measure?project=AbsaOSS_commons&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=AbsaOSS_commons)
[![SonarCloud Security](https://sonarcloud.io/api/project_badges/measure?project=AbsaOSS_commons&metric=security_rating)](https://sonarcloud.io/dashboard?id=AbsaOSS_commons)

---

# Building

### Switch the codebase to the required Scala version.
By default, Scala 2.11 is used. To build _Commons_ for another Scala version, switch to the required Scala version first. 
```shell
# E.g. to switch to Scala 2.13 use
mvn scala-cross-build:change-version -Pscala-2.13
```

### Build the project
When building the project activate a Scala profile corresponding to the Scala version of the codebase.
```shell
# E.g. for Scala 2.13 use
mvn clean install -Pscala-2.13
```

### Building for all supported Scala versions
```shell
./build-all.sh
```

### Measuring code coverage
```shell
./mvn clean verify -Pcode-coverage
```
Code coverage will be generated on path:
```
{local-path}\commons\target\jacoco
```

# Type extensions
## AnyExtension
```scala
import za.co.absa.commons.lang.extensions.AnyExtension._

// Optionally call a method in a chain
new MyBuilder
  .withX(42)
  .withY(77)
  .optionally(_.withZ, maybeZ) // <---- withZ is called with a `value` if `maybeZ` is `Some(value)`
  .optionally(_.withABC(a, _, c), maybeB) // <---- it also works with n-ary methods

// Alternatively `having()` method can be used. It does the same thing as `optionally()`,
// but because of re-arranged method parameters it's easier for the compiler to infer types.
// See: https://github.com/AbsaOSS/commons/issues/56
new MyBuilder
  .withX(42)
  .withY(77)
  .having(maybeZ)(_.withZ)
  
// Conditionally call a method in a chain
new MyBuilder
  .withX(42)
  .withY(77)
  .when(shouldIncludeZ)(_.withZ(88))

// ...same as `when()` but with inverted condition
new MyBuilder
  .withX(42)
  .withY(77)
  .unless(shouldExcludeZ)(_.withZ(88))
```
## ArrayExtension
```scala
import za.co.absa.commons.lang.extensions.ArrayExtension._

val arr = Array(1, 2, 3)

// removes duplicates in an array;
// an argument to distinctBy is a function which projects each array value into another value
// that is used to determine whether two elements are duplicated
val duplicatesEliminated = arr.distinctBy(identity)
```
## IteratorExtension
```scala
import za.co.absa.commons.lang.extensions.IteratorExtension._

val iter: Iterator[_] = ???
val arr: Array[_] = ???

// copy 42 items from the `iter` to the `arr` with array offset 7
iter.fetchToArray(arr, 7, 42) // returns a number of actually copied items
```
## NonOptionExtension
```scala
import za.co.absa.commons.lang.extensions.NonOptionExtension._

// returns the object as Some(_) if anyNonOptionObject is not null, None otherwise
anyNonOptionObject.toOption
```
## OptionExtension
```scala
import za.co.absa.commons.lang.extensions.OptionExtension._

val someOption = Some("abc")

// returns Success("abc")
someOption.toTry(new Exception)

val noneOption = None
val e = new Exception

// returns Failure(e)
someOption.toTry(e)
```
## SeqExtension
```scala
import za.co.absa.commons.lang.extensions.SeqExtension._

Seq(1, 2, 2, 2, 1).groupConsecutiveBy[Int](a => a) // Seq(Seq(1), Seq(2, 2, 2), Seq(1))
Seq(1, 2, 2, 2, 1).groupConsecutiveBy[Int](a => 1) // Seq(Seq(1, 2, 2, 2, 1))
Seq(1, 24, 27, 2, 1).groupConsecutiveBy[Int](a => a.toString.length) // Seq(Seq(1), Seq(24, 27), Seq(2, 1))

Seq(1, 2, 2, 2, 1).groupConsecutiveByPredicate(a => a == 2) // Seq(Seq(1), Seq(2, 2, 2), Seq(1))
Seq(1, 1, 2, 2, 4, 1).groupConsecutiveByPredicate(a => a == 2) // Seq(Seq(1, 1), Seq(2), Seq(2), Seq(4), Seq(1))

Seq(1, 24, 27, 2, 1).groupConsecutiveByOption[Int](
  a => if(a.toString.length > 1) Some(a.toString.length) else None
) // Seq(Seq(1), Seq(24, 27), Seq(2), Seq(1))
```
## StringExtension
```scala
import za.co.absa.commons.lang.extensions.StringExtension._

"abcba".replaceChars(Map('a' -> 'b', 'b' -> 'a')) // "bacab"

"Hello world".findFirstUnquoted(Set('w', 'e', 'l'), Set.empty) // Some(1)
"Hello world".findFirstUnquoted(Set('w'), Set.empty) // Some(6)
"Hello world".findFirstUnquoted(Set('a'), Set.empty) // None
"Hello `w`orld".findFirstUnquoted(Set('w'), '`') // None
"Hello `world".findFirstUnquoted(Set('w'), '`') // Some(7)
"`Hello` \\'world".findFirstUnquoted(Set('w', 'e', 'l'), Set('\'', '`')) // Some(10)

"Hello world".hasUnquoted(Set('w', 'e', 'l'), Set('`')) // true
"`Hello world`".hasUnquoted(Set('w', 'e', 'l'), Set('`')) // false

"Lorem i ipsum".countUnquoted(Set('o', 'i'), Set.empty) // Map('o' -> 1, 'i' -> 2)
"Lorem `i` ipsum".countUnquoted(Set('o', 'i'), Set('`')) // Map('o' -> 1, 'i' -> 1)

"aaa" / "123" // "aaa/123"
"aaa/" / "123" // "aaa/123"

"a".nonEmptyOrElse("b") // "a"
"".nonEmptyOrElse("b") // "b"

"".coalesce("A", "") // "A"
"".coalesce("", "", "B", "", "C") // "B"
"X".coalesce("Y", "Z") // "X"

(null: String).nonBlankOption // None
"            ".nonBlankOption // None
" foo bar 42 ".nonBlankOption // Some(" foo bar 42 ")
```
## TraversableExtension
```scala
import za.co.absa.commons.lang.extensions.TraversableExtension._

Traversable(1, 2, 3).toNonEmptyOption // Some(Traversable(1, 2, 3))
Traversable().toNonEmptyOption // None
```
## TraversableOnceExtension
```scala
import za.co.absa.commons.lang.extensions.TraversableOnceExtension._

List(1, 2).distinctBy(identity) // List(1, 2)
List(1, 2, 1).distinctBy(identity) // List(1, 2)
List(1, 2, 1, 0, 5).distinctBy(a => a % 2) // List(1, 2)
```


# Collection implicits

**Warning**: these are deprecated.

Use type-specific `...Extension` instead, for example, `za.co.absa.commons.lang.extensions.IteratorExtension`.

```scala
import CollectionImplicits._

val iter: Iterator[_] = ???
val arr: Array[_] = ???

// copy 42 items from the `iter` to the `arr` with array offset 7
iter.fetchToArray(arr, 7, 42) // returns a number of actually copied items
```

```scala
import CollectionImplicits._

// Get distinct elements by only comparing certain property(-es)
val xs = Seq(
   Foo(x = 1, ...), // A
   Foo(x = 2, ...), // B
   Foo(x = 1, ...), // C
   Foo(x = 2, ...), // D
   Foo(x = 3, ...), // E
)
xs.distinctBy(_.x) // returns elements A, B, E
```

# Graph Utils

### Topological sorting (DAG only)
```scala
val myNodes: Seq[MyNode] = ??? // an arbitrary sequence of objects that can represent graph nodes 

// import extension methods
import za.co.absa.commons.graph.GraphImplicits._

val sortedNodes = myNodes.sortedTopologicallyBy(_.id, _.refIds) // arguments are functions that return a self ID and outbound IDs for every node in the collection 

// ... or using implicit `DAGNodeIDMapping` instance instead of explicitly passing mapping functions as arguments

implicit object MyNodeIdMapping extends DAGNodeIdMapping[MyNode, NodeId] {
   override def selfId(n: MyNode): NodeId = ???
   override def refIds(n: MyNode): Traversable[NodeId] = ???
}

val sortedNodes = myNodes.sortedTopologically()
```

# Abstract Converters
A simple stackable `Converter` trait with a simple memoized wrapper.
### Usage 
```scala
// 1. Define your converter
class AlchemicalConverter extends Converter {
  override type From = Lead
  override type To   = Gold
  override def convert(arg: Lead): Gold = ??? // treat with dragon eyes and cosmic rays
}

// 2. Instantiate it
val forge = new AlchemicalConverter
// or a memoized variant
val forge = new AlchemicalConverter with CachingConverter

// 3. Use it
forge.convert(... some lead ...) // returns some gold
```

# Type constraints
Utility object that defines extended type constraints to be used in Scala type definitions.
In particular it defines a `not` type constraint.
### Usage
```scala
trait VegetarianMenu {
  def add[A <: Food : not[Meat]#λ](food: A)
}
```

# Option implicits
**Warning**: these are deprecated.

Use type-specific `...Extension` instead, for example, `za.co.absa.commons.lang.extensions.StringExtension`.

```scala
// Strings
(null: String).nonBlankOption // == None
"  \t \n \r  ".nonBlankOption // == None
"foo bar".nonBlankOption // == Some("foo bar")

// Collections
Seq.empty.asOption // == None
Seq(1, 2).asOption // == Some(Seq(1, 2))

// Just another way of doing Option(foo)
foo.asOption

// Optionally call a method in a chain
new MyBuilder
  .withX(42)
  .withY(77)
  .optionally(_.withZ, maybeZ) // <---- withZ is called with a `value` if `maybeZ` is `Some(value)`
  .optionally(_.withABC(a, _, c), maybeB) // <---- it also works with n-ary methods

// Alternatively `having()` method can be used. It does the same thing as `optionally()`,
// but because of re-arranged method parameters it's easier for the compiler to infer types.
// See: https://github.com/AbsaOSS/commons/issues/56
new MyBuilder
  .withX(42)
  .withY(77)
  .having(maybeZ)(_.withZ)
  ...
```

# UrisConnectionStringParser
Parses a connection string containing one or multiple URIs into a list of strings (each being one URI).
Input URIs are supposed to have semi-colon-separated base URIs, and each can have multiple comma-separated hosts.
```scala
val connectionString = "https://localhost:8080,host2:8080/rest_api;http://localhost:9000/rest_api"

UrisConnectionStringParser.parse(connectionString)
// List("https://localhost:8080/rest_api", "https://host2:8080/rest_api", "http://localhost:9000/rest_api")
```

# Commons Configuration
### Implicits
Useful methods for `org.apache.commons.configuration.Configuration`.

```scala
import za.co.absa.commons.config.ConfigurationImplicits._

// return value or throw
conf.getRequiredInt("property.key")

// return Some(value) or None
conf.getOptionalInt("property.key")

// return Map("conf.key1" -> 123, "conf.key2" -> 456)
conf.toMap[Int]

```
Available for String, Array[String], Boolean, BigDecimal, Byte, Short, Int, Float, Long and Double.

### Configuration sub-classes

##### UpperSnakeCaseEnvironmentConfiguration
This is an extension of `EnvironmentConfiguration` that converts key names between 
dot-separated _camelCase_ notation and _UPPER_SNAKE_CASE_ notation which is common for naming environment variables.

See: https://github.com/AbsaOSS/commons/issues/54
```scala
// Any of the following calls returns a value of FOO_BAR_BAZ environment variable
(new UpperSnakeCaseEnvironmentConfiguration).getString("foo.bar.baz")
(new UpperSnakeCaseEnvironmentConfiguration).getString("fooBarBaz")
(new UpperSnakeCaseEnvironmentConfiguration).getString("foo.barBaz")
```

### Typed Configuration

Trait `ConfTyped` provides a DSL for creating a typed hierarchical configuration object.
It provides access to two main abstractions: `Conf` and `Prop`

Example:

```scala
import za.co.absa.commons.config._

object MyAppConfig extends ConfTyped {
   val confSource: java.util.Properties = ???

   override val rootPrefix = "com.example"

   object Foo extends Conf("foo") {
      object Bar extends Conf("bar") {
         val baz: String = confSource getProperty Prop("baz")
         val qux: String = confSource getProperty Prop("qux")
      }
   }
}

// somewhere in your application

import MyAppConfig._

val baz = Foo.Bar.baz // mapped to the key "com.example.foo.bar.baz" in the <code>confSource</code>
val baz = Foo.Bar.qux // mapped to the key "com.example.foo.bar.qux" in the <code>confSource</code>
```

Note that `ConfTyped` doesn't impose or depend on the way how the configuration values are loaded.
It only provides a convenient way to implicitly construct the configuration key names from the nested object structure.

The key names are obtained by calling `Prop("...")` method.
It returns a full property key name that reflects the nesting structure of the `Conf` instances' names,
concatenated with dot (`.`) and prefixed with the `rootPrefix` if one is provided.

Another example of usage `ConfTyped`:
```scala
val props = new java.util.Properties with ConfTyped {
   val foo = new Conf("foo") {
      val bar = new Conf("bar") {
         lazy val baz = getProperty(Prop("baz"))
      }
   }
}

props.put("foo.bar.baz", "42")

println(props.foo.bar.baz) // prints 42
```

# Reflection Utils
### Basics
##### Get direct sub-types of a sealed type
```scala
ReflectionUtils.directSubClassesOf[Food] // == Seq(classOf[Vegetables], classOf[Meat], classOf[Fish])
```
##### Get `object` instances extending a sealed type
```scala
ReflectionUtils.objectsOf[Currency] // == Seq(classOf[EUR], classOf[USD], classOf[CZK])
```
##### Get `object` instance by it's full type name (similar to `Class.forName(...)`, but for objects)
```scala
ReflectionUtils.objectForName[MySingleton]("com.example.MySingleton") // == MySingleton
```
##### `objectForName` with more descriptive exception message in case there is something wrong with provided name.
```scala
ReflectionUtils.objectForNameWithDescriptiveException[MySingleton]("com.example.MySingleton") // == MySingleton
```
##### Get private field value of an arbitrary class. (a typed variant of `field.get(o).asInstanceOf[T]`)
```scala
ReflectionUtils.extractFieldValue[Int](foo, "bar")
// or if you know a type where the field is declared
ReflectionUtils.extractFieldValue[Doh, Int](foo, "bar")
```
##### Extract object properties as a key-value map
```scala
case class Person(name: String, age: Int, sex: Sex)
val aPerson = Person("Alex", 41, Male)

ReflectionUtils.extractProperties(aPerson) // == Map("name" -> "Alex, "age" -> 42, "sex" -> Male)
```
##### Extract a case class argument default value (if exists)
```scala
case class Button(title: String, isPressed = false)
ReflectionUtils.caseClassCtorArgDefaultValue[Int](classOf[Button], "name") // == None
ReflectionUtils.caseClassCtorArgDefaultValue[Int](classOf[Button], "isPressed") // == Some(false)
```

##### Get all interfaces/traits of a given class including inherited ones
```scala
ReflectionUtils.ReflectionUtils.allInterfacesOf[A]
// or
ReflectionUtils.ReflectionUtils.allInterfacesOf(aClass)
```

### Enumeration macros
##### Obtain all instances of a sealed trait
Can be used to e.g. in a _Case Object Enumeration_ pattern.

A similar solution and the motivation is well describes in the
[Scala Enumerations hell](https://medium.com/@yuriigorbylov/scala-enumerations-hell-5bdba2c1216) article.
But unlike the above approach `EnumerationMacros.sealedInstancesOf` utilizes Scala compiler macros,
so that the instances are lookup at the compile time.  

```scala
  sealed trait Color
  
  object Color {
    // returns Set(Red, Green, Blue)
    val values: Set[Color] = EnumerationMacros.sealedInstancesOf[Color]

    case object Red extends Color
    case object Green extends Color
    case object Blue extends Color
  }
``` 

### Run-time compilation
If you wants some code to be linked and executed at run-time, here's a simple way to do it:
```scala
val fn = ReflectionUtils.compile(q""" some scala code """)
fn()
```
Of with input parameters:
```scala
val fn = ReflectionUtils.compile(q"""
  val foo = arg("foo")
  val bar = arg("bar")
  foo + bar
  """)
fn(Map("foo" -> ..., "bar" -> ...))
```

### Run-time value extractors
Sometimes you need to support different versions of some library with a breaking changes in API.
If there aren't too many breaking changes, or you only use a certain subset of an API then creating a proper adapter layer could be an overkill.
In that case simple run-time evaluation could be a decent alternative:

##### Getting a value of from an accessor by name
```scala
object FilenameExtractor extends AccessorMethodValueExtractor[String]("filename", "name", "file")

val FilenameExtractor(fileName) = someObjectRepresentingAFile
// The first matching accessor name with type wins
```

##### Safely matching on a type that might be missing at run-time.
If you try to pattern-match on a type that is missing from the classpath at runtime (e.g. optional dependency) you'll get `NoClassDefFoundError`. Though it looks strange as from the use case perspective if there is no `Foo` class there couldn't be a `Foo` instance. Logically one would expect it to just not match, but in fact it throws.
```scala
aObject match {
  ...
  case foo: Foo => // <----- this could throw NoClassDefFoundError !!
  ...
}
```
To get a desired behavior you can use `SafeTypeMatchingExtractor`:
```scala
object FooExtractor extends SafeTypeMatchingExtractor("com.example.Foo")

aObject {
  ...
 case FooExtractor(foo) => // do something with `foo`
  ...
}
```
or you can even make a fancy DSL from it:
```scala
object `_: Foo` extends SafeTypeMatchingExtractor("com.example.Foo")

aObject {
  ...
  case `_: Foo`(foo) => // do something with `foo`
  ...
}
```


# A project Build Info Utils
A singleton that parses `build.properties` from the classpath and return version and timestamp as constants.
### Usage
Copy `build.properties.template` file and paste in into your classpath root as `build.properties`. Make sure the resource filtering is enabled on your project build.
Then you can access it's content as simply as this:
```scala
BuildInfo.Version // returns `build.version` property from the `build.properties` file
BuildInfo.Timestamp // returns `build.timestamp` property from the `build.properties` file
BuildInfo.BuildProps // returns entire `build.properties` content as immutable Java `Properties`
```
### Other ways of usage & customization
If needed, you can customize a `build.properties` resource path and/or the property mapping.

##### Custom `.properties` file
```scala
// loads '/foo/bar.properties' from the classpath
object MyBuildInfo extends BuildInfo(resourcePrefix = "/foo/bar")
```
##### Custom property mapping
```scala
object MyBuildInfo extends BuildInfo(propMapping = PropMapping(
  version = "bld.ver",  // binds "Version" field to "bld.ver" property
  timestamp = "bld.ttt" // binds "Timestamp" field to "bld.ttt" property
))
```

You can also use `apply()` method instead of inheritance. It all depends on your preferred code style:

```scala
val myBuildInfo = BuildInfo(...)
```

# Error handling utils

### Client/Server error cross-linking
In client-server application the errors sent to a client is often sanitised for privacy and security reasons.
This however complicates troubleshooting because it's difficult to find a much between a client error message
and the corresponding exception details in the server logs.

One way to solve this issue is to generate a unique identifier that is then incorporated into
the server log on one hand, and is sent to the client along with a client friendly error message on the other hand.
Such unique ID will be easy to lookup in logs, and will precisely identify the root cause of the error seen by the client.

```scala
// somewhere on the server
try {
  service.doSomething()
} catch {
  case NonFatal(e) =>
    import za.co.absa.commons.error
    val errorRef = ErrorRef(e, "oops!")   
    clientResponse.sendError(errorRef)
}
```
This way the exception `e` is silently logged into the server logs with the message
```
[ERROR] ... ERROR_ID [123e4567-e89b-12d3-a456-426614174000] oops!
            caused by: NullPointerException in ...
            <stack trace>
```
... while the client receives a serialized representation that only contains the error UUID, timestamp and the message "oops!".

For example:
```json
{
   "errorId": "123e4567-e89b-12d3-a456-426614174000",
   "timestamp": 1611945666787,
   "message": "oops!"
}
```

# IO Utils

## Temporary file/directory
An easy way to create a temporary file or directory with the support for automatic recursive deletion (as `rm -rf`) on JVM shutdown.
### Usage
```scala
val myTmpFile: Path = TempFile.deleteOnExit.path
val myTmpDir: Path = TempDirectory.deleteOnExit.path

val myTmpFile: String = TempFile.deleteOnExit.asString
val myTmpDir: String = TempDirectory.deleteOnExit.asSTring

val myTmpFile: URI = TempFile.deleteOnExit.toURI
val myTmpDir: URI = TempDirectory.deleteOnExit.toURI
```
It also mimics Java IO API for a similar purpose
```scala
TempFile("myPrefix", "mySuffix")
```

## LocalFileSystemUtils
An object containing useful functions that operate on local file system.
### Usage
```scala
val doesExist = LocalFileSystemUtils.localExists("/user/u1/somefile") // true if this file exists, false otherwise

if(doesExist) {
  val fileContent = LocalFileSystemUtils.readLocalFile("/user/u1/somefile") // full file as string
}

val tildeReplaced = LocalFileSystemUtils.replaceHome("~/Projects/somedir") // path with replaces tilde with home directory path
```

# JSON (Json4s) Utils
A set of stackable traits, serving a wrapper around the way how Json4s (de)serializers are created. Instead of relying on implicit `Formats` objects a stackable traits are used.
This API is also binary compatible to Json4s 3.2 and 3.3+ versions (Jackson and Native)
### Usage
There are two default SerDe implementation that you can use out of the box:
  - `DefaultJacksonJsonSerDe`
  - `DefaultNativeJsonSerDe`

```scala
class MyApp extends App with DefaultJacksonJsonSerDe {
  FooBar.toJson // returns JSON string
  FooBar.toPrettyJson // returns formatted JSON string
  "{...}".fromJson[FooBar] // returns a FooBar instance
}
```
Or you can create a singleton and use that instead:
```scala
object JsonSerDe extends DefaultNativeJsonSerDe
import JsonSerDe._
fooBar.toJson
```
If you want another parser impl, then you do this:
```scala
object MyJsonSerDe 
  extends AbstractJsonSerDe[MyJson]
  with my.JsonMethods
  with DefaultFormatsBuilder

import MyJsonSerDe._
fooBar.toJson
```
If you want custom formats then instead of mixing in `DefaultFormatsBuilder` simply override `def formats` method.

# Version Utils
A simple utility that parses version strings.
It supports SemVer 2.0 as well as a simple dot-separated version format.
Can be used to compare the versions, for instance when implementing version predicates.

### Example

```scala
import Version._

require(Version.asSimple(SPARK_VERSION) > ver"2.4")
// or
require(Version.asSemVer(SomeLibVersion) > semver"1.2.3-beta.2")
```

To get a string representation from a `Version` instance `asString` extension method can be used:

```scala
val myVer: Version = semver"1.2.3-beta.2+777.42"
myVer.asString  // returns "1.2.3-beta.2+777.42"
```  

Semantic Versioning specific operations:

```scala
import Version._

val myVer = semver"111.222.333-alpha.444+build.555"

myVer.major      == 111
myVer.minor      == 222
myVer.patch      == 333
myVer.core       == semver"111.222.333"
myVer.preRelease == ver"alpha.444"
myVer.buildMeta  == ver"build.555"
```

# Scalatest Utils

1. `ConditionalTestTags` - runs certain tests conditionally
    ```scala
      it should "test that new Spark feature" taggedAs ignoreIf(ver"$SPARK_VERSION" < ver"2.4") in  {
        ...
      }
    
      it should "test some DAO" taggedAs ignoreIf(!isDatabaseAvailable) in  {
        ...
      }
    ```

2. `ConsoleStubs` - stubs console IO
    ```scala
      captureStdOut(Console.out.print("foo")) should be("foo")
    ```

3. `SystemExitFixture` - intercepts `System.exit()` and asserts status
    ```scala
      captureExitStatus(System.exit(42)) should be(42)
    
      // OR
    
      assertingExitStatus(be > 0 and be < 5) {
          // run some code that calls System.exit(...)
      }
    ```
   
4. `EnvFixture` - adds an API to set an environment variable for the scope of a single test method
    ```scala
    class MySpec ... with EnvFixture {
      it should "set FOO variable for this test body only" in {
        setEnv("FOO", 42)
        // execute some test code that reads FOO environment variable
        // via the standard Java API like System.getenv("FOO")
      }
    }
    ```

5. `WhitespaceNormalizations` - extends Scalatest DSL with some whitespace treatment methods
    ```scala
      (
        """
          {
            a: 111,
            b: {
              v: 42
            }
          }
        """
        should equal ("{ a: 111, b: { v: 42 } }")
        (after being trimmed and whitespaceNormalized)
      )
    ```

6. `CommonMatchers` - provides matchers, currently only URI matcher
    ```scala
      class MySpec ... with CommonMatchers {
        it should "produce the correct URI" in {
          val uri: String = ???
   
          uri should equalToUri("file:///foo.txt")
          // compares using java.net.URI`s equals method
        }
      }
    ```

# S3 Utils

### S3 Location Utils
Provides simple means of checking a string to appear to be a valid S3 Location and parsing it into a `S3Location`.
That way, one can easily obtain the `protocol`, `bucketName`, and `path`.
- recognized `protocol`s are `s3`, `s3n` and `s3a`
- `bucketName` is checked according to the
  [official naming rules](https://docs.aws.amazon.com/AmazonS3/latest/dev/BucketRestrictions.html#bucketnamingrules)
  (aphanum chars / `.` / `-`, min length: 3, max length: 63)
- `path` content is not checked in any way

```scala
import za.co.absa.commons.s3._
import za.co.absa.commons.s3.SimpleS3Location._

"s3a://mybucket.some.where/my/path1".isValidS3Path // yields true

val s3loc: S3Location = "s3://mybucket-123/path/to/file.ext".toSimpleS3Location.get
s3loc.protocol // holds "s3"
s3loc.bucketName // holds "mybucket-123"
s3loc.path // holds "path/to/file.ext"

"s3x://bogus#$%/xxx".toSimpleS3Location // yields None
```


---

    Copyright 2019 ABSA Group Limited
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
