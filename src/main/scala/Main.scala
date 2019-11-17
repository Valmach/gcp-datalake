package com.deloitte.ac.datalake.gcp

import com.deloitte.ac.datalake.gcp.connection.{GCPConnection, GCPConnectionFactory}
import com.deloitte.ac.datalake.gcp.imports.HdfsImport
import com.deloitte.ac.datalake.gcp.mapper.{File2BigTableMapper, Mapper}
import com.deloitte.ac.datalake.gcp.persist.{BigTablePersistence, Persistence}
import org.apache.spark.sql.{Dataset, Row, SparkSession}

object Main {
  def main(args: Array[String]): Unit = {
    val ProjectId = args(0)
    val InstanceId = args(1)
    val tablename = args(2)
    val sourcefilepath = args(3)
    val filetype = args(4)

    try {
      val gcpconnection: GCPConnection = new GCPConnectionFactory()
      val sc: SparkSession = gcpconnection.getSpark("yarn")
      val hbaseconn = gcpconnection.createConnection(ProjectId, InstanceId, gcpconnection.ugiInfo(sc))
      gcpconnection.setBatchConfigOptions(hbaseconn.getConfiguration)

      val createbigtable = gcpconnection.createBigTableIfNotExists(hbaseconn, tablename)

      if (!createbigtable)
        throw new Exception("Cannot create BigTable")

      val importModule: HdfsImport = new HdfsImport()
      var df: Dataset[Row] = importModule.readFromSource(sc, sourcefilepath, filetype, opts = Map("header" -> "true"))

      df.printSchema()

      df = df.withColumnRenamed("rowkey", "key")

      df = importModule.parallelizeMyDf(sc, df)

      val bigtablemapper: Mapper = new File2BigTableMapper()
      val bigtablecatalog = bigtablemapper.createHBaseTableCatalog(tablename, df)

      print(bigtablecatalog)

      val persistobject: Persistence = new BigTablePersistence()
      persistobject.persistData(sc, df, bigtablecatalog)
    } catch {
      case ex: Exception => {
        print("Exception in Main check stacktrace")
        ex.printStackTrace()
      }
    } finally {
      print("--")
    }
  }
}