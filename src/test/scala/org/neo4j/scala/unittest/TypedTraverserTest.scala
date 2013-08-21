package org.neo4j.scala.unittest

import org.specs2.mutable.SpecificationWithJUnit
import sys.ShutdownHookThread
import org.neo4j.scala._

class TypedTraverserSpec extends SpecificationWithJUnit with Neo4jWrapper with SingletonEmbeddedGraphDatabaseServiceProvider with TypedTraverser {

  def neo4jStoreDir = "/tmp/temp-neo-TypedTraverserSpec"

  ShutdownHookThread {
    shutdown(ds)
  }

  final val nodes = Map("Neo" -> "Hacker",
    "Morpheus" -> "Hacker",
    "Trinity" -> "Hacker",
    "Cypher" -> "Hacker",
    "Agent Smith" -> "Program",
    "The Architect" -> "Whatever")


  def nodeMap = {
    withTx {  
      implicit neo =>
        val nm = for ((name, prof) <- nodes) yield (name, createNode(Test_Matrix(name, prof)))
  
        getReferenceNode --> "ROOT" --> nm("Neo")
  
        nm("Neo") --> "KNOWS" --> nm("Trinity")
        nm("Neo") --> "KNOWS" --> nm("Morpheus") --> "KNOWS" --> nm("Trinity")
        nm("Morpheus") --> "KNOWS" --> nm("Cypher") --> "KNOWS" --> nm("Agent Smith")
        nm("Agent Smith") --> "CODED_BY" --> nm("The Architect")
        nm
    }
  }

  "TypedTraverser" should {
    "be able to traverse one Node" in {
      withTx{
        implicit neo =>
          val erg = nodeMap("Neo").doTraverse[Test_Matrix](
              follow(BREADTH_FIRST) -- "KNOWS" ->- "CODED_BY" -<- "FOO") {
            END_OF_GRAPH
          } {
            case (x: Test_Matrix, tp) if (tp.depth == 2) => x.name.length > 2
          }.toList.sortWith(_.name < _.name)
          
          erg must contain(Test_Matrix("Cypher", "Hacker"))
          erg.length must be_==(1)
      }
    }
  }
}