package users

import scala.concurrent.ExecutionContext
import akka.actor.{ActorLogging, Actor}
import akka.event.LoggingAdapter
import spray.http.MediaTypes.{ `text/html` , `application/json`}
import spray.http.StatusCodes.{Created, OK}
import spray.httpx.SprayJsonSupport
import spray.json
import spray.json._
import spray.routing._


/**
 * An actor that implements functionality of HTTP server. All HTTP requests are delivered to its mailbox
 */
class UserServiceActor extends Actor with ActorLogging with UserService with SlickPersistenceService {

  // required by UserService
  def userLog = log

  // required by UserService
  def exeContext = context.dispatcher

  // required by HttpService
  def actorRefFactory = context

  def receive = runRoute(usersRoute)
}

/**
 * In order to be able to marshal and unmarshal User instances
 */
object UserJsonProtocol extends DefaultJsonProtocol {
  implicit object UserJsonFormat extends RootJsonFormat[User] {
    def write(u: User) =
      JsObject("invitee" -> JsString(u.name), "email" -> JsString(u.email))

    def read(value: JsValue) = value.asJsObject.getFields("invitee", "email") match {
      case Seq(JsString(name), JsString(email)) =>
        new User(None, name, email)
      case _ => json.deserializationError("Color expected")
    }
  }
}

/**
 * This trait defines our service behavior independently from the service actor
 */
trait UserService extends HttpService { this: PersistenceService =>
  // abstract logger
  def userLog:LoggingAdapter

  // required by onSuccess
  implicit def exeContext:ExecutionContext

  val usersRoute = {
    import SprayJsonSupport.sprayJsonMarshaller
    import SprayJsonSupport.sprayJsonUnmarshaller
    import UserJsonProtocol._

    path("") {
      get {
        respondWithMediaType(`text/html`) {
          complete {
            <html>
              <body>
                <p>The following service is exposed:</p>
                <a href="invitation">invitation</a> (GET, POST, DELETE)
              </body>
            </html>
          }
        }
      }
    } ~
    path("invitation") {
      get {
        respondWithMediaType(`application/json`) {
          parameter("name" ? "John Smith") { name => //default name is John Smith
            userLog.info(s"Loading $name")
            onSuccess(load(name)) { user =>
              complete(List(user))
            }
          }
        }
      } ~
      post {
        respondWithStatus(Created) {
          entity(as[User]) { user =>
            userLog.info(s"Creating $user")
            onSuccess(save(user)) { _ =>
              complete("")
            }
          }
        }
      } ~
      delete {
        respondWithStatus(OK) {
          parameter("name") { name =>
            userLog.info(s"Removing $name")
            onSuccess(remove(name)) { _ =>
              complete("")
            }
          }
        }
      }
    }
  }
}