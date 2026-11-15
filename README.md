
## Launch and Run Jobs on YARN

1. Build and start the cluster (scaling to 2 YARN NodeManagers):
```bash
docker compose up -d --scale nodemanager=2
```

2. Submit a Spark job in YARN Cluster Mode:

```bash
docker exec -it spark-client spark-submit \
  --class org.apache.spark.examples.SparkPi \
  --master yarn \
  --deploy-mode cluster \
  /opt/spark/examples/jars/spark-examples_2.13-4.3.0.jar 20
```

3. Launch an interactive PySpark shell on YARN:

```bash
docker exec -it spark-client pyspark --master yarn --deploy-mode client
```

4. Monitor Web UIs on your host:

YARN ResourceManager: http://localhost:8088 (verify active NodeManagers and running YARN containers)

HDFS NameNode: http://localhost:9870 (browse /spark-jars and /spark-logs)

Spark History Server: http://localhost:18080

