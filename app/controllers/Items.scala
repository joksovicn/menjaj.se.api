package controllers

import models._

import play.api._
import play.api.mvc._

import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.bson.handlers.DefaultBSONHandlers._

import play.api.Play.current
import play.api.libs.json._

import play.modules.reactivemongo._
import play.modules.reactivemongo.PlayBsonImplicits._
import org.joda.time.DateTime

object Items extends Controller with MongoController with JsonImplicits {
  val db = ReactiveMongoPlugin.db
  lazy val collection = db("items")

  def create(name: String, description: String) = Action {
    Async {
      val item = Item(Some(BSONObjectID.generate), name, description,
        "user_id", Some(DateTime.now()), Some(DateTime.now()))
      val id = item.id.map(_.stringify).getOrElse("")

      collection.insert[Item](item).map(lastError =>
        lastError.ok match {
          case true =>
            Logger.info("Item created: %s" format id)
            Created.withHeaders(("Location", "/items/%s" format id))
          case false =>
            Logger.info("Mongo Error: %s" format lastError.errMsg.getOrElse(""))
            InternalServerError
        }
      )
    }
  }


  def retrieve() = Action {
    Async {
      val qb = QueryBuilder().query(BSONDocument())

      collection.find[JsValue](qb)(DefaultBSONReaderHandler, PrettyJsValueReader, ec).toList.map {
        persons =>
          Ok(persons.foldLeft(JsArray(List()))((obj, person) => obj ++ Json.arr(person)))
      }
    }
  }
}