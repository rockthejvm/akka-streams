package part2_primer

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, RunnableGraph, Sink, Source}

import scala.concurrent.Future

object FirstPrinciples extends App {

  implicit val system: ActorSystem             = ActorSystem("FirstPrinciples")
  implicit val materializer: ActorMaterializer = ActorMaterializer() // allows to run akka streams components

  // sources
  val source: Source[Int, NotUsed] = Source(1 to 10)
  // sinks
  val sink: Sink[Int, Future[Done]] = Sink.foreach[Int](println) // for input of sink apply println

  val graph: RunnableGraph[NotUsed] = source.to(sink)
  // 1 to 10 emitted form the source and received in the sink: -> prints 1 2 3 4 5 6 7 8 9 10
  //  graph.run()

  // flows job is to transform elements
  val flow: Flow[Int, Int, NotUsed] = Flow[Int].map(x => x + 1)

  // Composing Source and a Flow result the resulting component is emit elements and that's done by Source
  val sourceWithFlow: Source[Int, NotUsed] = source.via(flow) // attach flow to source

  val flowWithSink = flow.to(sink)

  // All three do the same
  //  sourceWithFlow.to(sink).run()
  //  source.to(flowWithSink).run()
  //  source.via(flow).to(sink).run() // most used?

  // nulls are NOT allowed
  // val illegalSource = Source.single[String](null)
  // illegalSource.to(Sink.foreach(println)).run()
  // use Options instead

  // various kinds of sources
  val finiteSource        = Source.single(1)
  val anotherFiniteSource = Source(List(1, 2, 3))
  val emptySource         = Source.empty[Int]
  val infiniteSource      = Source(Stream.from(1)) // do not confuse an Akka stream with a "collection" Stream
  import scala.concurrent.ExecutionContext.Implicits.global
  val futureSource = Source.fromFuture(Future(42))

  // sinks
  val theMostBoringSink: Sink[Any, Future[Done]] = Sink.ignore
  val foreachSink: Sink[String, Future[Done]]    = Sink.foreach[String](println)
  val headSink: Sink[Int, Future[Int]]           = Sink.head[Int] // retrieves head and then closes the stream
  val foldSink: Sink[Int, Future[Int]]           = Sink.fold[Int, Int](0)((a, b) => a + b)

  // flows - usually mapped to collection operators
  val mapFlow: Flow[Int, Int, NotUsed] = Flow[Int].map(x => 2 * x)
  val takeFlow                         = Flow[Int].take(5)
  // drop, filter
  // NOT have flatMap

  // Constructing a stream: source -> flow -> flow -> ... -> sink
  val doubleFlowGraph: RunnableGraph[NotUsed] = source.via(mapFlow).via(takeFlow).to(sink)
  //  doubleFlowGraph.run()

  // syntactic sugars
  val mapSource = Source(1 to 10).map(x => x * 2) // Source(1 to 10).via(Flow[Int].map(x => x * 2))
  // run streams directly
  //  mapSource.runForeach(println) // mapSource.to(Sink.foreach[Int](println)).run()

  // OPERATORS = components

  /**
    * Exercise: create a stream that takes the names of persons, then you will keep the first 2 names with length > 5 characters.
    */
  val names: List[String]                         = List("Alice", "Bob", "Charlie", "David", "Martin", "AkkaStreams")
  val nameSource: Source[String, NotUsed]         = Source(names)
  val longNameFlow: Flow[String, String, NotUsed] = Flow[String].filter(name => name.length > 5) // This flow receive elements of String
  val limitFlow: Flow[String, String, NotUsed]    = Flow[String].take(2)
  val nameSink: Sink[String, Future[Done]]        = Sink.foreach[String](println)

  val composeGraph: RunnableGraph[NotUsed] = nameSource.via(longNameFlow).via(limitFlow).to(nameSink)
  composeGraph.run() // runs the stream
  val alias: Future[Done] = nameSource.filter(_.length > 5).take(2).runForeach(println) // does the same as line above

}
