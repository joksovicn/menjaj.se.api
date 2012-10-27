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

import org.joda.time.DateTime
import tyrex.services.UUID

object UserController extends Controller with MongoController with JsonImplicits with Secured {
  val db = ReactiveMongoPlugin.db
  lazy val collection = db("users")

  implicit val read = User.UserBSONReader

  def save(name: String, email: String, externalId: String, avatarUrl: String) = Action {
    Async {
      val findQuery = QueryBuilder().query(Json.obj("externalId" -> externalId))
      val token = UUID.create()

      collection.find[User](findQuery).toList flatMap {
        items => items match {
          case x :: xs =>
            Logger.info("User found by external id: %s" format externalId)

            val modifier = x.copy(name = name, email = email, avatarUrl = avatarUrl,
              apiToken = token, updateDate = Some(DateTime.now()))

            collection.update(BSONDocument("_id" -> x.id.get), User.UserBSONWriter.toBSON(modifier)) map {
              lastError => lastError.ok match {
                case false =>
                  Logger.info("Mongo Error: %s" format
                    lastError.errMsg.getOrElse(""))
                  InternalServerError
                case true =>
                  Ok.withHeaders(("Location", "/users/%s" format x.id.
                    map(_.stringify).getOrElse("")), ("User-Token", x.apiToken))
              }
            }
          case Nil =>
            val user = User(Some(BSONObjectID.generate), name, email,
              externalId, avatarUrl, token, DateTime.now(), None)
            Logger.info("Create user: %s" format user)

            collection.insert[User](user) map {
              lastError => lastError.ok match {
                case false =>
                  Logger.info("Mongo Error: %s" format lastError.errMsg.getOrElse(""))
                  InternalServerError
                case true =>
                  val id = user.id.map(_.stringify).getOrElse("")
                  Logger.info("User created: %s" format id)
                  Created.withHeaders(("Location", "/users/%s" format id), ("User-Token", token))
              }
            }
        }
      }
    }
  }

  def get(id: String) = Action {
    Async {
      UserDao.findById(id).map {
        user => user match {
          case Some(json) => Ok(json)
          case None => NotFound
        }
      }
    }
  }

}
