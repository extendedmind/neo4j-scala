package org.neo4j.scala.unittest

import org.specs2.mutable.SpecificationWithJUnit
import org.neo4j.scala.{Neo4jIndexProvider, EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
import collection.JavaConversions._
import sys.ShutdownHookThread
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.scala.TestLabel

/**
 * Test spec to check usage of index convenience methods
 *
 * @author Christopher Schmidt
 */

class IndexTestSpec extends SpecificationWithJUnit with Neo4jWrapper with EmbeddedGraphDatabaseServiceProvider with Neo4jIndexProvider {

  def neo4jStoreDir = "/tmp/temp-neo-index-test"

  def graphDatabaseFactory = new GraphDatabaseFactory

  override def NodeIndexConfig = ("MyTestIndex", Map("provider" -> "lucene", "type" -> "fulltext")) :: Nil
  
  "Neo4jIndexProvider" should {

    ShutdownHookThread {
      shutdown(ds)
    }

    "use the fulltext search index" in {
      withTx {
        implicit db =>
          val nodeIndex = getNodeIndex("MyTestIndex").get

          val theMatrix = createNode(TestLabel.TEST)
          val theMatrixReloaded = createNode(TestLabel.TEST)
          theMatrixReloaded.setProperty("name", "theMatrixReloaded")
  
          nodeIndex +=(theMatrix, "title", "The Matrix")
          nodeIndex +=(theMatrixReloaded, "title", "The Matrix Reloaded")
  
          // search in the fulltext index
          val found = nodeIndex.query("title", "reloAdEd")
          found.size must beGreaterThanOrEqualTo(1)
      }
    }
    "remove items from index" in {
      withTx {
        implicit db =>
          val nodeIndex = getNodeIndex("MyTestIndex").get

          val found = nodeIndex.query("title", "reloAdEd")
          val size = found.size
          for (f <- found.iterator)
            nodeIndex -= f
  
          // search in the fulltext index
          val found2 = nodeIndex.query("title", "reloAdEd")
          found2.size must beLessThanOrEqualTo(size)
      }
    }
  }
}