package models

import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime

case class Bump(id: Option[BSONObjectID],
                item: String,
                origin_user_id: String,
                destination_user_id: String,
                bumpDate: DateTime)

