package org.neo4j.scala.unittest

import org.specs2.mutable.SpecificationWithJUnit
import org.neo4j.scala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
import org.neo4j.scala.util.CaseClassDeserializer
import org.neo4j.graphdb.{Direction, DynamicRelationshipType}
import sys.ShutdownHookThread
import CaseClassDeserializer._
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.scala.TestLabel

/**
 * Test spec to check deserialization and serialization of case classes
 *
 * @author Christopher Schmidt
 */

case class Test(s: String, i: Int, ji: java.lang.Integer, d: Double, l: Long, b: Boolean, ar: Array[String])

case class Test2(jl: java.lang.Long, jd: java.lang.Double, jb: java.lang.Boolean, 
                 optionalString: Option[String] = None, alwaysIgnoredString: Option[String] = None)

case class NotTest(s: String, i: Int, ji: java.lang.Integer, d: Double, l: Long, b: Boolean)

trait PolyBase

case class Poly1(s: String) extends PolyBase

case class Poly2(s: String) extends PolyBase

class DeSerializingSpec extends SpecificationWithJUnit with Neo4jWrapper with EmbeddedGraphDatabaseServiceProvider {

  def neo4jStoreDir = "/tmp/temp-neo-test2"  
  def graphDatabaseFactory = new GraphDatabaseFactory
    
  "Node" should {

    ShutdownHookThread {
      shutdown(ds)
    }

    implicit val exclusions: Option[List[String]] = Some(
      List("alwaysIgnoredString")
    )
   
    "be serializable with Test" in {
      
      val o = Test("sowas", 1, 2, 3.3, 10, true, Array("2", "3"))
      val notO = NotTest("sowas", 1, 2, 3.3, 10, true)

      withTx {
        implicit neo => 
          val node = createNode(o, TestLabel.TEST)
          val oo1 = Neo4jWrapper.deSerialize[Test](node)      
          checkEquality(oo1, o)
    
          val oo2 = node.toCC[Test]
          checkEquality(oo2.get, o)
    
          val oo3 = node.toCC[NotTest]
          oo3 must beEqualTo(Some(notO))
          
          val oo4 = Neo4jWrapper.deSerialize[NotTest](node)
          oo4 must beEqualTo(notO)
      }
    }
    
    "be serializable with Test using custom converter" in {
      
      val o = Test("sowas", 1, 2, 3.3, 10, true, Array("2", "3"))
      withTx {
        implicit neo => 
          val node = createNode(o, TestLabel.TEST)
          implicit val customConverters: Option[Map[String, AnyRef => AnyRef]] = Some(
            Map("l" -> (p => p.asInstanceOf[Long] + 32: java.lang.Long))
          )
          val oo1 = Neo4jWrapper.deSerialize[Test](node)      
          oo1.l must beEqualTo(42)
      }
    }
    
    "be serializable with Test2" in {
      val o = Test2(1, 3.3, true)
      withTx {
        implicit neo => 
          val node = createNode(o)
          
          val oo1 = Neo4jWrapper.deSerialize[Test2](node)
          oo1 must beEqualTo(o)
          
          val oo2 = node.toCC[Test2]
          oo2 must beEqualTo(Option(o))
    
          val oo3 = node.toCC[NotTest]
          oo3 must beEqualTo(None)
    
          Neo4jWrapper.deSerialize[NotTest](node) must throwA[IllegalArgumentException]
      }
    }
    
    "be serializable with Test2 using exclusions" in {
      
      val o = Test2(1, 3.3, true, Some("Test"), Some("Ignored"))
      val oBack = Test2(1, 3.3, true, Some("Test"))

      withTx {
        implicit neo => 
          val node = createNode(o, TestLabel.TEST)
          val oo1 = Neo4jWrapper.deSerialize[Test2](node)      
          oo1 must beEqualTo(oBack)
      }
    }

    "be serializable with Test2 using Some" in {
      val o = Test2(1, 3.3, true, Some("Test"))
      withTx {
        implicit neo => 
          val node = createNode(o)
          
          val oo1 = Neo4jWrapper.deSerialize[Test2](node)
          oo1 must beEqualTo(o)
          
          val oo2 = node.toCC[Test2]
          oo2 must beEqualTo(Option(o))
    
          val oo3 = node.toCC[NotTest]
          oo3 must beEqualTo(None)
    
          Neo4jWrapper.deSerialize[NotTest](node) must throwA[IllegalArgumentException]
      }
    }

    "be possible with relations" in {
      val o = Test2(1, 3.3, true)
      withTx {
        implicit neo =>
          val start = createNode(TestLabel.TEST)
          val end = createNode(TestLabel.TEST)
          end <-- "foo" <-- start < o

          val rel = start.getSingleRelationship("foo", Direction.OUTGOING)
          val oo = rel.toCC[Test2]
          oo must beEqualTo(Some(o))
      }
    }
  }
  
  def checkEquality(test: Test, test2: Test){
      test.s must beEqualTo(test2.s)
      test.i must beEqualTo(test2.i)
      test.ji must beEqualTo(test2.ji)
      test.d must beEqualTo(test2.d)
      test.l must beEqualTo(test2.l)
      test.b must beEqualTo(test2.b)
      test.ar must beEqualTo(test2.ar)
  }
}