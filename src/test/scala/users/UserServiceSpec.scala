package users

import akka.event.Logging
import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http.StatusCodes.{Created, OK, NotAcceptable}

class UserServiceSpec extends Specification with Specs2RouteTest with UserService with SlickPersistenceService {
  def actorRefFactory = system

  def exeContext = system.dispatcher

  def userLog = Logging(system, "test logger")

  "An entry endpoint" should {
    "list available operations" in {
      Get("/") ~> jointRoute ~> check {
        status === OK
      }

      Get("/") ~> addHeader("Accept", "text/html") ~> jointRoute ~> check {
        status === OK
      }

      Get("/") ~> addHeader("Accept", "application/json") ~> jointRoute ~> check {
        status === OK
      }

      Get("/") ~> addHeader("Accept", "application/pdf") ~> sealRoute(jointRoute) ~> check {
        status === NotAcceptable
      }
    }
  }

  "A group service" should {
    "allow insertion, retrieval and removal of groups" in {
      import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
      import spray.httpx.SprayJsonSupport.sprayJsonUnmarshaller
      import UserJsonProtocol._

      val testGroup = Group(None, "Main group")

      Post("/" + groupsEndpointName, testGroup) ~> jointRoute ~> check {
        val createdGroup = responseAs[Group]
        createdGroup.name === testGroup.name
        status === Created

        Get("/" + groupsEndpointName) ~> jointRoute ~> check {
          responseAs[List[Group]].size === 2
          status === OK
        }

        val groupPath = "/" + groupsEndpointName + "/" + createdGroup.id.get

        Get(groupPath) ~> jointRoute ~> check {
          responseAs[Group] === createdGroup
          status === OK
        }

        Delete(groupPath) ~> jointRoute ~> check {
          status === OK
        }
      }
    }
  }

  "An user service" should {
    "allow insertion, retrieval and removal of users" in {
      import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
      import spray.httpx.SprayJsonSupport.sprayJsonUnmarshaller
      import UserJsonProtocol._

      val testUser = User(None, "John Smith 1", "john@smith.mx")

      Post("/" + usersEndpointName, testUser) ~> jointRoute ~> check {
        val createdUser = responseAs[User]
        createdUser.name === testUser.name
        createdUser.email === testUser.email
        status === Created

        Get("/" + usersEndpointName) ~> jointRoute ~> check {
          responseAs[List[User]].size >= 2
          status === OK
        }

        val userPath = "/" + usersEndpointName + "/" + createdUser.id.get

        Get(userPath) ~> jointRoute ~> check {
          responseAs[User] === createdUser
          status === OK
        }

        Delete(userPath) ~> jointRoute ~> check {
          status === OK
        }
      }
    }
  }

  "It" should {
    "be possible to associate users with groups" in {
      import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
      import spray.httpx.SprayJsonSupport.sprayJsonUnmarshaller
      import UserJsonProtocol._

      val testUser = User(None, "John Smith 2", "john@smith.mx")

      Post("/" + usersEndpointName, testUser) ~> jointRoute ~> check {
        val createdUser = responseAs[User]
        status === Created

        val userPath = "/" + usersEndpointName + "/" + createdUser.id.get + "/" + groupsEndpointName

        Get(userPath) ~> jointRoute ~> check {
          responseAs[List[Int]] === List()
          status === OK
        }

        val defaultGroupId = 1

        Post(userPath, Group(Some(1), "")) ~> jointRoute ~> check {
          status === Created
        }

        Get(userPath) ~> jointRoute ~> check {
          responseAs[List[Int]] === List(defaultGroupId)
          status === OK
        }

        Delete(userPath + "/" + defaultGroupId) ~> jointRoute ~> check {
          status === OK
        }
      }
    }
  }
}