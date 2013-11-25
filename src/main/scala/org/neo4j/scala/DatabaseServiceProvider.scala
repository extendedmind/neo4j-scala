package org.neo4j.scala

import org.neo4j.kernel.EmbeddedGraphDatabase
import java.net.URI
import java.util.{ HashMap => jMap }
import org.neo4j.unsafe.batchinsert.BatchInserter
import org.neo4j.unsafe.batchinsert.BatchInserterImpl
import org.neo4j.unsafe.batchinsert.BatchInserters
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.server.WrappingNeoServerBootstrapper
import org.neo4j.kernel.GraphDatabaseAPI
import org.neo4j.server.configuration.ServerConfigurator
import org.neo4j.server.configuration.Configurator

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
   * Graph Database Factory to use to create the new
   */
  def graphDatabaseFactory: GraphDatabaseFactory

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
   * Location to config file
   */
  def configFileLocation: String = null
    
  /**
   * using an instance of an embedded graph database
   */
  val ds: DatabaseService = {
    import collection.JavaConversions.mapAsJavaMap
    if (configFileLocation != null) {
      DatabaseServiceImpl(
        graphDatabaseFactory
          .newEmbeddedDatabaseBuilder(neo4jStoreDir)
          .loadPropertiesFromFile(configFileLocation)
          .newGraphDatabase())
    } else {
      DatabaseServiceImpl(
        graphDatabaseFactory
          .newEmbeddedDatabaseBuilder(neo4jStoreDir)
          .setConfig(configParams)
          .newGraphDatabase())
    }
  }
}