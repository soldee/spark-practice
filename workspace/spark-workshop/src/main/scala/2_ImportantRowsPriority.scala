/*
Write a structured query that selects the most important rows per assigned priority.
*/

package workshop.sql

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object ImportantRowsPriority {
    def main(args: Array[String]): Unit = {

        val spark = SparkSession.builder()
            .appName("ImportantRowsPriority")
            .getOrCreate()

        import spark.implicits._

        val input = Seq(
            (1, "MV1"),
            (1, "MV2"),
            (2, "VPV"),
            (2, "Others")
        ).toDF("id", "value")

        input.groupBy("id").agg(first("value")).show()
    }
}