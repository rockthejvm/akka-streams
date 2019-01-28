package playground

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}

import scala.concurrent.Future

object Playground extends App {

  implicit val system = ActorSystem("AkkaStreamsDemo")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import system.dispatcher

  val source = Source(1 to 10)
  val flow = Flow[Int].map(x => { println(x); x })
  val sink = Sink.fold[Int, Int](0)(_ + _)

  // connect the Source to the Sink, obtaining a RunnableGraph
  val runnable: RunnableGraph[Future[Int]] = source.via(flow).toMat(sink)(Keep.right)

  // materialize the flow and get the value of the FoldSink
  val sum: Future[Int] = runnable.run()
  sum.onComplete(x => println(s"Sum: $x"))


}
