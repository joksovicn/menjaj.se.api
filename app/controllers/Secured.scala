package controllers

import play.api.mvc._
import play.api.mvc.Results._

import dao.UserDao

import scala.concurrent.ExecutionContext.Implicits.global

trait Secured {
  private def tokenHandler(request: RequestHeader) = request.headers.get("User-Token")

  private def onUnauthorized(request: RequestHeader) = Results.Unauthorized

  def IsAuthenticated(f: => String => Request[AnyContent] => Result) =
    Security.Authenticated(tokenHandler, onUnauthorized) {
      token =>
        Action(request =>
          Async {
            UserDao.findByToken(token) map {
              userOpt =>
                println(userOpt)
                userOpt match {
                case Some(user) =>
                  f(user.id.get.stringify)(request)
                case None =>
                  Unauthorized
              }
            }
          })
    }
}
