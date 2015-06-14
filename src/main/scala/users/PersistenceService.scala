package users

/**
 * Persistence service
 */
trait PersistenceService {
  def save(user: User)
  def load(name: String) : User
  def remove(name: String)
}

trait SlickPersistenceService extends PersistenceService{
  import slick.lifted.TableQuery
  import slick.driver.H2Driver.api._
  import scala.concurrent.Await
  import scala.concurrent.duration.DurationLong
  import scala.concurrent.ExecutionContext.Implicits.global

  val db = Database.forConfig("h2mem1")

  val users: TableQuery[Users] = TableQuery[Users]

  val usersWithoutId = users.map(u => (u.name, u.email))

  val setupAction: DBIO[Unit] = DBIO.seq(
    // Create the schema
    users.schema.create,

    // Insert some suppliers
    usersWithoutId += ("John Smith", "john@smith.mx")
  )

  db.run(setupAction).map(_ => println(s"Setup completed"))

  def save(user: User) = {
    db.run(usersWithoutId += (user.name, user.email)).map(result => println(s"Number of records inserted: $result"))
  }

  def load(name: String) : User = {
    val f1 = db.run(users.filter(_.name === name).result).map(userSeq => {
      println(s"Number of records loaded: ${userSeq.size}")
      val singleUser = userSeq.head
      User(Some(singleUser._1), singleUser._2, singleUser._3)})
    Await.result(f1, 1 second)  //TODO: make load method asynchronous
  }

  def remove(name: String) = {
    db.run(users.filter(_.name === name).delete).map(result => println(s"Number of records deleted: $result"))
  }
}

/**
 * Simple in-memory map based persistence service
 */
trait FakePersistenceService extends PersistenceService{
  var users : Map[String, User] = Map("John Smith" -> User(None,"John Smith", "john@smith.mx"))

  def save(user: User) = users = users + (user.name -> user)
  def load(name: String) : User = users(name)
  def remove(name: String) = users = users - name
}

