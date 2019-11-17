import com.deloitte.ac.datalake.gcp.connection.{GCPConnection, GCPConnectionFactory}
import com.deloitte.ac.datalake.gcp.imports.HdfsImport
import com.deloitte.ac.datalake.gcp.mapper.{File2BigTableMapper, Mapper}
import com.deloitte.ac.datalake.gcp.persist.{BigTablePersistence, Persistence}
import org.apache.hadoop.hbase.client.Connection
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers, Suite}

class FunctionalTesting extends FlatSpec with BeforeAndAfter with Matchers with MockitoSugar with Suite {
  var sourcedf: Dataset[Row] = null

  var gcpconnection: GCPConnection = null
  var sc: SparkSession = null

  var importModule: HdfsImport = null

  var bigtablemapper: Mapper = null
  var bigtablecatalog = ""

  var persistobject: Persistence = null

  "createGCPConnection" should "return GCPConnectionFactory object" in {
    this.gcpconnection = new GCPConnectionFactory()
    assert(this.gcpconnection != null)
  }

  "getSparkSession" should "return SparkSession object" in {
    this.sc = this.gcpconnection.getSpark("local[*]")
    assert(this.sc != null)
  }

  "readFromSource" should "read data from source" in {
    this.importModule = new HdfsImport()
    this.sourcedf = importModule.readFromSource(this.sc, "../resources/TestDataProc.csv", "csv", Map("header" -> "false"))
    assert(this.importModule != null)
    this.sourcedf.show()
    assert(this.sourcedf.count() > 0)
  }

  "File2BigTableMapper" should "map dataframe fields to BigTable Catalog" in {
    this.bigtablemapper = new File2BigTableMapper()
    assert(this.bigtablemapper != null)
    this.bigtablecatalog = bigtablemapper.createHBaseTableCatalog("testtable", this.sourcedf)
    print(bigtablecatalog)
    assert(this.bigtablecatalog != null && !this.bigtablecatalog.equals(""))
  }

  "Persistence" should "write dataframe fields using format BigTableCatalog" in {
    val hbaseconn = mock[Connection]

    this.persistobject = new BigTablePersistence()
    assert(this.persistobject != null)
    val ret = this.persistobject.persistData(this.sourcedf, this.bigtablecatalog)
    assert(ret)
  }

}
