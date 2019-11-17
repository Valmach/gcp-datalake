package com.deloitte.ac.datalake.gcp.persist

import org.apache.spark.sql.execution.datasources.hbase.HBaseTableCatalog
import org.apache.spark.sql.{Dataset, Row, SparkSession}

import scala.util.Random

class BigTablePersistence extends Persistence {

  val PROBABILITY_OF_INTERESTING_VALUE = 0.5f

  def persistData(sc: SparkSession, df: Dataset[Row], tablecatalog: String): Boolean = {
    var ret = true
    val opts = Map(HBaseTableCatalog.tableCatalog -> tablecatalog, HBaseTableCatalog.nameSpace -> "default",
      HBaseTableCatalog.newTable -> "1", HBaseTableCatalog.tablePlatform -> "bigtable")
    try {
      import org.apache.spark.sql.functions.col
      df.repartition(sc.sparkContext.getConf.getInt("spark.job.numhbaseregionservers", 6),
        sc.conf.get("spark.job.hbaserowkey", "key").split(",").map(f => col(f)): _*)
        .write
        .options(opts)
        .format("org.apache.spark.sql.execution.datasources.hbase")
        .save()
    } catch {
      case ex: Exception => {
        ret = false
        print("Error write to HBase / BigTable")
        ex.printStackTrace()
      }
    }
    return ret
  }


  private def randomNumeric[T](
                                rand: Random,
                                uniformRand: Random => T,
                                interestingValues: Seq[T]): Some[() => T] = {
    val f = () => {
      if (rand.nextFloat() <= PROBABILITY_OF_INTERESTING_VALUE) {
        interestingValues(rand.nextInt(interestingValues.length))
      } else {
        uniformRand(rand)
      }
    }
    Some(f)
  }

}
