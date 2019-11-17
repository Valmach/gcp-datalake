package com.deloitte.ac.datalake.gcp.mapper

import org.apache.spark.sql.{Dataset, Row}

trait Mapper {
  def createHBaseTableCatalog(tableName: String, df: Dataset[Row], namespace: String = "default"): String
}
