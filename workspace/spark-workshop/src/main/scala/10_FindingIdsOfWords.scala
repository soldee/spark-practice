/* 
Develop a standalone Spark SQL application (using IntelliJ IDEA) that finds the ids of the rows that have values of one column in an array column.

input:
    +---+------------------+-----+
    | id|             words| word|
    +---+------------------+-----+
    |  1|     one,two,three|  one|
    |  2|     four,one,five|  six|
    |  3|seven,nine,one,two|eight|
    |  4|    two,three,five| five|
    |  5|      six,five,one|seven|
    +---+------------------+-----+

result:
    +-----+------------+
    |    w|         ids|
    +-----+------------+
    | five|   [2, 4, 5]|
    |  one|[1, 2, 3, 5]|
    |seven|         [3]|
    |  six|         [5]|
    +-----+------------+
*/

package workshop.sql

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window

object FindingsIdsOfWords {

    def main(args: Array[String]): Unit = {

        val spark = SparkSession.builder()
            .appName("FindingsIdsOfWords")
            .getOrCreate()

        import spark.implicits._

        val input = spark.read
        .option("header", true)
        .option("inferSchema", true) // will assume id is int
        .csv("/data/10.csv")

        input.withColumn("words", split($"words", ",")).show(truncate=false)

        val wordSet = input.select("word").distinct().collect().map(row => row.getAs[String](0))
        

    }
}