package controllers

import play.api._
import play.api.mvc.Controller
import play.api.Play
import play.api.Play.current
import play.modules.reactivemongo._

import scala.concurrent.util._
import scala.concurrent.util.Duration._
import java.util.concurrent.TimeUnit

trait StorageController extends Controller with MongoController {

  implicit val timeout = Duration(30, TimeUnit.SECONDS)

  lazy val storage = {
    val underlying = ReactiveMongoPlugin.db
    underlying.authenticate(Play.application.configuration.getString("mongodb.username").getOrElse(""),
      Play.application.configuration.getString("mongodb.password").getOrElse(""))
    underlying
  }
}
