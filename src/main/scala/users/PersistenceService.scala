package users

import akka.event.LoggingAdapter

import scala.concurrent.Future

/**
 * Persistence service
 */
trait PersistenceService {
  def saveGroup(user: Group): Future[Group]

  def loadGroup(id: Int): Future[Group]

  def loadGroups(): Future[Seq[Group]]

  def removeGroup(id: Int): Future[Unit]

  def saveUser(user: User): Future[User]

  def loadUser(id: Int): Future[User]

  def loadUsers(): Future[Seq[User]]

  def removeUser(id: Int): Future[Unit]

  def getUsersGroups(userId: Int): Future[Seq[UsersGroup]]

  def addGroup(userId: Int, groupId: Int): Future[Unit]

  def removeFromGroup(userId: Int, groupId: Int): Future[Unit]
}

/**
 * FRM based persistence service
 */
trait SlickPersistenceService extends PersistenceService {

  import slick.lifted.TableQuery
  import slick.driver.H2Driver.api._
  import scala.concurrent.ExecutionContext.Implicits.global

  def userLog: LoggingAdapter

  val db = Database.forConfig("h2mem1")

  val groups = TableQuery[Groups]
  val users = TableQuery[Users]
  val usersGroups = TableQuery[UsersGroups]

  val groupsReturningId = groups returning groups.map(_.id)
  val usersReturningId = users returning users.map(_.id)

  val setupAction: DBIO[Unit] = DBIO.seq(
    // create the schema
    users.schema.create,
    groups.schema.create,
    usersGroups.schema.create,

    // insert a test group and user
    groups +=(-1, "All users group"),
    users +=(-1, "John Smith", "john@smith.mx"),
    usersGroups +=(1, 1) //ids start from 1 so we know that they are 1 and 1
  )

  db.run(setupAction).map(_ => userLog.debug(s"Setup completed"))

  def saveGroup(group: Group) = db
    .run(groupsReturningId +=(-1, group.name))
    .map(newId => {
    userLog.debug(s"A new record inserted with id: $newId")
    Group(Some(newId), group.name)
  })

  def loadGroup(id: Int) = db
    .run(groups.filter(_.id === id).result)
    .map(groupSeq => {
    userLog.debug(s"Number of records loaded: ${groupSeq.size}")
    val singleGroup = groupSeq.head
    Group(Some(singleGroup._1), singleGroup._2)
  })

  def loadGroups() = db
    .run(groups.result).map(groupSeq => {
    userLog.debug(s"Number of records loaded: ${groupSeq.size}")
    groupSeq.map(singleGroup => Group(Some(singleGroup._1), singleGroup._2))
  })

  def removeGroup(id: Int) = db
    .run(groups.filter(_.id === id).delete)
    .map(result => userLog.debug(s"Number of records deleted: $result"))

  def saveUser(user: User) = db
    .run(usersReturningId +=(-1, user.name, user.email))
    .map(newId => {
    userLog.debug(s"A new record inserted with id: $newId")
    User(Some(newId), user.name, user.email)
  })

  def loadUser(id: Int) = db
    .run(users.filter(_.id === id).result)
    .map(userSeq => {
    userLog.debug(s"Number of records loaded: ${userSeq.size}")
    val singleUser = userSeq.head
    User(Some(singleUser._1), singleUser._2, singleUser._3)
  })

  def loadUsers() = db
    .run(users.result).map(userSeq => {
    userLog.debug(s"Number of records loaded: ${userSeq.size}")
    userSeq.map(singleUser => User(Some(singleUser._1), singleUser._2, singleUser._3))
  })

  def removeUser(id: Int) = db
    .run(users.filter(_.id === id).delete)
    .map(result => userLog.debug(s"Number of records deleted: $result"))

  def getUsersGroups(userId: Int): Future[Seq[UsersGroup]] = db
    .run(usersGroups.filter(_.userId === userId).result)
    .map(usersGroupsSeq => {
    userLog.debug(s"returning groups for user $userId")
    usersGroupsSeq.map(ug => UsersGroup(ug._2))
  })

  def addGroup(userId: Int, groupId: Int) = db
    .run(usersGroups +=(userId, groupId))
    .map(tmp => userLog.debug("Added a group to a user"))

  def removeFromGroup(userId: Int, groupId: Int) = db
    .run(usersGroups.filter(ug => ug.userId === userId && ug.groupId === groupId).delete)
    .map(result => userLog.debug(s"Number of records deleted: $result"))
}
