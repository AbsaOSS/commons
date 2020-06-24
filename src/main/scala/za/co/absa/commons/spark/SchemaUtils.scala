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

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{ArrayType, DataType, StructField, StructType}
import org.apache.spark.sql.{Column, DataFrame}

object SchemaUtils {
  /**
   * Compares 2 array fields of a dataframe schema.
   *
   * @param array1 The first array to compare
   * @param array2 The second array to compare
   * @return true if provided arrays are the same ignoring nullability
   */
  @scala.annotation.tailrec
  private def equalArrayTypes(array1: ArrayType, array2: ArrayType): Boolean = {
    array1.elementType match {
      case arrayType1: ArrayType =>
        array2.elementType match {
          case arrayType2: ArrayType => equalArrayTypes(arrayType1, arrayType2)
          case _ => false
        }
      case structType1: StructType =>
        array2.elementType match {
          case structType2: StructType => equivalentSchemas(structType1, structType2)
          case _ => false
        }
      case _ => array1.elementType == array2.elementType
    }
  }

  /**
   * Finds all differences of two ArrayTypes and returns their paths
   *
   * @param array1 The first array to compare
   * @param array2 The second array to compare
   * @param parent Parent path. This is used for the accumulation of differences and their print out
   * @return Returns a Seq of found difference paths in scheme in the Array
   */
  @scala.annotation.tailrec
  private def diffArray(array1: ArrayType, array2: ArrayType, parent: String): Seq[String] = {
    array1.elementType match {
      case _ if array1.elementType.typeName != array2.elementType.typeName =>
        Seq(s"$parent data type doesn't match (${array1.elementType.typeName}) vs (${array2.elementType.typeName})")
      case arrayType1: ArrayType =>
        diffArray(arrayType1, array2.elementType.asInstanceOf[ArrayType], s"$parent")
      case structType1: StructType =>
        diffSchema(structType1, array2.elementType.asInstanceOf[StructType], s"$parent")
      case _ => Seq.empty[String]
    }
  }

  /**
   * Compares 2 fields of a dataframe schema.
   *
   * @param type1 The first field to compare
   * @param type2 The second field to compare
   * @return true if provided fields are the same ignoring nullability
   */
  private def equivalentTypes(type1: DataType, type2: DataType): Boolean = {
    type1 match {
      case arrayType1: ArrayType =>
        type2 match {
          case arrayType2: ArrayType => equalArrayTypes(arrayType1, arrayType2)
          case _ => false
        }
      case structType1: StructType =>
        type2 match {
          case structType2: StructType => equivalentSchemas(structType1, structType2)
          case _ => false
        }
      case _ => type1 == type2
    }
  }

  /**
   * Finds all differences of two StructFields and returns their paths
   *
   * @param field1 The first field to compare
   * @param field2 The second field to compare
   * @param parent Parent path. This is used for the accumulation of differences and their print out
   * @return Returns a Seq of found difference paths in scheme in the StructField
   */
  private def diffField(field1: StructField, field2: StructField, parent: String): Seq[String] = {
    field1.dataType match {
      case _ if field1.dataType.typeName != field2.dataType.typeName =>
        Seq(s"$parent.${field1.name} data type doesn't match (${field1.dataType.typeName}) vs (${field2.dataType.typeName})")
      case arrayType1: ArrayType =>
        diffArray(arrayType1, field2.dataType.asInstanceOf[ArrayType], s"$parent.${field1.name}")
      case structType1: StructType =>
        diffSchema(structType1, field2.dataType.asInstanceOf[StructType], s"$parent.${field1.name}")
      case _ =>
        Seq.empty[String]
    }
  }

  /**
   * Returns data selector that can be used to align schema of a data frame. You can use [[alignSchema]].
   *
   * @param schema Schema that serves as the model of column order
   * @return Sorted DF to conform to schema
   */
  def getDataFrameSelector(schema: StructType): List[Column] = {
    import za.co.absa.spark.hofs._

    def processArray(arrType: ArrayType, column: Column, name: String): Column = {
      arrType.elementType match {
        case arrType: ArrayType =>
          transform(column, x => processArray(arrType, x, name)).as(name)
        case nestedStructType: StructType =>
          transform(column, x => struct(processStruct(nestedStructType, Some(x)): _*)).as(name)
        case _ => column
      }
    }

    def processStruct(curSchema: StructType, parent: Option[Column]): List[Column] = {
      curSchema.foldRight(List.empty[Column])((field, acc) => {
        val currentCol: Column = parent match {
          case Some(x) => x.getField(field.name).as(field.name)
          case None => col(field.name)
        }
        field.dataType match {
          case arrType: ArrayType => processArray(arrType, currentCol, field.name) :: acc
          case structType: StructType => struct(processStruct(structType, Some(currentCol)): _*).as(field.name) :: acc
          case _ =>  currentCol :: acc
        }
      })
    }

    processStruct(schema, None)
  }

  /**
   * Using schema selector from [[getDataFrameSelector]] aligns the schema of a DataFrame to the selector for operations
   * where schema order might be important (e.g. hashing the whole rows and using except)
   *
   * @param df DataFrame to have it's schema aligned/sorted
   * @param structType model structType for the alignment of df
   * @return Returns aligned and filtered schema
   */
  def alignSchema(df: DataFrame, structType: StructType): DataFrame = df.select(getDataFrameSelector(structType): _*)

  /**
   * Using schema selector returned from [[getDataFrameSelector]] aligns the schema of a DataFrame to the selector
   * for operations where schema order might be important (e.g. hashing the whole rows and using except)
   *
   * @param df DataFrame to have it's schema aligned/sorted
   * @param selector model structType for the alignment of df
   * @return Returns aligned and filtered schema
   */
  def alignSchema(df: DataFrame, selector: List[Column]): DataFrame = df.select(selector: _*)

  /**
   * Compares 2 dataframe schemas.
   *
   * @param schema1 The first schema to compare
   * @param schema2 The second schema to compare
   * @return true if provided schemas are the same ignoring nullability
   */
  def equivalentSchemas(schema1: StructType, schema2: StructType): Boolean = {
    val fields1 = schema1.sortBy(_.name.toLowerCase)
    val fields2 = schema2.sortBy(_.name.toLowerCase)

    fields1.size == fields2.size &&
      fields1.zip(fields2).forall {
        case (f1, f2) =>
          f1.name.equalsIgnoreCase(f2.name) &&
            equivalentTypes(f1.dataType, f2.dataType)
      }
  }

  /**
   * Returns a list of differences in one schema to the other
   *
   * @param schema1 The first schema to compare
   * @param schema2 The second schema to compare
   * @param parent  Parent path. Should be left default by the users first run. This is used for the accumulation of
   *                differences and their print out.
   * @return Returns a Seq of paths to differences in schemas
   */
  def diffSchema(schema1: StructType, schema2: StructType, parent: String = ""): Seq[String] = {
    val fields1 = getMapOfFields(schema1)
    val fields2 = getMapOfFields(schema2)

    val diff = fields1.values.foldLeft(Seq.empty[String])((difference, field1) => {
      val field1NameLc = field1.name.toLowerCase()
      if (fields2.contains(field1NameLc)) {
        val field2 = fields2(field1NameLc)
        difference ++ diffField(field1, field2, parent)
      } else {
        difference ++ Seq(s"$parent.${field1.name} cannot be found in both schemas")
      }
    })

    diff.map(_.stripPrefix("."))
  }

  /**
   * Checks if the originalSchema is a subset of subsetSchema.
   *
   * @param subsetSchema The schema that needs to be extracted
   * @param originalSchema The schema that needs to have at least all t
   * @return true if provided schemas are the same ignoring nullability
   */
  def isSubset(subsetSchema: StructType, originalSchema: StructType): Boolean = {
    val subsetFields = getMapOfFields(subsetSchema)
    val originalFields = getMapOfFields(originalSchema)

    subsetFields.forall( subsetField =>
      originalFields.contains(subsetField._1) &&
        equivalentTypes(subsetField._2.dataType, originalFields(subsetField._1).dataType) )
  }

  private def getMapOfFields(schema: StructType): Map[String, StructField] = {
    schema.map(field => field.name.toLowerCase() -> field).toMap
  }
}
