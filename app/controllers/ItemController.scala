package controllers

import models._
import dao._

import play.api._
import play.api.Play.current
import play.api.mvc._
import play.api.Play._
import play.api.libs.json._

import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.bson.handlers.DefaultBSONHandlers._

import play.modules.reactivemongo._
import play.modules.reactivemongo.PlayBsonImplicits._

import org.joda.time.DateTime
import dao.JsonImplicits

object ItemController extends Controller with MongoController with JsonImplicits with Secured {
  val db = ReactiveMongoPlugin.db
  lazy val collection = db("items")

  def save(name: String, description: String, imageUrl: String) = IsAuthenticated {
    userId => request =>
      Async {
        val item = Item(Some(BSONObjectID.generate), name, description,
          imageUrl, userId, DateTime.now(), None)
        Logger.info("Crate item: %s" format item)

        collection.insert[Item](item).map(lastError =>
          lastError.ok match {
            case true =>
              val id = item.id.map(_.stringify).getOrElse("")
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
      collection.find[JsValue](
        QueryBuilder().query(BSONDocument()))(
        DefaultBSONReaderHandler, PrettyJsValueReader, ec).toList map {
        persons =>
          Ok(persons.foldLeft(JsArray(List()))((obj, person) => obj ++ Json.arr(person)))
      }
    }
  }
}