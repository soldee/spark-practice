/* 
Write a structured query that limits collect_set standard function.
*/

package workshop.sql

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window

object CollectSetLimit {
    def main(args: Array[String]): Unit = {

        val spark = SparkSession.builder()
            .appName("CollectSetLimit")
            .getOrCreate()

        import spark.implicits._

        val input = spark.range(50).withColumn("key", $"id" % 5)

        input.groupBy("key").agg(
            slice(
                sort_array(collect_set("id"), asc=true), 
                start=1, length=3
            ).as("only_first_three")
        ).show()

    }
}