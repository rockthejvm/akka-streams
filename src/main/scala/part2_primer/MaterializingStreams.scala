package part2_primer

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object MaterializingStreams extends App {

  implicit val system: ActorSystem             = ActorSystem("MaterializingStreams")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import system.dispatcher

  val simpleGraph: RunnableGraph[NotUsed] = Source(1 to 10).to(Sink.foreach(println))
  //  val simpleMaterializedValue = simpleGraph.run()

  val source: Source[Int, NotUsed] = Source(1 to 10)
  val sink: Sink[Int, Future[Int]] = Sink.reduce[Int]((a, b) => a + b)
  //  val sumFuture = source.runWith(sink)
  //  sumFuture.onComplete {
  //    case Success(value) => println(s"The sum of all elements is :$value")
  //    case Failure(ex) => println(s"The sum of the elements could not be computed: $ex")
  //  }

  // choosing materialized values
  val simpleSource                        = Source(1 to 10)
  val simpleFlow: Flow[Int, Int, NotUsed] = Flow[Int].map(x => x + 1)
  val simpleSink                          = Sink.foreach[Int](println)
  val graph: RunnableGraph[Future[Done]]  = simpleSource.viaMat(simpleFlow)(Keep.right).toMat(simpleSink)(Keep.right)
//  graph.run().onComplete {
//    case Success(_)         => println("Stream processing finished.")
//    case Failure(exception) => println(s"Stream processing failed with: $exception")
//  }

  // sugars
  Source(1 to 10).runWith(Sink.reduce[Int](_ + _)) // source.to(Sink.reduce)(Keep.right)
  Source(1 to 10).runReduce[Int](_ + _)            // same

  // backwards
  Sink.foreach[Int](println).runWith(Source.single(42)) // source(..).to(sink...).run()
  // both ways
  Flow[Int].map(x => 2 * x).runWith(simpleSource, simpleSink)

  /**
    * - return the last element out of a source (use Sink.last)
    * - compute the total word count out of a stream of sentences
    *   - map, fold, reduce
    */
  val f1: Future[Int] = Source(1 to 10).toMat(Sink.last)(Keep.right).run()
  val f2: Future[Int] = Source(1 to 10).runWith(Sink.last)

  val sentenceSource = Source(
    List(
      "Akka is awesome",
      "I love streams",
      "Materialized values are killing me"
    )
  )
  val wordCountSink: Sink[String, Future[Int]] = Sink.fold[Int, String](0)((currentWords, newSentence) => currentWords + newSentence.split(" ").length)
  //  val g1: Future[Int]                          = sentenceSource.toMat(wordCountSink)(Keep.right).run()
  //  val g2: Future[Int]                          = sentenceSource.runWith(wordCountSink)
  //  val g3: Future[Int]                          = sentenceSource.runFold(0)((currentWords, newSentence) => currentWords + newSentence.split(" ").length)

  val wordCountFlow: Flow[String, Int, NotUsed] = Flow[String].fold[Int](0)((currentWords, newSentence) => currentWords + newSentence.split(" ").length)
//  val g4: Future[Int]                           = sentenceSource.via(wordCountFlow).toMat(Sink.head)(Keep.right).run()
//  val g5: Future[Int]                           = sentenceSource.viaMat(wordCountFlow)(Keep.left).toMat(Sink.head)(Keep.right).run()
//  val g6: Future[Int]                           = sentenceSource.via(wordCountFlow).runWith(Sink.head)
//  val g7: Future[Int]                           = wordCountFlow.runWith(sentenceSource, Sink.head)._2

}
