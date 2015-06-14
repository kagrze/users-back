package users

import java.net.URLEncoder

import akka.event.Logging
import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http.StatusCodes.{Created, OK}

class UserServiceSpec extends Specification with Specs2RouteTest with UserService with SlickPersistenceService {
  def actorRefFactory = system
  def userLog = Logging(system, "test logger")

  "An user service" should  {
    "allow insertion, retrieval and removal of users" in {
      import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
      import spray.httpx.SprayJsonSupport.sprayJsonUnmarshaller
      import UserJsonProtocol._

      val userName = "John Smith"
      val testUser = User(None, userName, "john@smith.mx")

      Post("/invitation", testUser) ~> usersRoute ~> check {
        status === Created
      }

      val encodedUserName = URLEncoder.encode(userName,"UTF-8")

      Get(s"/invitation?name=$encodedUserName") ~> usersRoute ~> check {
        responseAs[List[User]] === List(testUser)
        status === OK
      }

      Delete(s"/invitation?name=$encodedUserName") ~> usersRoute ~> check {
        status === OK
      }
    }
  }
}