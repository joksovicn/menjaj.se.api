package controllers

import models._
import dao._

import play.api._
import play.api.mvc._
import play.api.Play._
import play.api.libs.json._

import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import reactivemongo.core.commands.LastError

import play.modules.reactivemongo._
import play.modules.reactivemongo.PlayBsonImplicits._

import org.joda.time.DateTime
import scala.concurrent.Future

object BumpController extends Controller with MongoController with JsonImplicits with Secured {
  val db = ReactiveMongoPlugin.db
  lazy val collection = db("bumps")

  implicit val reader = Bump.BumpBSONReader

  def save(channelId: String, fromItemId: Option[String],
           toItemId: Option[String], toUserId: String) = IsAuthenticated {
    userId => request =>
      Async {
        val bump = Bump.apply(Some(BSONObjectID.generate),
          channelId, BumpParty(userId, fromItemId), BumpParty(toUserId, toItemId), DateTime.now())

        for {
          f1 <- collection.find[JsValue](QueryBuilder().
            query(BSONDocument("channelId" -> BSONString(channelId)))).headOption
          f2 <- if (f1.isEmpty) {
            for {
              _ <- collection.insert[Bump](bump)
              _ <- toItemId.map(id =>
                ItemDao.updateOwner(id, userId)).getOrElse(Future {
                LastError.apply(false, None, None, None, None)
              })
              _ <- fromItemId.map(id =>
                ItemDao.updateOwner(id, toUserId)).getOrElse(Future {
                LastError.apply(false, None, None, None, None)
              })
            } yield {
              true
            }
          } else {
            Future {
              false
            }
          }
        } yield {
          f2 match {
            case true =>
              Logger.info("Bump created: %s" format bump)
              Created
            case false =>
              Logger.warn("Bump already processed: %s" format bump)
              BadRequest
          }
        }
      }
  }
}