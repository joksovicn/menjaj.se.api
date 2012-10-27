package controllers

import play.api.mvc._

trait Secured {
  private def token(request: RequestHeader) = request.headers.get("User-Token")

  private def onUnauthorized(request: RequestHeader) = Results.Unauthorized

  def IsAuthenticated(f: => String => Request[AnyContent] => Result) =
    Security.Authenticated(token, onUnauthorized) {
      user =>
        Action(request => f(user)(request))
    }
}