
## Launch and Run Jobs on YARN

1. Build and start the cluster (scaling to 2 YARN NodeManagers):
```bash
docker compose up -d --scale nodemanager=2
```

2. Submit a Spark job in YARN Cluster Mode:

```bash
docker exec -it spark-client spark-submit \
  --class workshop.sql.SplitFunctionDelimiter \
  --master yarn \
  --deploy-mode cluster \
  /workspace/spark-workshop/target/scala-2.13/spark-workshop_2.13-0.1.0.jar
```

3. Launch an interactive PySpark shell on YARN:

```bash
docker exec -it spark-client pyspark --master yarn --deploy-mode client
```

4. Monitor Web UIs on your host:

YARN ResourceManager: http://localhost:8088 (verify active NodeManagers and running YARN containers)

HDFS NameNode: http://localhost:9870 (browse /spark-jars and /spark-logs)

Spark History Server: http://localhost:18080


5. View logs after submitting

```bash
docker exec spark-client yarn logs -applicationId application_1794746533678_0003 -log_files stdout
```