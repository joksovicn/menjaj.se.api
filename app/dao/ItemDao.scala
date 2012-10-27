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

object ItemDao extends JsonImplicits {
  val db = ReactiveMongoPlugin.db
  lazy val collection = db("items")

  implicit val reader = User.UserBSONReader

  def findById(id: String) =
    collection.find[JsValue](
      QueryBuilder().query(
        BSONDocument("_id" -> BSONObjectID(id)))
    )(
      DefaultBSONReaderHandler, PrettyJsValueReader, global
    ).headOption

  def updateOwner(id: String, ownerId: String) = {
    println(id + "---->" + ownerId)
    collection.update(BSONDocument("_id" -> BSONObjectID(id)),
      BSONDocument("$set" -> BSONDocument("ownerId" -> BSONString(ownerId))))
  }
}