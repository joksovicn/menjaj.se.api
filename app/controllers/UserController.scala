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
import com.eaio.uuid.UUIDGen

class UserController extends StorageController with JsonImplicits with Secured {
  lazy val collection = storage("users")

  def create(name: String, email: String, externalId: String, avatarUrl: String) = Action {
    Async {
      val token = String.valueOf(UUIDGen.getClockSeqAndNode)
      val user = User(Some(BSONObjectID.generate), name, email,
        externalId, avatarUrl, token, DateTime.now(), None)
      val id = user.id.map(_.stringify).getOrElse("")

      collection.insert[User](user).map(lastError =>
        lastError.ok match {
          case true =>
            Logger.info("User created: %s" format id)
            Created.withHeaders(("Location", "/users/%s" format id), ("User-Token", token))
          case false =>
            Logger.info("Mongo Error: %s" format lastError.errMsg.getOrElse(""))
            InternalServerError
        }
      )
    }
  }


  def retrieve(id: String) = Action {
    Async {
      val qb = QueryBuilder().query(BSONDocument())

      collection.find[JsValue](qb)(DefaultBSONReaderHandler, PrettyJsValueReader, ec).toList.map {
        persons =>
          Ok(persons.foldLeft(JsArray(List()))((obj, person) => obj ++ Json.arr(person)))
      }
    }
  }
}
