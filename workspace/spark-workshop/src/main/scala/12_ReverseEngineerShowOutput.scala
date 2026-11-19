/* 
Write a structured query that loads a text file that contains the output of Dataset.show operator.
That is, convert the plain text output of show() to a Dataframe.
*/

package workshop.sql

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object ReverseEngineerShowOutput {

    def main(args: Array[String]): Unit = {

        val spark = SparkSession.builder()
            .appName("ReverseEngineerShowOutput")
            .getOrCreate()

        import spark.implicits._

        val input = spark.read
        .options(Map.apply(
            "comment" -> "+",
            "delimiter" -> "|",
            "header" -> "true"
        ))
        .csv("/data/12.txt")
        
        input
        .drop("_c0")
        .drop(input.schema.last.name)
        .show(truncate=false)

        spark.stop()
    }
}