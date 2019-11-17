package com.deloitte.ac.datalake.gcp.connection

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.Connection
import org.apache.hadoop.security.UserGroupInformation
import org.apache.spark.sql.SparkSession

trait GCPConnection {
  def createConnection(ProjectId: String, InstanceID: String, ugi: UserGroupInformation): Connection

  def createBigTableIfNotExists(connection: Connection, name: String): Boolean

  def setBatchConfigOptions(config: Configuration)

  def getSpark(master: String): SparkSession

  def ugiInfo(sc: SparkSession): UserGroupInformation
}
