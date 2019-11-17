import sbt._

object Dependencies {

  val bigtableVersion = "1.9.0"
  val sparkVersion = "2.2.3"
  val scalatestVersion = "3.0.5"
  val hbaseVersion = "1.3.1"

  val shcCore = "com.hortonworks" % "shc-core"
  val sparkSql = "org.apache.spark" %% "spark-sql" % sparkVersion
  val hbaseClient = "org.apache.hbase" % "hbase-client" % hbaseVersion
  val hbaseServer = "org.apache.hbase" % "hbase-server" % hbaseVersion
  val bigtableHbase = "com.google.cloud.bigtable" % "bigtable-hbase-2.x-hadoop" % bigtableVersion
  val sparkCore = "org.apache.spark" %% "spark-core" % sparkVersion
  val sparkHive = "org.apache.spark" %% "spark-hive" % sparkVersion
  val mockito = "org.mockito" % "mockito-core" % "2.18.0"
  val scalaTest = "org.scalatest" %% "scalatest" % scalatestVersion

  val shcVersionPrefix = "1.1.1-2.1-s_"

  def all(scalaBinaryVersion: String) = Seq(
    shcCore % (shcVersionPrefix + scalaBinaryVersion) excludeAll(
      ExclusionRule("org.apache.hbase", "hbase-server"),
      ExclusionRule("org.apache.hbase", "hbase-annotations"),
      ExclusionRule("org.apache.hbase.thirdparty", "hbase-shaded-protobuf"),
      ExclusionRule("org.apache.hbase.thirdparty", "hbase-shaded-nettyc")
    ),
    bigtableHbase excludeAll(
      ExclusionRule("org.apache.hbase", "hbase-shaded-client"),
      ExclusionRule("org.apache.hbase", "hbase-annotations"),
      ExclusionRule("org.apache.hbase", "hbase-shaded-netty")
    ),
    sparkSql % Provided,
    sparkCore % Provided,
    sparkHive % Provided,
    hbaseClient % Compile,
    hbaseServer % Compile,
    "com.google.guava" % "guava" % "28.1-jre" % Compile,
    "commons-validator" % "commons-validator" % "1.6" % Compile,
    scalaTest % Test,
    mockito % Test
  )
}