/*
Write a structured query that splits a column by using delimiters from another column.
EXTRA Write a structured query that removes empty tokens.

val dept = Seq(
  ("50000.0#0#0#", "#"),
  ("0@1000.0@", "@"),
  ("1$", "$"),
  ("1000.00^Test_string", "^")).toDF("VALUES", "Delimiter")
*/

import org.apache.spark.sql.functions._
import org.apache.spark.sql.SparkSession

object SplitFunctionDelimiter {
    def main(args: Array[String]): Unit = {

        val spark = SparkSession.builder()
            .appName("SplitFunctionDelimiter")
            .getOrCreate()

        import spark.implicits._

        val dept = Seq(
        ("50000.0#0#0#", "#"),
        ("0@1000.0@", "@"),
        ("1$", "$"),
        ("1000.00^Test_string", "^")).toDF("VALUES", "Delimiter")

        dept.select($"values", $"delimiter", split($"values", $"delimiter")).show()
    }
}