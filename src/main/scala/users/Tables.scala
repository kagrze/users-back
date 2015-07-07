package users

import slick.driver.H2Driver.api._
import slick.lifted.{TableQuery, ProvenShape}

class Users(tag: Tag) extends Table[(Int, String, String)](tag, "USERS") {

  // This is the primary key column:
  def id: Rep[Int] = column[Int]("USER_ID", O.PrimaryKey, O.AutoInc)

  def name: Rep[String] = column[String]("USER_NAME")

  def email: Rep[String] = column[String]("USER_EMAIL")

  // Every table needs a * projection with the same type as the table's type parameter
  def * : ProvenShape[(Int, String, String)] = (id, name, email)
}

class Groups(tag: Tag) extends Table[(Int, String)](tag, "GROUPS") {

  // This is the primary key column:
  def id: Rep[Int] = column[Int]("GROUP_ID", O.PrimaryKey, O.AutoInc)

  def name: Rep[String] = column[String]("GROUP_NAME")

  // Every table needs a * projection with the same type as the table's type parameter
  def * : ProvenShape[(Int, String)] = (id, name)
}

class UsersGroups(tag: Tag) extends Table[(Int, Int)](tag, "USERS_GROUPS") {
  def userId = column[Int]("USER_ID")

  def groupId = column[Int]("GROUP_ID")

  // Every table needs a * projection with the same type as the table's type parameter
  def * = (userId, groupId)

  // this table has a compound primary key
  def pk = primaryKey("PK_USERS_GROUPS", (userId, groupId))

  // and two foreign keys
  def user = foreignKey("FK_USERS_GROUPS_USERS", userId, TableQuery[Users])(_.id)

  def group = foreignKey("FK_USERS_GROUPS_GROUPS", groupId, TableQuery[Groups])(_.id)
}
