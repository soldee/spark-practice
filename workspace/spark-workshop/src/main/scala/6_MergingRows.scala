/* 
Write a structured query that “merges” two rows of the same id (to replace nulls).

input:
    +---+-----+----+--------+
    | id| name| age|    city|
    +---+-----+----+--------+
    |100| John|  35|    null|
    |100| John|null| Georgia|
    |101| Mike|  25|    null|
    |101| Mike|null|New York|
    |103| Mary|  22|    null|
    |103| Mary|null|   Texas|
    |104|Smith|  25|    null|
    |105| Jake|null| Florida|
    +---+-----+----+--------+
Solution:
    +---+-----+----+--------+
    |id |name |age |city    |
    +---+-----+----+--------+
    |100|John |35  |Georgia |
    |101|Mike |25  |New York|
    |103|Mary |22  |Texas   |
    |104|Smith|25  |null    |
    |105|Jake |null|Florida |
    +---+-----+----+--------+
*/

package workshop.sql

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object MergingRows {

    def main(args: Array[String]): Unit = {

        val spark = SparkSession.builder()
            .appName("MergingRows")
            .getOrCreate()

        import spark.implicits._
        
        val input = Seq(
        ("100","John", Some(35),None),
        ("100","John", None,Some("Georgia")),
        ("101","Mike", Some(25),None),
        ("101","Mike", None,Some("New York")),
        ("103","Mary", Some(22),None),
        ("103","Mary", None,Some("Texas")),
        ("104","Smith", Some(25),None),
        ("105","Jake", None,Some("Florida"))).toDF("id", "name", "age", "city")

        input.groupBy("id").agg(
            any_value($"name", ignoreNulls=lit(true)).as("name"),
            any_value($"age", ignoreNulls=lit(true)).as("age"),
            any_value($"city", ignoreNulls=lit(true)).as("city")
        ).show()

        // In reality, we might want to deal with multiple possible options of the same value instead of choosing a random one with any_value
        // So we could collect_set and flag those that contain multiple values or none
        val collectDf = input.groupBy("id").agg(
            collect_set($"name").as("name"),
            collect_set($"age").as("age"),
            collect_set($"city").as("city")
        )
        collectDf.withColumn("age_flagDQ", 
            when(array_size($"age") > 1, lit("MULTIPLE_VALUES"))
            .when(array_size($"age") === 0, lit("NO_VALUE"))
        )
        .withColumn("city_flagDQ", 
            when(array_size($"city") > 1, lit("MULTIPLE_VALUES"))
            .when(array_size($"city") === 0, lit("NO_VALUE"))
        ).show()
    }
}