/*
Write a structured query that adds aggregations to the input dataset.
EXTRA Can you think of another query to solve it? What’s more efficient?
*/

package workshop.sql

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window

object AddingCountSource {
    def main(args: Array[String]): Unit = {

        val spark = SparkSession.builder()
            .appName("AddingCountSource")
            .getOrCreate()

        import spark.implicits._

        val input = Seq(
        ("05:49:56.604899", "10.0.0.2.54880", "10.0.0.3.5001",  2),
        ("05:49:56.604900", "10.0.0.2.54880", "10.0.0.3.5001",  2),
        ("05:49:56.604899", "10.0.0.2.54880", "10.0.0.3.5001",  2),
        ("05:49:56.604900", "10.0.0.2.54880", "10.0.0.3.5001",  2),
        ("05:49:56.604899", "10.0.0.2.54880", "10.0.0.3.5001",  2),
        ("05:49:56.604900", "10.0.0.2.54880", "10.0.0.3.5001",  2),
        ("05:49:56.604899", "10.0.0.2.54880", "10.0.0.3.5001",  2),
        ("05:49:56.604900", "10.0.0.2.54880", "10.0.0.3.5001",  2),
        ("05:49:56.604899", "10.0.0.2.54880", "10.0.0.3.5001",  2),
        ("05:49:56.604900", "10.0.0.2.54880", "10.0.0.3.5001",  2),
        ("05:49:56.604899", "10.0.0.2.54880", "10.0.0.3.5001",  2),
        ("05:49:56.604900", "10.0.0.2.54880", "10.0.0.3.5001",  2),
        ("05:49:56.604899", "10.0.0.2.54880", "10.0.0.3.5001",  2),
        ("05:49:56.604908", "10.0.0.3.5001",  "10.0.0.2.54880", 2),
        ("05:49:56.604908", "10.0.0.3.5001",  "10.0.0.2.54880", 2),
        ("05:49:56.604908", "10.0.0.3.5001",  "10.0.0.2.54880", 2),
        ("05:49:56.604908", "10.0.0.3.5001",  "10.0.0.2.54880", 2),
        ("05:49:56.604908", "10.0.0.3.5001",  "10.0.0.2.54880", 2),
        ("05:49:56.604908", "10.0.0.3.5001",  "10.0.0.2.54880", 2),
        ("05:49:56.604908", "10.0.0.3.5001",  "10.0.0.2.54880", 2)).toDF("column0", "column1", "column2", "label")

        val aggDf = input.groupBy("column1", "column2").count().as("count")

        val withJoinDf = input.join(aggDf, Seq("column1", "column2"))
        withJoinDf.show()
        withJoinDf.explain()

        // EXTRA
        val withWindow = input.withColumn("count", count("column1").over(Window.partitionBy("column1", "column2")))
        withWindow.show()
        withWindow.explain()

        /*
        JOIN PLAN
            - 2 table scans: 
                -> once to read input table (only two columns read) and compute the aggregate
                -> the second one to join it (all columns read)
            - HashAggregate: the count. 
                -> The first HashAggregate pre-aggregates locally per partition before the Exchange
                -> This makes the Exchange lighter as it sends partial results
            - BroadcastExchange:
                -> the aggDf is small enough to be broadcasted for the join
            - BroadcastHashJoin: the join itself

        == Physical Plan ==
        AdaptiveSparkPlan isFinalPlan=false
        +- Project [column1#18, column2#19, column0#17, label#20, count#21L]
        +- BroadcastHashJoin [column1#18, column2#19], [column1#32, column2#33], Inner, BuildRight, false, false
            :- LocalTableScan [column0#17, column1#18, column2#19, label#20]
            +- BroadcastExchange HashedRelationBroadcastMode(List(input[0, string, true], input[1, string, true]),false), [plan_id=136]
                +- HashAggregate(keys=[column1#32, column2#33], functions=[count(1)])
                    +- Exchange hashpartitioning(column1#32, column2#33, 200), ENSURE_REQUIREMENTS, [plan_id=133]
                    +- HashAggregate(keys=[column1#32, column2#33], functions=[partial_count(1)])
                        +- LocalTableScan [column1#32, column2#33]

        WINDOW PLAN
            - 1 table scan (all columns read)
            - Sort
                -> Partition-local sort (indicated by the false flag)
                -> sorts rows locally inside each partition by (column1, column2) so the Window operator can stream through each partition's frame (unboundedPreceding to unboundedFollowing)

        == Physical Plan ==
        AdaptiveSparkPlan isFinalPlan=false
        +- Window [count(column1#18) windowspecdefinition(column1#18, column2#19, specifiedwindowframe(RowFrame, unboundedpreceding$(), unboundedfollowing$())) AS count#53L], [column1#18, column2#19]
        +- Sort [column1#18 ASC NULLS FIRST, column2#19 ASC NULLS FIRST], false, 0
            +- Exchange hashpartitioning(column1#18, column2#19, 200), ENSURE_REQUIREMENTS, [plan_id=195]
                +- LocalTableScan [column0#17, column1#18, column2#19, label#20]
        */

        /* 
        Which one is more performant?

        In this case, the Join is more performant.
            -> there are no shuffles and no sorts for the join because the aggregated DF is broadcasted.
            -> the aggregated DF is pre-aggregated locally, minimizing the Exchange impact (less data shuffled)
        
        When is the join less performant?
            - High cardinality: 
                -> If (column1, column2) has millions of distinct combinations, the aggregated table grows larger than the broadcast threshold.
                    -> Spark can't perform the BroadcastHashJoin and falls back to SortMergeJoin (2 shuffles and 2 sorts)
            - Expensive uncached transformations:
                -> If input is not a direct file/table scan (where Spark can prune columns on disk) 
                   but rather the output of heavy upstream joins or UDFs, scanning it twice forces Spark to compute that entire 
                   lineage twice (unless we call input.cache() first).        
        */
    }
}