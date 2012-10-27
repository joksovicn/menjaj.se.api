package models

import reactivemongo.bson._
import reactivemongo.bson.handlers._
import org.joda.time.DateTime

case class Bump(id: Option[BSONObjectID],
                channelId: String,
                from: BumpParty,
                to: BumpParty,
                creationDate: DateTime)

case class BumpParty(userId: String, itemId: Option[String])

object Bump {

  implicit object BumpBSONReader extends BSONReader[Bump] {
    def fromBSON(document: BSONDocument): Bump = {
      val doc = document.toTraversable
      Bump(
        doc.getAs[BSONObjectID]("_id"),
        doc.getAs[BSONString]("channelId").get.value,
        BumpParty(doc.getAs[BSONString]("fromUserId").get.value,
          doc.getAs[BSONString]("fromItemId").map(_.value)),
        BumpParty(doc.getAs[BSONString]("toUserId").get.value,
          doc.getAs[BSONString]("toItemId").map(_.value)),
        doc.getAs[BSONDateTime]("creationDate").map(dt => new DateTime(dt)).get
      )
    }
  }

  implicit object BumpBSONWriter extends BSONWriter[Bump] {
    def toBSON(bump: Bump) = {
      val bson = BSONDocument(
        "_id" -> bump.id.getOrElse(BSONObjectID.generate),
        "channelId" -> BSONString(bump.channelId),
        "fromUserId" -> BSONString(bump.from.userId),
        "toUserId" -> BSONString(bump.to.userId),
        "creationDate" -> BSONDateTime(bump.creationDate.getMillis))
      if (bump.from.itemId.isDefined)
        bson += "fromItemId" -> BSONString(bump.from.itemId.get)
      if (bump.to.itemId.isDefined)
        bson += "toItemId" -> BSONString(bump.to.itemId.get)
      bson
    }
  }

}

