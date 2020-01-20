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

import org.apache.spark.sql.types._
import org.scalatest.flatspec.AnyFlatSpec
import za.co.absa.commons.spark.SchemaUtils._
import org.scalatest.matchers.should.Matchers

class SchemaUtilsSpec extends AnyFlatSpec with Matchers{
  // scalastyle:off magic.number

  private val schema = StructType(Seq(
    StructField("a", IntegerType, nullable = false),
    StructField("b", StructType(Seq(
      StructField("c", IntegerType),
      StructField("d", StructType(Seq(
        StructField("e", IntegerType))), nullable = true)))),
    StructField("f", StructType(Seq(
      StructField("g", ArrayType.apply(StructType(Seq(
        StructField("h", IntegerType))))))))))

  private val nestedSchema = StructType(Seq(
    StructField("a", IntegerType),
    StructField("b", ArrayType(StructType(Seq(
      StructField("c", StructType(Seq(
        StructField("d", ArrayType(StructType(Seq(
          StructField("e", IntegerType))))))))))))))

  private val arrayOfArraysSchema = StructType(Seq(
    StructField("a", ArrayType(ArrayType(IntegerType)), nullable = false),
    StructField("b", ArrayType(ArrayType(StructType(Seq(
      StructField("c", StringType, nullable = false)
    ))
    )), nullable = true)
  ))

  private val structFieldNoMetadata = StructField("a", IntegerType)

  private val structFieldWithMetadataNotSourceColumn = StructField("a", IntegerType, nullable = false, new MetadataBuilder().putString("meta", "data").build)
  private val structFieldWithMetadataSourceColumn = StructField("a", IntegerType, nullable = false, new MetadataBuilder().putString("sourcecolumn", "override_a").build)

  it should "Testing getFieldType" in {

    val a = getFieldType("a", schema)
    val b = getFieldType("b", schema)
    val c = getFieldType("b.c", schema)
    val d = getFieldType("b.d", schema)
    val e = getFieldType("b.d.e", schema)
    val f = getFieldType("f", schema)
    val g = getFieldType("f.g", schema)
    val h = getFieldType("f.g.h", schema)

    a.get.isInstanceOf[IntegerType] should be(true)
    b.get.isInstanceOf[StructType] should be(true)
    c.get.isInstanceOf[IntegerType] should be(true)
    d.get.isInstanceOf[StructType] should be(true)
    e.get.isInstanceOf[IntegerType] should be(true)
    f.get.isInstanceOf[StructType] should be(true)
    g.get.isInstanceOf[ArrayType] should be(true)
    h.get.isInstanceOf[IntegerType] should be(true)

    getFieldType("z", schema) should be (empty)
    getFieldType("x.y.z", schema) should be (empty)
    getFieldType("f.g.h.a", schema) should be (empty)
  }

  it should "Testing fieldExists" in {
    fieldExists("a", schema) should be (true)
    fieldExists("b", schema) should be (true)
    fieldExists("b.c", schema) should be (true)
    fieldExists("b.d", schema) should be (true)
    fieldExists("b.d.e", schema) should be (true)
    fieldExists("f", schema) should be (true)
    fieldExists("f.g", schema) should be (true)
    fieldExists("f.g.h", schema) should be (true)
    fieldExists("z", schema) should be (false)
    fieldExists("x.y.z", schema) should be (false)
    fieldExists("f.g.h.a", schema) should be (false)
  }

  it should "Test isColumnArrayOfStruct" in {
    isColumnArrayOfStruct("a", schema) should be (false)
    isColumnArrayOfStruct("b", schema) should be (false)
    isColumnArrayOfStruct("b.c", schema) should be (false)
    isColumnArrayOfStruct("b.d", schema) should be (false)
    isColumnArrayOfStruct("b.d.e", schema) should be (false)
    isColumnArrayOfStruct("f", schema) should be (false)
    isColumnArrayOfStruct("f.g", schema) should be (true)
    isColumnArrayOfStruct("f.g.h", schema) should be (false)
    isColumnArrayOfStruct("a", nestedSchema) should be (false)
    isColumnArrayOfStruct("b", nestedSchema) should be (true)
    isColumnArrayOfStruct("b.c.d", nestedSchema) should be (true)
  }

  it should "getRenamesInSchema - no renames" in {
    val result = getRenamesInSchema(StructType(Seq(
      structFieldNoMetadata,
      structFieldWithMetadataNotSourceColumn)))
    result should be (empty)
  }

  it should "getRenamesInSchema - simple rename" in {
    val result = getRenamesInSchema(StructType(Seq(structFieldWithMetadataSourceColumn)))
    result should equal (Map("a" -> "override_a"))
  }

  it should "getRenamesInSchema - complex with includeIfPredecessorChanged set" in {
    val sub = StructType(Seq(
      StructField("d", IntegerType, nullable = false, new MetadataBuilder().putString("sourcecolumn", "o").build),
      StructField("e", IntegerType, nullable = false, new MetadataBuilder().putString("sourcecolumn", "e").build),
      StructField("f", IntegerType)
    ))
    val schema = StructType(Seq(
      StructField("a", sub, nullable = false, new MetadataBuilder().putString("sourcecolumn", "x").build),
      StructField("b", sub, nullable = false, new MetadataBuilder().putString("sourcecolumn", "b").build),
      StructField("c", sub)
    ))

    val includeIfPredecessorChanged = true
    val result = getRenamesInSchema(schema, includeIfPredecessorChanged)
    val expected = Map(
      "a"   -> "x"  ,
      "a.d" -> "x.o",
      "a.e" -> "x.e",
      "a.f" -> "x.f",
      "b.d" -> "b.o",
      "c.d" -> "c.o"
    )

    result should equal (expected)
  }

  it should "getRenamesInSchema - complex with includeIfPredecessorChanged not set" in {
    val sub = StructType(Seq(
      StructField("d", IntegerType, nullable = false, new MetadataBuilder().putString("sourcecolumn", "o").build),
      StructField("e", IntegerType, nullable = false, new MetadataBuilder().putString("sourcecolumn", "e").build),
      StructField("f", IntegerType)
    ))
    val schema = StructType(Seq(
      StructField("a", sub, nullable = false, new MetadataBuilder().putString("sourcecolumn", "x").build),
      StructField("b", sub, nullable = false, new MetadataBuilder().putString("sourcecolumn", "b").build),
      StructField("c", sub)
    ))

    val includeIfPredecessorChanged = false
    val result = getRenamesInSchema(schema, includeIfPredecessorChanged)
    val expected = Map(
      "a"   -> "x",
      "a.d" -> "x.o",
      "b.d" -> "b.o",
      "c.d" -> "c.o"
    )

    result should equal (expected)
  }


  it should "getRenamesInSchema - array" in {
    val sub = StructType(Seq(
      StructField("renamed", IntegerType, nullable = false, new MetadataBuilder().putString("sourcecolumn", "rename source").build),
      StructField("same", IntegerType, nullable = false, new MetadataBuilder().putString("sourcecolumn", "same").build),
      StructField("f", IntegerType)
    ))
    val schema = StructType(Seq(
      StructField("array1", ArrayType(sub)),
      StructField("array2", ArrayType(ArrayType(ArrayType(sub)))),
      StructField("array3", ArrayType(IntegerType), nullable = false, new MetadataBuilder().putString("sourcecolumn", "array source").build)
    ))

    val includeIfPredecessorChanged = false
    val result = getRenamesInSchema(schema, includeIfPredecessorChanged)
    val expected = Map(
      "array1.renamed" -> "array1.rename source",
      "array2.renamed" -> "array2.rename source",
      "array3"   -> "array source"
    )

    result should equal (expected)
  }


  it should "getRenamesInSchema - source column used multiple times" in {
    val sub = StructType(Seq(
      StructField("x", IntegerType, nullable = false, new MetadataBuilder().putString("sourcecolumn", "src").build),
      StructField("y", IntegerType, nullable = false, new MetadataBuilder().putString("sourcecolumn", "src").build)
    ))
    val schema = StructType(Seq(
      StructField("a", sub),
      StructField("b", IntegerType, nullable = false, new MetadataBuilder().putString("sourcecolumn", "src").build)
    ))

    val result = getRenamesInSchema(schema)
    val expected = Map(
      "a.x" -> "a.src",
      "a.y" -> "a.src",
      "b"   -> "src"
    )

    result should equal (expected)
  }

  it should "Testing getFirstArrayPath" in {
    getFirstArrayPath("f.g.h", schema) should equal ("f.g")
    getFirstArrayPath("f.g", schema) should equal ("f.g")
    getFirstArrayPath("z.x.y", schema) should equal ("")
    getFirstArrayPath("b.c.d.e", schema) should equal ("")
  }

  it should "Testing getAllArrayPaths" in {
    getAllArrayPaths(schema) should equal (Seq("f.g"))
    getAllArrayPaths(schema("b").dataType.asInstanceOf[StructType]) should equal (Seq())
  }

  it should "Testing getAllArraysInPath" in {
    getAllArraysInPath("b.c.d.e", nestedSchema) should equal (Seq("b", "b.c.d"))
  }

  it should "Testing getFieldNameOverriddenByMetadata" in {
    getFieldNameOverriddenByMetadata(structFieldNoMetadata) should equal ("a")
    getFieldNameOverriddenByMetadata(structFieldWithMetadataNotSourceColumn) should equal ("a")
    getFieldNameOverriddenByMetadata(structFieldWithMetadataSourceColumn) should equal ("override_a")
  }

  it should "Testing getFieldNullability" in {
    getFieldNullability("a", schema).get should be (false)
    getFieldNullability("b.d", schema).get should be (true)
    getFieldNullability("x.y.z", schema).isEmpty should be (true)
  }

  it should "Test isCastAlwaysSucceeds()" in {
    isCastAlwaysSucceeds(StructType(Seq()), StringType) should be (false)
    isCastAlwaysSucceeds(ArrayType(StringType), StringType) should be (false)
    isCastAlwaysSucceeds(StringType, ByteType) should be (false)
    isCastAlwaysSucceeds(StringType, ShortType) should be (false)
    isCastAlwaysSucceeds(StringType, IntegerType) should be (false)
    isCastAlwaysSucceeds(StringType, LongType) should be (false)
    isCastAlwaysSucceeds(StringType, DecimalType(10,10)) should be (false)
    isCastAlwaysSucceeds(StringType, DateType) should be (false)
    isCastAlwaysSucceeds(StringType, TimestampType) should be (false)
    isCastAlwaysSucceeds(StructType(Seq()), StructType(Seq())) should be (false)
    isCastAlwaysSucceeds(ArrayType(StringType), ArrayType(StringType)) should be (false)

    isCastAlwaysSucceeds(ShortType, ByteType) should be (false)
    isCastAlwaysSucceeds(IntegerType, ByteType) should be (false)
    isCastAlwaysSucceeds(IntegerType, ShortType) should be (false)
    isCastAlwaysSucceeds(LongType, ByteType) should be (false)
    isCastAlwaysSucceeds(LongType, ShortType) should be (false)
    isCastAlwaysSucceeds(LongType, IntegerType) should be (false)

    isCastAlwaysSucceeds(StringType, StringType) should be (true)
    isCastAlwaysSucceeds(ByteType, StringType) should be (true)
    isCastAlwaysSucceeds(ShortType, StringType) should be (true)
    isCastAlwaysSucceeds(IntegerType, StringType) should be (true)
    isCastAlwaysSucceeds(LongType, StringType) should be (true)
    isCastAlwaysSucceeds(DecimalType(10,10), StringType) should be (true)
    isCastAlwaysSucceeds(DateType, StringType) should be (true)
    isCastAlwaysSucceeds(TimestampType, StringType) should be (true)
    isCastAlwaysSucceeds(StringType, StringType) should be (true)

    isCastAlwaysSucceeds(ByteType, ByteType) should be (true)
    isCastAlwaysSucceeds(ByteType, ShortType) should be (true)
    isCastAlwaysSucceeds(ByteType, IntegerType) should be (true)
    isCastAlwaysSucceeds(ByteType, LongType) should be (true)
    isCastAlwaysSucceeds(ShortType, ShortType) should be (true)
    isCastAlwaysSucceeds(ShortType, IntegerType) should be (true)
    isCastAlwaysSucceeds(ShortType, LongType) should be (true)
    isCastAlwaysSucceeds(IntegerType, IntegerType) should be (true)
    isCastAlwaysSucceeds(IntegerType, LongType) should be (true)
    isCastAlwaysSucceeds(LongType, LongType) should be (true)
    isCastAlwaysSucceeds(DateType, TimestampType) should be (true)
  }

  it should "Test isCommonSubPath()" in {
    isCommonSubPath() should be (true)
    isCommonSubPath("a") should be (true)
    isCommonSubPath("a.b.c.d.e.f", "a.b.c.d", "a.b.c", "a.b", "a") should be (true)
    isCommonSubPath("a.b.c.d.e.f", "a.b.c.x", "a.b.c", "a.b", "a") should be (false)
  }

  it should "Test getDeepestCommonArrayPath() for a path without an array" in {
    val schema = StructType(Seq[StructField](
      StructField("a",
        StructType(Seq[StructField](
          StructField("b", StringType))
        ))))

    getDeepestCommonArrayPath(schema, Seq("a", "a.b")) should be (empty)
  }

  it should "Test getDeepestCommonArrayPath() for a path with a single array at top level" in {
    val schema = StructType(Seq[StructField](
      StructField("a", ArrayType(StructType(Seq[StructField](
        StructField("b", StringType)))
      ))))

    val deepestPath = getDeepestCommonArrayPath(schema, Seq("a", "a.b"))

    deepestPath should not be empty
    deepestPath.get should equal ("a")
  }

  it should "Test getDeepestCommonArrayPath() for a path with a single array at nested level" in {
    val schema = StructType(Seq[StructField](
      StructField("a", StructType(Seq[StructField](
        StructField("b", ArrayType(StringType))))
      )))

    val deepestPath = getDeepestCommonArrayPath(schema, Seq("a", "a.b"))

    deepestPath should not be empty
    deepestPath.get should equal ("a.b")
  }

  it should "Test getDeepestCommonArrayPath() for a path with several nested arrays of struct" in {
    val schema = StructType(Seq[StructField](
      StructField("a", ArrayType(StructType(Seq[StructField](
        StructField("b", StructType(Seq[StructField](
          StructField("c", ArrayType(StructType(Seq[StructField](
            StructField("d", StructType(Seq[StructField](
              StructField("e", StringType))
            )))
          ))))
        )))
      )))))

    val deepestPath = getDeepestCommonArrayPath(schema, Seq("a", "a.b", "a.b.c.d.e", "a.b.c.d"))

    deepestPath should not be empty
    deepestPath.get should equal ("a.b.c")
  }

  it should "Test getDeepestArrayPath() for a path without an array" in {
    val schema = StructType(Seq[StructField](
      StructField("a",
        StructType(Seq[StructField](
          StructField("b", StringType))
        ))))

    getDeepestArrayPath(schema, "a.b") should be (empty)
  }

  it should "Test getDeepestArrayPath() for a path with a single array at top level" in {
    val schema = StructType(Seq[StructField](
      StructField("a", ArrayType(StructType(Seq[StructField](
        StructField("b", StringType)))
      ))))

    val deepestPath = getDeepestArrayPath(schema, "a.b")

    deepestPath should not be empty
    deepestPath.get should equal ("a")
  }

  it should "Test getDeepestArrayPath() for a path with a single array at nested level" in {
    val schema = StructType(Seq[StructField](
      StructField("a", StructType(Seq[StructField](
        StructField("b", ArrayType(StringType))))
      )))

    val deepestPath = getDeepestArrayPath(schema, "a.b")
    val deepestPath2 = getDeepestArrayPath(schema, "a")

    deepestPath should not be empty
    deepestPath2 should be (empty)
    deepestPath.get should equal ("a.b")
  }

  it should "Test getDeepestArrayPath() for a path with several nested arrays of struct" in {
    val schema = StructType(Seq[StructField](
      StructField("a", ArrayType(StructType(Seq[StructField](
        StructField("b", StructType(Seq[StructField](
          StructField("c", ArrayType(StructType(Seq[StructField](
            StructField("d", StructType(Seq[StructField](
              StructField("e", StringType))
            )))
          ))))
        )))
      )))))

    val deepestPath = getDeepestArrayPath(schema, "a.b.c.d.e")

    deepestPath should not be empty
    deepestPath.get should equal ("a.b.c")
  }


  it should "Test getClosestUniqueName() is working properly" in {
    val schema = StructType(Seq[StructField](
      StructField("value", StringType)))

    // A column name that does not exist
    val name1 = SchemaUtils.getClosestUniqueName("v", schema)
    // A column that exists
    val name2 = SchemaUtils.getClosestUniqueName("value", schema)

    name1 should equal ("v")
    name2 should equal ("value_1")
  }

  it should "Test isOnlyField()" in {
    val schema = StructType(Seq[StructField](
      StructField("a", StringType),
      StructField("b", StructType(Seq[StructField](
        StructField("e", StringType),
        StructField("f", StringType)
      ))),
      StructField("c", StructType(Seq[StructField](
        StructField("d", StringType)
      )))
    ))

    isOnlyField(schema, "a") should be (false)
    isOnlyField(schema, "b.e") should be (false)
    isOnlyField(schema, "b.f") should be (false)
    isOnlyField(schema, "c.d") should be (true)
  }

  it should "Test getStructField on array of arrays" in {
    getField("a", arrayOfArraysSchema).contains(StructField("a",ArrayType(ArrayType(IntegerType)),nullable = false)) should be (true)
    getField("b", arrayOfArraysSchema).contains(StructField("b",ArrayType(ArrayType(StructType(Seq(StructField("c",StringType,nullable = false))))), nullable = true)) should be (true)
    getField("b.c", arrayOfArraysSchema).contains(StructField("c",StringType,nullable = false)) should be (true)
    getField("b.d", arrayOfArraysSchema).isEmpty should be (true)
  }

  it should "Test fieldExists" in {
    fieldExists("a", schema) should be (true)
    fieldExists("b", schema) should be (true)
    fieldExists("b.c", schema) should be (true)
    fieldExists("b.d", schema) should be (true)
    fieldExists("b.d.e", schema) should be (true)
    fieldExists("f", schema) should be (true)
    fieldExists("f.g", schema) should be (true)
    fieldExists("f.g.h", schema) should be (true)
    fieldExists("z", schema) should be (false)
    fieldExists("x.y.z", schema) should be (false)
    fieldExists("f.g.h.a", schema) should be (false)

    fieldExists("a", arrayOfArraysSchema) should be (true)
    fieldExists("b", arrayOfArraysSchema) should be (true)
    fieldExists("b.c", arrayOfArraysSchema) should be (true)
    fieldExists("b.d", arrayOfArraysSchema) should be (false)
  }

  it should "unpath - empty string remains empty" in {
    val result = unpath("")
    val expected = ""
    result should equal (expected)
  }

  it should "unpath - underscores get doubled" in {
    val result = unpath("one_two__three")
    val expected = "one__two____three"
    result should equal (expected)
  }

  it should "unpath - dot notation conversion" in {
    val result = unpath("grand_parent.parent.first_child")
    val expected = "grand__parent_parent_first__child"
    result should equal (expected)
  }
}

