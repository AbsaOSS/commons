package za.co.absa.commons.spark.sql

import java.io.ByteArrayOutputStream

import org.apache.spark.sql.DataFrame

object DataFrameImplicits {
  implicit class DataFrameEnhancements(val df: DataFrame) {

    private def gatherData(showFnc: () => Unit): String = {
      val outCapture = new ByteArrayOutputStream
      Console.withOut(outCapture) {
        showFnc()
      }
      val dfData = new String(outCapture.toByteArray).replace("\r\n", "\n")
      dfData
    }

    def dataAsString(): String = {
      val showFnc: () => Unit = df.show
      gatherData(showFnc)
    }

    def dataAsString(truncate: Boolean): String = {
      val showFnc:  () => Unit = ()=>{df.show(truncate)}
      gatherData(showFnc)
    }

    def dataAsString(numRows: Int, truncate: Boolean): String = {
      val showFnc: ()=>Unit = () => df.show(numRows, truncate)
      gatherData(showFnc)
    }

    def dataAsString(numRows: Int, truncate: Int): String = {
      val showFnc: ()=>Unit = () => df.show(numRows, truncate)
      gatherData(showFnc)
    }

    def dataAsString(numRows: Int, truncate: Int, vertical: Boolean): String = {
      val showFnc: ()=>Unit = () => df.show(numRows, truncate, vertical)
      gatherData(showFnc)
    }

  }

}
