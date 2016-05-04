package org.neo4j.scala

import org.neo4j.graphdb._

/**
 * trait for implicits
 * used by Neo4j wrapper
 *
 * @author Christopher Schmidt
 */
trait Neo4jWrapperImplicits {

  /**
   * converts to a relationship builder to use --> <-- methods
   */
  implicit def node2relationshipBuilder(node: Node) = new NodeRelationshipMethods(node)

  /**
   * converts a String to a relationship type
   */
  implicit def string2RelationshipType(relType: String) = DynamicRelationshipType.withName(relType)

  /**
   * conversion to use property set and get convenience
   */
  implicit def propertyContainer2RichPropertyContainer(propertyContainer: PropertyContainer) = new RichPropertyContainer(propertyContainer)

  /**
   * Stuff for Indexes
   */
  implicit def indexManager(implicit ds: DatabaseService) = ds.gds.index

  /**
   * for serialization
   */
  implicit def nodeToCaseClass(pc: PropertyContainer)(implicit customConverters: Option[Map[String, AnyRef => AnyRef]] = None) = new {
    def toCC[T: Manifest]: Option[T] = Neo4jWrapper.toCC[T](pc)
  }
}
