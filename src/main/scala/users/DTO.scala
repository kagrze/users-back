package users

/**
 * Simplistic user DTO
 */
case class User(id: Option[Int], name: String, email: String)

/**
 * Simplistic user DTO
 */
case class Group(id: Option[Int], name: String)

/**
  * Simplistic user's group DTO
  */
case class UsersGroup(groupId: Int)
