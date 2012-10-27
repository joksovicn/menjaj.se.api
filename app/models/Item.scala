package models

import org.joda.time.DateTime
import reactivemongo.bson._
import reactivemongo.bson.handlers._

import play.api.libs.json._

case class Item(id: Option[BSONObjectID],
                name: String,
                description: String,
                publisher: String,
                creationDate: Option[DateTime],
                updateDate: Option[DateTime])

object Item {

  implicit object ItemBSONReader extends BSONReader[Item] {
    def fromBSON(document: BSONDocument): Item = {
      val doc = document.toTraversable
      Item(
        doc.getAs[BSONObjectID]("_id"),
        doc.getAs[BSONString]("name").get.value,
        doc.getAs[BSONString]("description").get.value,
        doc.getAs[BSONString]("publisher").get.value,
        doc.getAs[BSONDateTime]("creationDate").map(dt => new DateTime(dt.value)),
        doc.getAs[BSONDateTime]("updateDate").map(dt => new DateTime(dt.value)))
    }
  }

  implicit object ItemJSONReader extends Reads[Item] {
    def reads(json: JsValue) = null
  }

  implicit object ItemBSONWriter extends BSONWriter[Item] {
    def toBSON(item: Item) = {
      val bson = BSONDocument(
        "_id" -> item.id.getOrElse(BSONObjectID.generate),
        "name" -> BSONString(item.name),
        "description" -> BSONString(item.description),
        "publisher" -> BSONString(item.publisher))
      if (item.creationDate.isDefined)
        bson += "creationDate" -> BSONDateTime(item.creationDate.get.getMillis)
      if (item.updateDate.isDefined)
        bson += "updateDate" -> BSONDateTime(item.updateDate.get.getMillis)
      bson
    }
  }

}