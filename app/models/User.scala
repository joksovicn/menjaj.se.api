package models

import org.joda.time.DateTime
import reactivemongo.bson._
import reactivemongo.bson.handlers._

case class User(
  id: Option[BSONObjectID],
  name: String,
  email: String,
  externalId: String,
  creationDate: Option[DateTime],
  updateDate: Option[DateTime]
)