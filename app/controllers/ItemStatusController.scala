package controllers

import models._
import dao._

import play.api._
import play.api.Play.current
import play.api.mvc._
import play.api.libs.json._

import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.bson.handlers.DefaultBSONHandlers._

import play.modules.reactivemongo._
import play.modules.reactivemongo.PlayBsonImplicits._

import scala.concurrent.Future

object ItemStatusController extends Controller with MongoController with JsonImplicits with Secured {
  val db = ReactiveMongoPlugin.db
  lazy val collection = db("items")

  implicit val reader = Item.ItemBSONReader
  implicit val writer = Item.ItemBSONWriter

  def save(id: String, value: Int) = IsAuthenticated {
    userId => request =>
      Async {
        val docId = BSONDocument("_id" -> BSONObjectID(id))
        val query = QueryBuilder().query(docId)

        collection.find[Item](query).toList flatMap {
          items =>
            val item = items.head
            if (item.ownerId == userId) {
              val modifier = item.copy(
                status = ItemStatus.apply(value))

              collection.update(docId, modifier) map {
                lastError => lastError.ok match {
                  case false =>
                    Logger.info("Mongo Error: %s" format
                      lastError.errMsg.getOrElse(""))
                    InternalServerError
                  case true =>
                    Ok
                }
              }
            } else {
              Future {
                Unauthorized
              }
            }
        }
      }
  }
}
