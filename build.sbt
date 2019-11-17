name := "gcp-datalake"

version := "0.1"

scalaVersion := "2.11.8"

resolvers += "MVN" at "http://central.maven.org/maven2/"
resolvers += "HDP" at "http://repo.hortonworks.com/content/groups/public/"

// additional libraries
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.2.3" % "provided",
  "org.apache.spark" %% "spark-sql" % "2.2.3" % "provided",
  "org.apache.spark" %% "spark-hive" % "2.2.3" % "provided",
  "org.apache.hbase" % "hbase-common" % "2.0.4" % "compile",
  "org.apache.hbase" % "hbase-server" % "2.0.4" % "compile",
  "org.apache.hbase" % "hbase-mapreduce" % "2.0.4" % "compile",
  "io.grpc" % "grpc-netty-shaded" % "1.25.0" % "compile",
  "com.google.guava" % "guava" % "28.1-jre" % "compile",
  //"com.google.cloud.bigtable" % "bigtable-hbase-1.x" % "1.11.0",
  "com.google.cloud.bigtable" % "bigtable-hbase-2.x-hadoop" % "1.9.0" % "compile",
  "org.apache.hbase" % "hbase-client" % "2.0.4" % "compile",
  // Adding custom compiled SHC
  //"com.hortonworks" % "shc-core" % "1.1.1-2.1-s_2.11" % "compile",
  "org.scalatest" % "scalatest_2.11" % "3.0.5" % "test",
  "org.mockito" % "mockito-core" % "2.18.0" % "test"
  //"commons-validator" % "commons-validator" % "1.6" % "compile",
  //"io.netty" % "netty-all" % "4.1.17.Final" % "provided"
)

unmanagedBase := baseDirectory.value / "lib"

assemblyMergeStrategy in assembly := {
  case x if Assembly.isConfigFile(x) =>
    MergeStrategy.concat
  case PathList(ps@_*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
    MergeStrategy.rename
  case PathList("META-INF", xs@_*) =>
    (xs map {
      _.toLowerCase
    }) match {
      case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
        MergeStrategy.discard
      case ps@(x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
        MergeStrategy.discard
      case "plexus" :: xs =>
        MergeStrategy.discard
      case "services" :: xs =>
        MergeStrategy.filterDistinctLines
      case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
        MergeStrategy.filterDistinctLines
      case _ => MergeStrategy.first
    }
  case _ => MergeStrategy.first
}