package playground

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorAttributes.supervisionStrategy
import akka.stream.Supervision.resumingDecider
import akka.stream.{ActorMaterializer, Attributes, FlowShape, Inlet, Outlet}
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}
import akka.stream.stage.{GraphStage, GraphStageLogic, GraphStageWithMaterializedValue, InHandler, OutHandler}
import akka.testkit.TestKit

import scala.concurrent.{Future, Promise}

object Playground extends App {

  implicit val system = ActorSystem("AkkaStreamsDemo")
  // this line needs to be here for Akka < 2.6
  // implicit val materializer: ActorMaterializer = ActorMaterializer()
  import system.dispatcher

  val source = Source(1 to 10)
  val flow = Flow[Int].map(x => { println(x); x })
  val sink = Sink.fold[Int, Int](0)(_ + _)

  // connect the Source to the Sink, obtaining a RunnableGraph
  val runnable: RunnableGraph[Future[Int]] = source.via(flow).toMat(sink)(Keep.right)

  // materialize the flow and get the value of the FoldSink
  val sum: Future[Int] = runnable.run()
  sum.onComplete(x => println(s"Sum: $x"))

  val independentFlow = Flow[String].map(_.reverse)
  import akka.stream.scaladsl.FlowWithContext

  val independentFlowWithContext: FlowWithContext[String, Int, String, Int, NotUsed] =
    independentFlow.asFlowWithContext[String, Int, Int]((string, ctx) => string)(string => 0)

  val flowWithContext: FlowWithContext[String, Int, String, Int, NotUsed] = ???
  val mapped = flowWithContext.map(_.reverse)
  val mappedVia = flowWithContext.via(independentFlowWithContext)
}