package users

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http

import scala.concurrent.duration._

object Boot extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("on-spray-can")

  // create and start our service actor
  val service = system.actorOf(Props[UserServiceActor], "user-service")

  //implicit timeout is required by ask pattern
  implicit val timeout = Timeout(5.seconds)

  //start HTTP server by sending a Http.Bind command to the Http IO extension
  IO(Http) ? Http.Bind(service, interface = "localhost", port = 8080)
}
