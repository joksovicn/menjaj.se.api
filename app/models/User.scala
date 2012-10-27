package models

import org.joda.time.DateTime
import reactivemongo.bson._
import reactivemongo.bson.handlers._

case class User(
  id: Option[BSONObjectID],
  name: String,
  email: String,
  externalId: String,
  avatarUrl: String,
  apiToken: String,
  creationDate: DateTime,
  updateDate: Option[DateTime]
)

object User {
  implicit object UserBSONReader extends BSONReader[User] {
    def fromBSON(document: BSONDocument): User = {
      val doc = document.toTraversable
      User(
        doc.getAs[BSONObjectID]("_id"),
        doc.getAs[BSONString]("name").get.value,
        doc.getAs[BSONString]("email").get.value,
        doc.getAs[BSONString]("externalId").get.value,
        doc.getAs[BSONString]("avatarUrl").get.value,
        doc.getAs[BSONString]("apiToken").get.value,
        doc.getAs[BSONDateTime]("creationDate").map(dt => new DateTime(dt.value)).get,
        doc.getAs[BSONDateTime]("updateDate").map(dt => new DateTime(dt.value)))
    }
  }

  implicit object UserBSONWriter extends BSONWriter[User] {
    def toBSON(user: User) = {
      val bson = BSONDocument(
        "_id" -> user.id.getOrElse(BSONObjectID.generate),
        "name" -> BSONString(user.name),
        "email" -> BSONString(user.email),
        "externalId" -> BSONString(user.externalId),
        "avatarUrl" -> BSONString(user.avatarUrl),
        "apiToken" -> BSONString(user.apiToken),
        "creationDate" -> BSONDateTime(user.creationDate.getMillis))
      if (user.updateDate.isDefined)
        bson += "updateDate" -> BSONDateTime(user.updateDate.get.getMillis)
      bson
    }
  }
}