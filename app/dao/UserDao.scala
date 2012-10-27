package dao

import models._

import play.api._
import play.api.Play.current
import play.api.mvc._
import play.api.libs.json._

import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.bson.handlers.DefaultBSONHandlers._

import play.modules.reactivemongo._
import play.modules.reactivemongo.PlayBsonImplicits._

import scala.concurrent.ExecutionContext.Implicits.global

object UserDao extends JsonImplicits {
  val db = ReactiveMongoPlugin.db
  lazy val collection = db("users")

  implicit val reader = User.UserBSONReader

  def findByToken(token: String) =
    collection.find[User](
      QueryBuilder().query(
        BSONDocument("apiToken" -> BSONString(token))
      )
    ).headOption
}
