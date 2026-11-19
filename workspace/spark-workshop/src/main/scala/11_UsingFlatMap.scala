/* 
Write a query that creates as many rows as the number of elements in a given array column. 
The values of the new rows should be the elements of the array column themselves.

input:
+---------+
|     nums|
+---------+
|[1, 2, 3]|
+---------+

result:
+---------+---+
|     nums|num|
+---------+---+
|[1, 2, 3]|  1|
|[1, 2, 3]|  2|
|[1, 2, 3]|  3|
+---------+---+
*/

package workshop.sql

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object UsingFlatMap {

    def main(args: Array[String]): Unit = {

        val spark = SparkSession.builder()
            .appName("UsingFlatMap")
            .getOrCreate()

        import spark.implicits._

        val nums = Seq(Seq(1,2,3)).toDF("nums")

        nums.flatMap(row => {
            val nums = row.getSeq[Int](0)
            nums.map(n => (n, nums))
        })
        .show(truncate=false)

        // And without flatmap
        nums
        .withColumn("num", explode($"nums"))
        .show(truncate=false)

        spark.stop()
    }
}