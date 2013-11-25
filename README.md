#Neo4j Scala Wrapper Lite

The Neo4j Scala Wrapper Lite allows you access the [Neo4j open source graph database](http://neo4j.org/) through a
domain-specific simplified language in an embedded environment. It is written in Scala and is intended
to be used in other Scala projects.

This wrapper is a stripped down version of [FaKod's Neo4j Scala wrapper library](https://github.com/FaKod/neo4j-scala)
with some new additions, such as labels, to support Neo4j 2.0.0.

##Building

    $ git clone git://github.com/ttiurani/neo4j-scala.git
    $ cd neo4j-scala
    $ mvn clean install

#Using this library

##Graph Database Service Provider

Neo4j Scala Wrapper needs a Graph Database Service Provider, it has to implement GraphDatabaseServiceProvider trait.
Use the EmbeddedGraphDatabaseServiceProvider for embedded Neo4j instances where you simply have
to define a Neo4j storage directory. The class MyNeo4jClass using the wrapper is f.e.:

```scala
class MyNeo4jClass extends SomethingClass with Neo4jWrapper with EmbeddedGraphDatabaseServiceProvider {
  def neo4jStoreDir = "/tmp/temp-neo-test"
  . . .
}
```

##Transaction Wrapping

Transactions are wrapped by withTx. After leaving the "scope" success is called (or rollback if an exception is raised):

```scala
withTx {
 implicit neo =>
   val start = createNode
   val end = createNode
   start --> "foo" --> end
}
```

##Using an Index

Neo4j provides indexes for nodes and relationships. The indexes can be configured by mixing in the Neo4jIndexProvider trait. See [Indexing](http://docs.neo4j.org/chunked/stable/indexing.html)

```scala
class MyNeo4jClass extends . . . with Neo4jIndexProvider {
  // configuration for the index being created.
  override def NodeIndexConfig = ("MyTest1stIndex", Some(Map("provider" -> "lucene", "type" -> "fulltext"))) ::
                                 ("MyTest2ndIndex", Some(Map("provider" -> "lucene", "type" -> "fulltext"))) :: Nil
}
```

Use one of the configured indexes with

```scala
val nodeIndex = getNodeIndex("MyTest1stIndex").get
```

Add and remove entries by:

```scala
nodeIndex += (Node_A, "title", "The Matrix")
nodeIndex -= (Node_A)
```

##Relations


Using this wrapper, this is how creating two relationships can look in Scala.
The String are automatically converted into Dynamic Relationsships:

```scala
start --> "KNOWS" --> intermediary --> "KNOWS" --> end
left --> "foo" --> middle <-- "bar" <-- right
```

To return the Property Container for the Relation Object use the '<' method:

```scala
val relation = start --> "KNOWS" --> end <
```

##Properties

And this is how getting and setting properties on a node or relationship looks like :

```scala
// setting the property foo
start("foo") = "bar"
// cast Object to String and match . . .
start[String]("foo") match {
  case Some(x) => println(x)
  case None => println("aww")
}
```

##Using Case Classes

Neo4j provides storing keys (String) and values (Object) into Nodes. To store Case Classes the properties are stored
as key/values to the Property Container, thai can be a Node or a Relation. However, Working types are limited to basic
types like String, integer etc.

```scala
case class Test(s: String, i: Int, ji: java.lang.Integer, d: Double, l: Long, b: Boolean)

. . .
withTx {
  implicit neo =>
    // create new Node with Case Class Test
    val node1 = createNode(Test("Something", 1, 2, 3.3, 10, true))

    // can Test be created from node
    val b:Boolean = node.toCCPossible[Test]

    // or using Option[T] (returning Some[T] if possible)
    val nodeOption: Option[Test] = node.toCC[Test]

    // yield all Nodes that are of type Case Class Test
    val tests = for(n <- getTraverser; t <- n.toCC[Test]) yield t

    // create new relation with Case Class Test
    node1 --> "foo" --> node2 < Test("other", 0, 1, 1.3, 1, false)
 }
```

##Traversing

Besides, the neo4j scala binding makes it possible to write stop and returnable evaluators in a functional style :

```scala
//StopEvaluator.END_OF_GRAPH, written in a Scala idiomatic way :
start.traverse(Traverser.Order.BREADTH_FIRST, (tp : TraversalPosition) => false,
ReturnableEvaluator.ALL_BUT_START_NODE, "foo", Direction.OUTGOING)

//ReturnableEvaluator.ALL_BUT_START_NODE, written in a Scala idiomatic way :
start.traverse(Traverser.Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH, (tp : TraversalPosition) => tp.notStartNode(),
"foo", Direction.OUTGOING)
```

Copyright and License
---------------------

Copyright (C) 2012 [Christopher Schmidt](http://blog.fakod.eu/)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
