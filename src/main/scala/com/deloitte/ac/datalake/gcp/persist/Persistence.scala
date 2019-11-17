package com.deloitte.ac.datalake.gcp.persist

import org.apache.spark.sql.{Dataset, Row, SparkSession}

trait Persistence {
  def persistData(sc: SparkSession, df: Dataset[Row], tablecatalog: String): Boolean
}
