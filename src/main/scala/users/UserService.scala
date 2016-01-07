package users

import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingAdapter
import spray.http.HttpHeaders._
import spray.http.MediaTypes.{`application/json`, `text/html`}
import spray.http.StatusCodes.{Created, MethodNotAllowed, OK}
import spray.http.{HttpMethods, HttpOrigin, SomeOrigins}
import spray.httpx.SprayJsonSupport
import spray.json._
import spray.routing._

import scala.concurrent.ExecutionContext

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

  def receive = runRoute(jointRoute)
}

/**
  * In order to be able to marshal and unmarshal User and Group instances
  */
object UserJsonProtocol extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat3(User)
  implicit val groupFormat = jsonFormat2(Group)
  implicit val usersGroupFormat = jsonFormat1(UsersGroup)
}

/**
  * This trait defines our service behavior independently from the service actor
  */
trait UserService extends HttpService {
  this: PersistenceService =>
  // abstract logger
  def userLog: LoggingAdapter

  // required by onSuccess
  implicit def exeContext: ExecutionContext

  val usersEndpointName = "user"
  val groupsEndpointName = "group"

  def respondWithHtmlInfoPage(baseUri: String) = respondWithMediaType(`text/html`) {
    complete {
      <html>
        <body>
          <p>The following services are exposed:</p>
          <ul>
            <li>
              <a href={baseUri + usersEndpointName}>user</a>
              (GET, POST, DELETE)</li>
            <li>
              <a href={baseUri + groupsEndpointName}>group</a>
              (GET, POST, DELETE)</li>
          </ul>
        </body>
      </html>
    }
  }

  val jointRoute = {
    import SprayJsonSupport.{sprayJsonMarshaller, sprayJsonUnmarshaller}
    import UserJsonProtocol._

    val requestUri = extract(_.request.uri)

    path("") {
      get {
        requestUri { uri =>
          optionalHeaderValueByType[Accept]() {
            case Some(a) => a match {
              case a: Accept if a.value.contains("text/html") || a.value.contains("*/*") => respondWithHtmlInfoPage(uri.toString())
              case a: Accept if a.value.contains("application/json") => respondWithMediaType(`application/json`) {
                complete(JsObject(usersEndpointName -> JsString(uri + usersEndpointName), groupsEndpointName -> JsString(uri + groupsEndpointName)))
              }
              case a: Accept => respondWithStatus(MethodNotAllowed) {
                userLog.debug(s"Requested unsupported media type: ${a.value}")
                complete("")
              }
            }
            case None => respondWithHtmlInfoPage(uri.toString())
          }
        }
      }
    } ~
      respondWithHeaders(
        `Access-Control-Allow-Origin`(SomeOrigins(List(HttpOrigin("http://localhost:8000")))),
        `Access-Control-Allow-Headers`(List("Content-Type")),
        `Access-Control-Allow-Methods`(HttpMethods.GET, HttpMethods.POST, HttpMethods.DELETE)) {
        pathPrefix(groupsEndpointName) {
          pathEnd {
            options {
              respondWithHeader(
                Allow(HttpMethods.GET, HttpMethods.POST)) {
                complete("")
              }
            } ~
              post {
                respondWithStatus(Created) {
                  entity(as[Group]) { group =>
                    userLog.debug(s"Creating $group")
                    onSuccess(saveGroup(group)) { group =>
                      complete(group)
                    }
                  }
                }
              } ~
              get {
                respondWithMediaType(`application/json`) {
                  userLog.debug(s"Loading all groups")
                  onSuccess(loadGroups()) { groups =>
                    complete(groups)
                  }
                }
              }
          } ~
            path(IntNumber) { id =>
              options {
                respondWithHeader(
                  Allow(HttpMethods.GET, HttpMethods.DELETE)) {
                  complete("")
                }
              } ~
                get {
                  respondWithMediaType(`application/json`) {
                    userLog.debug(s"Loading group $id")
                    onSuccess(loadGroup(id.toInt)) { group =>
                      complete(group)
                    }
                  }
                } ~
                delete {
                  respondWithStatus(OK) {
                    userLog.debug(s"Removing group $id")
                    onSuccess(removeGroup(id.toInt)) { _ =>
                      complete("")
                    }
                  }
                }
            }
        } ~
          pathPrefix(usersEndpointName) {
            pathEnd {
              options {
                respondWithHeader(
                  Allow(HttpMethods.GET, HttpMethods.POST)) {
                  complete("")
                }
              } ~
                post {
                  respondWithStatus(Created) {
                    entity(as[User]) { user =>
                      userLog.debug(s"Creating $user")
                      onSuccess(saveUser(user)) { user =>
                        complete(user)
                      }
                    }
                  }
                } ~
                get {
                  respondWithMediaType(`application/json`) {
                    userLog.debug(s"Loading all users")
                    onSuccess(loadUsers()) { users =>
                      complete(users)
                    }
                  }
                }
            } ~
              pathPrefix(IntNumber) { userId =>
                pathEnd {
                  options {
                    respondWithHeader(
                      Allow(HttpMethods.GET, HttpMethods.DELETE)) {
                      complete("")
                    }
                  } ~
                    get {
                      respondWithMediaType(`application/json`) {
                        userLog.debug(s"Loading user $userId")
                        onSuccess(loadUser(userId.toInt)) { user =>
                          complete(user)
                        }
                      }
                    } ~
                    delete {
                      respondWithStatus(OK) {
                        userLog.debug(s"Removing user $userId")
                        onSuccess(removeUser(userId.toInt)) { _ =>
                          complete("")
                        }
                      }
                    }
                } ~
                  pathPrefix(groupsEndpointName) {
                    pathEnd {
                      options {
                        respondWithHeader(
                          Allow(HttpMethods.GET, HttpMethods.POST)) {
                          complete("")
                        }
                      } ~
                        post {
                          respondWithStatus(Created) {
                            entity(as[UsersGroup]) { group =>
                              userLog.debug(s"Adding user $userId to group ${group.groupId}")
                              onSuccess(addGroup(userId, group.groupId)) { tmp =>
                                complete("")
                              }
                            }
                          }
                        } ~
                        get {
                          respondWithMediaType(`application/json`) {
                            userLog.debug(s"Loading groups of user $userId")
                            onSuccess(getUsersGroups(userId)) { groups =>
                              userLog.debug("completed groups of user")
                              complete(groups)
                            }
                          }
                        }
                    } ~
                      path(IntNumber) { groupId =>
                        options {
                          respondWithHeader(
                            Allow(HttpMethods.DELETE)) {
                            complete("")
                          }
                        } ~
                          delete {
                            respondWithStatus(OK) {
                              userLog.debug(s"Removing user $userId from group $groupId")
                              onSuccess(removeFromGroup(userId, groupId)) { _ =>
                                complete("")
                              }
                            }
                          }
                      }
                  }
              }
          }
      }
  }
}
