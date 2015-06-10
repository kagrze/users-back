package users

/**
 * Persistence service
 */
trait PersistenceService {
  def save(user: User)
  def load(name: String) : User
  def remove(name: String)
}

/**
 * Simple in-memory map based persistence service
 */
trait FakePersistenceService extends PersistenceService{
  var users : Map[String, User] = Map("John Smith" -> User("John Smith", "john@smith.mx"))

  def save(user: User) = users = users + (user.name -> user)
  def load(name: String) : User = users(name)
  def remove(name: String) = users = users - name
}

