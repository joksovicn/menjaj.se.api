package models

import org.joda.time.DateTime
import reactivemongo.bson._
import reactivemongo.bson.handlers._

case class Item(id: Option[BSONObjectID],
                name: String,
                description: String,
                imageUrl: String,
                ownerId: String,
                status: ItemStatus.Value,
                creationDate: DateTime,
                updateDate: Option[DateTime])

object ItemStatus extends Enumeration {
  type ItemStatus = Value
  val INACTIVE = Value(0)
  val GIVEAWAY = Value(1)
  val EXCHANGE = Value(2)
}

object Item {
  implicit object ItemBSONReader extends BSONReader[Item] {
    def fromBSON(document: BSONDocument): Item = {
      val doc = document.toTraversable
      Item(
        doc.getAs[BSONObjectID]("_id"),
        doc.getAs[BSONString]("name").get.value,
        doc.getAs[BSONString]("description").get.value,
        doc.getAs[BSONString]("imageUrl").get.value,
        doc.getAs[BSONString]("ownerId").get.value,
        ItemStatus.apply(doc.getAs[BSONInteger]("status").get.value),
        doc.getAs[BSONDateTime]("creationDate").map(dt => new DateTime(dt.value)).get,
        doc.getAs[BSONDateTime]("updateDate").map(dt => new DateTime(dt.value)))
    }
  }

  implicit object ItemBSONWriter extends BSONWriter[Item] {
    def toBSON(item: Item) = {
      val bson = BSONDocument(
        "_id" -> item.id.getOrElse(BSONObjectID.generate),
        "name" -> BSONString(item.name),
        "description" -> BSONString(item.description),
        "imageUrl" -> BSONString(item.imageUrl),
        "ownerId" -> BSONString(item.ownerId),
        "status" -> BSONInteger(item.status.id),
        "creationDate" -> BSONDateTime(item.creationDate.getMillis))
      if (item.updateDate.isDefined)
        bson += "updateDate" -> BSONDateTime(item.updateDate.get.getMillis)
      bson
    }
  }
}