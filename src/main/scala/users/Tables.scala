package users

import slick.driver.H2Driver.api._
import slick.lifted.ProvenShape

class Users(tag: Tag) extends Table[(Int, String, String)](tag, "USERS"){

  // This is the primary key column:
  def id: Rep[Int] = column[Int]("SUP_ID", O.PrimaryKey, O.AutoInc)
  def name: Rep[String] = column[String]("SUP_NAME")
  def email: Rep[String] = column[String]("SUP_EMAIL")

  // Every table needs a * projection with the same type as the table's type parameter
  def * : ProvenShape[(Int, String, String)] = (id,name, email)
}