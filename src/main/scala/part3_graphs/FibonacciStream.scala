package part3_graphs

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, MergePreferred, RunnableGraph, Sink, Source, Zip}
import akka.stream.{ActorMaterializer, ClosedShape, UniformFanInShape}

object FibonacciStream extends App {

  implicit val system = ActorSystem("Fibo")
  implicit val materializer = ActorMaterializer()

  val fibonacciGraph = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val zip = builder.add(Zip[Long, Long])
    val mergeShape = builder.add(MergePreferred[(Long, Long)](1))
    val coreLogicShape = builder.add(Flow[(Long, Long)].map { t =>
      Thread.sleep(200)
      (t._2, t._1 + t._2)
    })
    val filterOutShape = builder.add(Flow[(Long, Long)].map(_._2))
    val broadcast = builder.add(Broadcast[(Long, Long)](2))

    zip.out ~> mergeShape ~> coreLogicShape ~> broadcast ~> filterOutShape
    mergeShape.preferred <~ broadcast

    UniformFanInShape(filterOutShape.out, zip.in0, zip.in1)
  }

  val source1 = Source.single(21L)
  val source2 = Source.single(34L)

  val stefiGraph = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val fiboShape = builder.add(fibonacciGraph)
    source1 ~> fiboShape
    source2 ~> fiboShape ~> Sink.foreach(println)

    ClosedShape
  }

  RunnableGraph.fromGraph(stefiGraph).run()

}

