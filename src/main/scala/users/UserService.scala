package users

import akka.actor.{ActorLogging, Actor}
import akka.event.LoggingAdapter
import spray.http.MediaTypes.{ `text/html` , `application/json`}
import spray.http.StatusCodes.{Created, OK}
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.routing._

/**
 * An actor that implements functionality of HTTP server. All HTTP requests are delivered to its mailbox
 */
class UserServiceActor extends Actor with ActorLogging with UserService with FakePersistenceService {

  def userLog = log

  def actorRefFactory = context

  def receive = runRoute(usersRoute)
}

/**
 * In order to be able to marshal and unmarshal User instances
 */
object UserJsonProtocol extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat(User, "invitee", "email")
}

/**
 * This trait defines our service behavior independently from the service actor
 */
trait UserService extends HttpService { this: PersistenceService =>
  def userLog:LoggingAdapter

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
            complete {
              List(load(name))
            }
          }
        }
      } ~
      post {
        respondWithStatus(Created) {
          entity(as[User]) { user =>
            userLog.info(s"Creating $user")
            save(user)
            complete("")
          }
        }
      } ~
      delete {
        respondWithStatus(OK) {
          parameter("name") { name =>
            userLog.info(s"Removing $name")
            remove(name)
            complete("")
          }
        }
      }
    }
  }
}