package org.neo4j.scala

import org.neo4j.kernel.EmbeddedGraphDatabase
import java.net.URI
import java.util.{HashMap => jMap}
import org.neo4j.unsafe.batchinsert.BatchInserter
import org.neo4j.unsafe.batchinsert.BatchInserterImpl
import org.neo4j.unsafe.batchinsert.BatchInserters

/**
 * Interface for a GraphDatabaseServiceProvider
 * must be implemented by and Graph Database Service Provider
 */
trait GraphDatabaseServiceProvider {
  val ds: DatabaseService
}


/**
 * provides a specific Database Service
 * in this case an embedded database service
 */
trait EmbeddedGraphDatabaseServiceProvider extends GraphDatabaseServiceProvider {

  /**
   * directory where to store the data files
   */
  def neo4jStoreDir: String

  /**
   * setup configuration parameters
   * @return Map[String, String] configuration parameters
   */
  def configParams = Map[String, String]()

  /**
   * using an instance of an embedded graph database
   */
  val ds: DatabaseService = {
    import collection.JavaConversions.mapAsJavaMap
    DatabaseServiceImpl(
      new EmbeddedGraphDatabase(neo4jStoreDir, new jMap[String, String](configParams))
    )
  }
}

/**
 * singleton EmbeddedGraphDatabase
 */
private[scala] object SingeltonProvider {
  private var ds: Option[DatabaseService] = None

  def apply(neo4jStoreDir: String, configParams: Map[String, String]) = ds match {
    case Some(x) => x
    case None =>
      import collection.JavaConversions.mapAsJavaMap
      ds = Some(DatabaseServiceImpl(new EmbeddedGraphDatabase(
        neo4jStoreDir, new jMap[String, String](configParams)))
      )
      ds.get
  }
}

/**
 * provides a specific Database Service
 * in this case an singleton embedded database service
 */
trait SingletonEmbeddedGraphDatabaseServiceProvider extends GraphDatabaseServiceProvider {

  /**
   * directory where to store the data files
   */
  def neo4jStoreDir: String

  /**
   * setup configuration parameters
   * @return Map[String, String] configuration parameters
   */
  def configParams = Map[String, String]()

  /**
   * using an instance of an embedded graph database
   */
  val ds: DatabaseService = SingeltonProvider(neo4jStoreDir, configParams)
}

/**
 * singleton provider
 */
private[scala] object SingeltonBatchProvider {
  private var inserter: Option[BatchInserter] = None
  
  
  def apply(neo4jStoreDir: String) = inserter match {
    case Some(x) => x
    case None =>
      inserter = Some(BatchInserters.inserter( neo4jStoreDir ))
      inserter.get
  }

  lazy val ds: DatabaseService = DatabaseServiceImpl(BatchInserters.batchDatabase(inserter.get.getStoreDir))
}

/**
 * provides a specific GraphDatabaseServiceProvider for
 * Batch processing
 */
trait BatchGraphDatabaseServiceProvider extends GraphDatabaseServiceProvider {


  /**
   * instance of BatchInserter
   */
  def batchInserter = SingeltonBatchProvider(neo4jStoreDir)

  /**
   * directory where to store the data files
   */
  def neo4jStoreDir: String

  /**
   * using an instance of an embedded graph database
   */
  val ds: DatabaseService = SingeltonBatchProvider.ds
}
