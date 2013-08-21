package org.neo4j.scala.unittest

import org.specs2.mutable.SpecificationWithJUnit
import org.neo4j.scala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
import org.neo4j.scala.util.CaseClassDeserializer
import CaseClassDeserializer._

class DeSerializingWithoutNeo4jSpec extends SpecificationWithJUnit {

  "De- and Serializing" should {
    "able to create an instance from map" in {
      val m = Map[String, AnyRef]("s" -> "sowas", "i" -> "1", "ji" -> "2", "d" -> (3.3).asInstanceOf[AnyRef], "l" -> "10", "b" -> "true", "ar" -> Array("1", "2"))
      val r = deserialize[Test](m)

      r.get.s must endWith("sowas")
      r.get.i must_== (1)
      r.get.ji must_== (2)
      r.get.d must_== (3.3)
      r.get.l must_== (10)
      r.get.b must_== (true)
      r.get.ar must_== (Array("1", "2"))

    }

    "able to create a map from an instance" in {
      val o = Test("sowas", 1, 2, 3.3, 10, true, Array("1", "2"))
      val resMap = serialize(o)

      resMap.size must_== 7
      resMap.get("d").get mustEqual (3.3)
      resMap.get("b").get mustEqual (true)
      resMap.get("ar").get mustEqual (Array("1", "2"))
    }
  }
}
