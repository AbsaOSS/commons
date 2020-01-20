package za.co.absa.commons.spark

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SparkUtilsSpec extends AnyFlatSpec with Matchers with SparkTestBase {
  it should "test a dataframe created from a JSON" in {
    val inputJson = Seq("""{"value":1}""", """{"value":2}""")

    val df = SparkUtils.getDataFrameFromJson(spark, inputJson)

    val expectedSchema = """root
                           | |-- value: long (nullable = true)
                           |""".stripMargin.replace("\r\n", "\n")
    val actualSchema = df.schema.treeString

    actualSchema should equal (expectedSchema)
  }
}
