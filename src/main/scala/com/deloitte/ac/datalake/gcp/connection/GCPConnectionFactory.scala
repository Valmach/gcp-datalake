package com.deloitte.ac.datalake.gcp.connection

import java.util.concurrent.TimeUnit

import com.google.bigtable.repackaged.com.google.cloud.bigtable.config.BigtableOptions
import com.google.cloud.bigtable.hbase.{BigtableConfiguration, BigtableOptionsFactory}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory}
import org.apache.hadoop.hbase.{HColumnDescriptor, HTableDescriptor, TableName}
import org.apache.hadoop.security.UserGroupInformation
import org.apache.spark.sql.SparkSession

class GCPConnectionFactory extends GCPConnection {

  def createConnection(ProjectId: String, InstanceID: String, ugi: UserGroupInformation): Connection = {
    val config = BigtableConfiguration.configure(ProjectId, InstanceID)
    config.set(BigtableOptionsFactory.APP_PROFILE_ID_KEY, InstanceID)
    ConnectionFactory.createConnection(config)
  }

  def createBigTableIfNotExists(connection: Connection, name: String): Boolean = {
    val tableName = TableName.valueOf("default", name)
    var ret = true
    val admin = connection.getAdmin()
    try {
      if (!admin.tableExists(tableName)) {
        val tableDescriptor = new HTableDescriptor(tableName)
        tableDescriptor.addFamily(
          new HColumnDescriptor(s"cf_${name}"))
        admin.createTable(tableDescriptor)
      }
    } catch {
      case ex: Exception => {
        ret = false
      }
    }
    finally {
      admin.close()
    }
    return ret
  }

  def setBatchConfigOptions(config: Configuration) = {
    config.set(BigtableOptionsFactory.BIGTABLE_USE_CACHED_DATA_CHANNEL_POOL, "true")

    // Dataflow should use a different endpoint for data operations than online traffic.
    // config.set(BigtableOptionsFactory.BIGTABLE_HOST_KEY, BigtableOptions.BIGTABLE_BATCH_DATA_HOST_DEFAULT)

    config.set(BigtableOptionsFactory.INITIAL_ELAPSED_BACKOFF_MILLIS_KEY, String.valueOf(TimeUnit.SECONDS.toMillis(5)))

    config.set(BigtableOptionsFactory.MAX_ELAPSED_BACKOFF_MILLIS_KEY, String.valueOf(TimeUnit.MINUTES.toMillis(5)))

    // This setting can potentially decrease performance for large scale writes. However, this
    // setting prevents problems that occur when streaming Sources, such as PubSub, are used.
    // To override this behavior, call:
    config.set(BigtableOptionsFactory.BIGTABLE_USE_BATCH, "true")
    config.set(BigtableOptionsFactory.BIGTABLE_ASYNC_MUTATOR_COUNT_KEY, BigtableOptions.getDefaultOptions.getBulkOptions.getAsyncMutatorCount.toString);
    config.set(BigtableOptionsFactory.BIGTABLE_ASYNC_MUTATOR_COUNT_KEY, "0")
  }

  def getSpark(master: String): SparkSession = {
    var sc = SparkSession.builder()
      .appName(getClass.getName).master(master)
      .enableHiveSupport()
      .getOrCreate()

    sc = setSparkConfiguration(sc)
    return sc
  }

  /**
    * Setting Spark Configuration parameters
    *
    * @param sc Spark Session
    */
  def setSparkConfiguration(sc: SparkSession): SparkSession = {
    // Avoid java.util.NoSuchElementException: next on empty iterator
    sc.conf.set("spark.sql.hive.convertMetastoreOrc", "true")
    sc.conf.set("spark.sql.orc.enabled", "true")
    sc.conf.set("spark.sql.orc.char.enabled", "true")
    sc.conf.set("spark.sql.orc.impl", "native")
    sc.conf.set("spark.sql.orc.filterPushdown", "true")
    sc.conf.set("hive.exec.dynamic.partition", "true")
    sc.conf.set("hive.exec.dynamic.partition.mode", "nonstrict")
    sc.conf.set("hive.support.quoted.identifiers", "none")
    sc.conf.set("spark.sql.tungsten.enabled", "true")
    sc.conf.set("spark.io.compression.codec", "snappy")
    sc.conf.set("spark.rdd.compress", "true")
    sc.conf.set("spark.sql.parquet.mergeSchema", "true")
    // New
    // sc.conf.set("hive.execution.engine", "tez")
    sc.conf.set("hive.enforce.bucketing", "true")
    sc.conf.set("optimize.sort.dynamic.partitionining", "true")
    sc.conf.set("hive.vectorized.execution.enabled", "true")
    sc.conf.set("hive.enforce.sorting", "true")

    //Retry failed attempts
    sc.conf.set("spark.yarn.maxAppAttempts", "1")

    sc
  }

  def ugiInfo(sc: SparkSession): UserGroupInformation = {
    // Kerberos
    //val keytab = sc.conf.get("spark.yarn.keytab")
    //val principal = sc.conf.get("spark.yarn.principal")
    //print(s"Kerberos keytab ${keytab} ; Principal ${principal}")
    val ugi = UserGroupInformation.getLoginUser()
    return ugi
  }
}
