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

# Collection utils

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

# Commons Configuration
### Implicits
Useful methods for `org.apache.commons.configuration.Configuration`.

```scala
import za.co.absa.commons.config.ConfigurationImplicits._

// return value or throw
conf.getRequiredInt("property.key")

// return Some(value) or None
conf.getOptionalInt("property.key")

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
An easy way to create a temporary file or directory with the support for automatic recursive deletion (as `rm -rf`) on JVM shutdown.
### Usage
```scala
val myTmpFile = TempFile.deleteOnExit.path
val myTmpDir = TempDirectory.deleteOnExit.path
```
It also mimics Java IO API for a similar purpose
```scala
TempFile("myPrefix", "mySuffix")
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

1. `ConsoleStubs` - stubs console IO
    ```scala
      captureStdOut(Console.out.print("foo")) should be("foo")
    ```

1. `SystemExitFixture` - intercepts `System.exit()` and asserts status
    ```scala
      captureExitStatus(System.exit(42)) should be(42)
    
      // OR
    
      assertingExitStatus(be > 0 and be < 5) {
          // run some code that calls System.exit(...)
      }
    ```
   
1. `EnvFixture` - adds an API to set an environment variable for the scope of a single test method
    ```scala
    class MySpec ... with EnvFixture {
      it should "set FOO variable for this test body only" in {
        setEnv("FOO", 42)
        // execute some test code that reads FOO environment variable
        // via the standard Java API like System.getenv("FOO")
      }
    }
    ```

1. `WhitespaceNormalizations` - extends Scalatest DSL with some whitespace treatment methods
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
   
# Spark Utils

### NonFatalQueryExecutionListenerAdapter

A trait that when is mixed with another `QueryExecutionListener` implementation, 
makes sure the later is not called with any fatal exception.   

See https://github.com/AbsaOSS/commons/issues/50

```scala
val myListener = new MyQueryExecutionListener with NonFatalQueryExecutionListenerAdapter

spark.listenerManager.register(myListener)
```

### Spark Schema Utils

Provides methods for working with schemas, its comparison and alignment.  

1. Schema comparison returning true/false. Ignores the order of columns
         
    ```scala
      SchemaUtils.equivalentSchemas(schema1, schema2)
    ```

2. Schema comparison returning difference. Ignores the order of columns
    
    ```scala
      SchemaUtils.diff(schema1, schema2)
    ```
   
3. Schema selector generator which provides a List of columns to be used in a 
select to order and positionally filter columns of a DataFrame
    
    ```scala
      SchemaUtils.getDataFrameSelector(schema)
    ```
   
4. Dataframe alignment method using the `getDataFrameSelector` method.
    
    ```scala
      SchemaUtils.alignSchema(dataFrameToBeAligned, modelSchema)
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
