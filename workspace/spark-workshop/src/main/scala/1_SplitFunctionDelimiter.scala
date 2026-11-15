/*
Write a structured query that splits a column by using delimiters from another column.
EXTRA Write a structured query that removes empty tokens.

val dept = Seq(
  ("50000.0#0#0#", "#"),
  ("0@1000.0@", "@"),
  ("1$", "$"),
  ("1000.00^Test_string", "^")).toDF("VALUES", "Delimiter")
*/
package workshop.sql

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

        dept.withColumn("split_values", split($"values", $"delimiter")).show()

        dept.createOrReplaceTempView("dept")

        val dept_split_df = spark.sql("select values, delimiter, split(values, delimiter) as split_values from dept")

        // EXTRA
        dept_split_df.withColumn("extra", array_remove($"split_values", "")).show()
    }
}