package part2_primer

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object Practice extends App {
  implicit val system: ActorSystem             = ActorSystem("PraciteMaterializer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  case class User(name: String, age: Int)
  val users: List[User] = List(User("Esma", 23))

  val userSource: Source[User, NotUsed]              = Source(users)
  val transForm: Flow[User, Int, NotUsed]            = Flow[User].map(usr => usr.age + 1)
  val userSink: Sink[Any, Future[Done]]              = Sink.foreach(println)
  private val userGraph: RunnableGraph[Future[Done]] = userSource.viaMat(transForm)(Keep.right).toMat(userSink)(Keep.right)

  userGraph.run().onComplete {
    case Success(value)     => println(s"result of userGraph: $value")
    case Failure(exception) => println(exception)
  }

  val asf = transForm.runWith(userSource, userSink)

}
