package org.neo4j.scala

import org.neo4j.graphdb.{Label => Neo4jLabel}

case class TestLabel(labelName: String) extends Neo4jLabel{
  override def name = labelName
}

object TestLabel {
  val TEST = TestLabel("TEST")
}

case class Test_Matrix(name: String, profession: String)

case class Test_NonMatrix(name: String, profession: String)

