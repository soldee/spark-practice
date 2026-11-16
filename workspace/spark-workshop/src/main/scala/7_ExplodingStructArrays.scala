/* 
Write a structured query that “explodes” an array of structs (of open and close hours).

$ cat input.json
    {
    "business_id": "abc",
    "full_address": "random_address",
    "hours": {
        "Monday": {
        "close": "02:00",
        "open": "11:00"
        },
        "Tuesday": {
        "close": "02:00",
        "open": "11:00"
        },
        "Friday": {
        "close": "02:00",
        "open": "11:00"
        },
        "Wednesday": {
        "close": "02:00",
        "open": "11:00"
        },
        "Thursday": {
        "close": "02:00",
        "open": "11:00"
        },
        "Sunday": {
        "close": "00:00",
        "open": "11:00"
        },
        "Saturday": {
        "close": "02:00",
        "open": "11:00"
        }
    }
    }

input:
    +-----------+--------------+----------------------------------------------------------------------------------------------------------------+
    |business_id|full_address  |hours                                                                                                           |
    +-----------+--------------+----------------------------------------------------------------------------------------------------------------+
    |abc        |random_address|[[02:00, 11:00], [02:00, 11:00], [02:00, 11:00], [00:00, 11:00], [02:00, 11:00], [02:00, 11:00], [02:00, 11:00]]|
    +-----------+--------------+----------------------------------------------------------------------------------------------------------------+

solution:
    +-----------+--------------+---------+---------+----------+
    |business_id|full_address  |day      |open_time|close_time|
    +-----------+--------------+---------+---------+----------+
    |abc        |random_address|Friday   |11:00    |02:00     |
    |abc        |random_address|Monday   |11:00    |02:00     |
    |abc        |random_address|Saturday |11:00    |02:00     |
    |abc        |random_address|Sunday   |11:00    |00:00     |
    |abc        |random_address|Thursday |11:00    |02:00     |
    |abc        |random_address|Tuesday  |11:00    |02:00     |
    |abc        |random_address|Wednesday|11:00    |02:00     |
    +-----------+--------------+---------+---------+----------+
*/

package workshop.sql

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object ExplodingStructArrays {

    def main(args: Array[String]): Unit = {

        val spark = SparkSession.builder()
            .appName("ExplodingStructArrays")
            .getOrCreate()

        import spark.implicits._
        
        val input = spark.read
        .option("multiline", true)
        .json("/data/7.json")

        input.show(truncate=false)
        input.printSchema()

        val cols = input
        .select("hours.*") // unpacks the struct into a column for each day
        .columns.map(day => {
            struct(
                lit(day).as("day"),
                col(f"hours.$day%s.open"),
                col(f"hours.$day%s.close")
            )
        })

        input
        .withColumn("dayStruct", explode(array(cols:_*)))
        .select($"business_id", $"full_address", $"dayStruct.day", $"dayStruct.open".as("open_time"), $"dayStruct.close".as("close_time"))
        .show()
    }
}