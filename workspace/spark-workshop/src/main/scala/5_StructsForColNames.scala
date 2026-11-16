/* 
Write a structured query that “transpose” a dataset so a new dataset uses column names and values from a struct column.
ratings:
    +------+--------------------------------------------------+
    |name  |movieRatings                                      |
    +------+--------------------------------------------------+
    |Manuel|[[Logan, 1.5], [Zoolander, 3.0], [John Wick, 2.5]]|
    |John  |[[Logan, 2.0], [Zoolander, 3.5], [John Wick, 3.0]]|
    +------+--------------------------------------------------+
Solution:
    +------+-----+---------+---------+
    |name  |Logan|Zoolander|John Wick|
    +------+-----+---------+---------+
    |Manuel|1.5  |3.0      |2.5      |
    |John  |2.0  |3.5      |3.0      |
    +------+-----+---------+---------+
*/

package workshop.sql

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object StructsForColNames {

    case class MovieRatings(movieName: String, rating: Double)
    case class MovieCritics(name: String, movieRatings: Seq[MovieRatings])

    def main(args: Array[String]): Unit = {

        val spark = SparkSession.builder()
            .appName("CollectSetLimit")
            .getOrCreate()

        import spark.implicits._
        
        val movies_critics = Seq(
            MovieCritics("Manuel", Seq(MovieRatings("Logan", 1.5), MovieRatings("Zoolander", 3), MovieRatings("John Wick", 2.5))),
            MovieCritics("John", Seq(MovieRatings("Logan", 2), MovieRatings("Zoolander", 3.5), MovieRatings("John Wick", 3)))
        )
        val ratings = movies_critics.toDF()

        val explodedDf = ratings
        .select($"name", explode($"movieRatings").as("explode"))
        .select($"name", $"explode.movieName".as("movieName"), $"explode.rating".as("rating"))

        explodedDf
        .groupBy("name")
        .pivot("movieName")
        .agg(first("rating")).show()

    }
}