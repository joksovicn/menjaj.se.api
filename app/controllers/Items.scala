package controllers

import models._
import play.api._
import play.api.mvc._
import play.api.Play.current
import play.modules.reactivemongo._
import play.api.libs.iteratee.Iteratee

import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.bson.handlers.DefaultBSONHandlers._

object Items extends Controller with MongoController {
  val db = ReactiveMongoPlugin.db

  def get = Action { implicit request =>
    Async {
    	Ok("")
    }
  }
}