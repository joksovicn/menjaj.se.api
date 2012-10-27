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

object ItemController extends Controller with MongoController with JsonImplicits with Secured {
  val db = ReactiveMongoPlugin.db
  lazy val collection = db("items")

  def save(name: String, description: String, status: Int, imageUrl: String) = IsAuthenticated {
    userId => request =>
      Async {
        val item = Item(Some(BSONObjectID.generate), name, description,
          imageUrl, userId, ItemStatus.apply(status), DateTime.now(), None)
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

  def get(id: String) = Action {
    Async {
      ItemDao.findById(id).map {
        user => user match {
          case Some(json) => Ok(json)
          case None => NotFound
        }
      }
    }
  }

  def find(query: String, page: Int, count: Int) = Action {
    Async {
      collection.find[JsValue](QueryBuilder().query(
        BSONDocument("name" -> BSONRegex(".*%s.*" format query, "i"))),
        QueryOpts().skip(page * count).batchSize(count).exhaust
      )(DefaultBSONReaderHandler, PrettyJsValueReader, ec).toList map {
        items =>
          Ok(items.foldLeft(JsArray(List()))((obj, item) => obj ++ Json.arr(item)))
      }
    }
  }
}