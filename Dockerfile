FROM eclipse-temurin:17-jdk-noble

ENV HADOOP_VERSION=3.4.3 \
    SPARK_VERSION=4.3.0 \
    JAVA_HOME=/opt/java/openjdk \
    HADOOP_HOME=/opt/hadoop \
    HADOOP_CONF_DIR=/opt/hadoop/etc/hadoop \
    SPARK_HOME=/opt/spark \
    PYSPARK_PYTHON=python3

ENV PATH="${PATH}:${HADOOP_HOME}/bin:${HADOOP_HOME}/sbin:${SPARK_HOME}/bin:${SPARK_HOME}/sbin"

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl procps python3 python3-pip \
    && rm -rf /var/lib/apt/lists/*

# Install Hadoop 3.4.3
RUN curl -fSL "https://dlcdn.apache.org/hadoop/common/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz" -o /tmp/hadoop.tar.gz \
    || curl -fSL "https://archive.apache.org/dist/hadoop/common/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz" -o /tmp/hadoop.tar.gz \
    && tar -xzf /tmp/hadoop.tar.gz -C /opt \
    && mv "/opt/hadoop-${HADOOP_VERSION}" "${HADOOP_HOME}" \
    && rm /tmp/hadoop.tar.gz

# Install Spark 4.3.0 (pre-built for Hadoop 3.4+)
RUN curl -fSL "https://dlcdn.apache.org/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop3.tgz" -o /tmp/spark.tgz \
    || curl -fSL "https://archive.apache.org/dist/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop3.tgz" -o /tmp/spark.tgz \
    && tar -xzf /tmp/spark.tgz -C /opt \
    && mv "/opt/spark-${SPARK_VERSION}-bin-hadoop3" "${SPARK_HOME}" \
    && rm /tmp/spark.tgz

# Link Spark to Hadoop classpath
RUN echo 'export HADOOP_CONF_DIR=/opt/hadoop/etc/hadoop' >> "${SPARK_HOME}/conf/spark-env.sh" \
    && echo 'export SPARK_DIST_CLASSPATH=$(/opt/hadoop/bin/hadoop classpath)' >> "${SPARK_HOME}/conf/spark-env.sh" \
    && chmod +x "${SPARK_HOME}/conf/spark-env.sh"

COPY config/*.xml "${HADOOP_CONF_DIR}/"
COPY config/spark-defaults.conf "${SPARK_HOME}/conf/"
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

WORKDIR /workspace
ENTRYPOINT ["/entrypoint.sh"]