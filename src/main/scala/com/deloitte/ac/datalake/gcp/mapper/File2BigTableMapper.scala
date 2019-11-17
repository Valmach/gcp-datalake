package com.deloitte.ac.datalake.gcp.mapper

import org.apache.spark.sql.{Dataset, Row}

class File2BigTableMapper extends Mapper {
  def createHBaseTableCatalog(tableName: String, df: Dataset[Row], namespace: String = "default"): String = {
    val catcols = df.schema.fields.map(f => {
      val s =
        s""""${f.name}":{"cf":"cf_$tableName", "col":"${f.name}", "type":"${f.dataType.catalogString}"}""".stripMargin
      s
    })

    val cat =
      s"""{"table":{"namespace":"${namespace}", "name":"$tableName", "tableCoder":"PrimitiveType"},"rowkey":"key","columns":{"col0": {"cf": "rowkey","col": "key","type": "string"}, ${catcols.filterNot(_.contains("key")).mkString(",")}}}""".stripMargin

    return cat
  }
}