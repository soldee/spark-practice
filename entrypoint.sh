#!/usr/bin/env bash
set -e

ROLE="$1"

case "$ROLE" in
  namenode)
    mkdir -p /data/hdfs/namenode
    if [ ! -d "/data/hdfs/namenode/current" ]; then
      hdfs namenode -format -force -nonInteractive
    fi

    # Bootstrap HDFS directories and upload Spark JARs once NameNode & DataNode are ready
    (
      set +e
      until hdfs dfsadmin -safemode wait >/dev/null 2>&1; do
        echo "Waiting for NameNode RPC to become available..."
        sleep 2
      done

      until hdfs dfsadmin -report 2>/dev/null | grep -q "Live datanodes ([1-9]"; do
        echo "Waiting for at least 1 live DataNode..."
        sleep 2
      done

      hdfs dfs -mkdir -p /spark-jars /spark-logs /user/root
      if ! hdfs dfs -ls /spark-jars/*.jar >/dev/null 2>&1; then
        echo "Uploading Spark JARs to /spark-jars..."
        hdfs dfs -put -f "${SPARK_HOME}"/jars/*.jar /spark-jars/
        echo "Done uploading JARs to /spark-jars."
      fi
    ) &

    exec hdfs namenode
    ;;

  datanode)
    mkdir -p /data/hdfs/datanode
    exec hdfs datanode
    ;;

  resourcemanager)
    exec yarn resourcemanager
    ;;

  nodemanager)
    exec yarn nodemanager
    ;;

  client)
    until hdfs dfs -test -d /spark-logs >/dev/null 2>&1; do
      echo "Waiting for HDFS /spark-logs directory..."
      sleep 3
    done
    "${SPARK_HOME}/sbin/start-history-server.sh"
    exec tail -f /dev/null
    ;;

  *)
    echo "Unknown role: $ROLE"
    exit 1
    ;;
esac