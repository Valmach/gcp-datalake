package com.deloitte.ac.datalake.gcp.imports

import org.apache.spark.sql.{Dataset, Row, SparkSession}

import scala.util.control.NonFatal

class HdfsImport {
  def readFromSource(spark: SparkSession, location: String, filetype: String, opts: Map[String, String] = Map()): Dataset[Row] = {
    var df: Dataset[Row] = null
    try {
      if (filetype != null && !filetype.toLowerCase.equals("")) {
        if (filetype.toLowerCase.equals("csv"))
          df = spark.read.options(opts).csv(location)
        else if (filetype.toLowerCase.equals("parquet"))
          df = spark.read.parquet(location)
        else if (filetype.toLowerCase.equals("orc"))
          df = spark.read.orc(location)
        else if (filetype.toLowerCase.equals("text"))
          df = spark.read.text(location)
        else
          df = null
      }
      else
        df = null
    } catch {
      case ex: org.apache.hadoop.mapred.InvalidInputException => {
        println("No new files" + ex.getMessage)
        df = null
      }
      case NonFatal(ex: Exception) => {
        ex.printStackTrace()
        df = null
      }
    }

    return df
  }

  def parallelizeMyDf(sc: SparkSession, _df: Dataset[Row]): Dataset[Row] = {
    import org.apache.spark.sql.functions.col
    var df: Dataset[Row] = _df.repartition(sc.sparkContext.getConf.getInt("spark.job.numhbaseregionservers", 4), sc.conf.get("spark.job.hbaserowkey", "key").split(",").map(f => col(f)): _*)
    df
  }
}