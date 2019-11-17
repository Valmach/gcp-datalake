package com.deloitte.ac.datalake.gcp.connection

import java.security.PrivilegedAction

import com.google.cloud.bigtable.hbase.BigtableConfiguration
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.Connection

/**
  * Creates a connection to HBase
  *
  * This can be removed once this gets migrated to Spark 2.x
  *
  * == Usage ==
  * Use with a [[org.apache.hadoop.security.UserGroupInformation]] instance.
  *
  * '''Requires''' a valid Kerberos keytab and principal on all cluster nodes!
  *
  * {{{
  *   val ugi = UserGroupInformation.loginUserFromKeytabAndReturnUGI(principal, keytab)
  *   val connection = ugi.doAs(new PrivilegedHBaseConnector(hconf))
  * }}}
  *
  * @param bconf BigTable Configuration
  */
class PrivilegedBigTableConnector(bconf: Configuration) extends PrivilegedAction[Connection] {

  /**
    * Run the core method
    *
    * @return Connection
    */
  override def run(): Connection = {
    BigtableConfiguration.connect(bconf)
  }
}
