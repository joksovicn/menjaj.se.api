package dao

import play.api.libs.json._
import reactivemongo.bson._
import reactivemongo.bson.handlers._
import reactivemongo.utils.Converters

trait JsonImplicits {
  def toTuple(e: BSONElement): (String, JsValue) = e.name -> (e.value match {
    case BSONDouble(value) => JsNumber(value)
    case BSONString(value) => JsString(value)
    case traversable: TraversableBSONDocument => JsObjectReader.read(traversable.toBuffer)
    case doc: AppendableBSONDocument => JsObjectReader.read(doc.toTraversable.toBuffer)
    case array: TraversableBSONArray => {
      array.bsonIterator.foldLeft(Json.arr()) {
        (acc: JsArray, e: BSONElement) => acc :+ toTuple(e)._2
      }
    }
    case array: AppendableBSONArray => JsArrayReader.read(array)
    case oid@BSONObjectID(value) => JsString(oid.stringify)
    case BSONBoolean(value) => JsBoolean(value)
    case BSONDateTime(value) => JsNumber(value)
    case BSONTimestamp(value) => Json.obj("$time" -> value.toInt, "i" -> (value >>> 4))
    case BSONRegex(value, flags) => Json.obj("$regex" -> value, "$options" -> flags)
    case BSONNull => JsNull
    case BSONUndefined => JsUndefined("")
    case BSONInteger(value) => JsNumber(value)
    case BSONLong(value) => JsNumber(value)
    case BSONBinary(value, subType) =>
      val arr = new Array[Byte](value.readableBytes())
      value.readBytes(arr)
      Json.obj(
        "$binary" -> Converters.hex2Str(arr),
        "$type" -> Converters.hex2Str(Array(subType.value.toByte))
      )
    case BSONDBPointer(value, id) => Json.obj("$ref" -> value, "$id" -> Converters.hex2Str(id))
    // NOT STANDARD AT ALL WITH JSON and MONGO
    case BSONJavaScript(value) => Json.obj("$js" -> value)
    case BSONSymbol(value) => Json.obj("$sym" -> value)
    case BSONJavaScriptWS(value) => Json.obj("$jsws" -> value)
    case BSONMinKey => Json.obj("$minkey" -> 0)
    case BSONMaxKey => Json.obj("$maxkey" -> 0)
  })

  object JsArrayReader {
    def read(array: BSONArray): JsArray = {
      val it = array.toTraversable.bsonIterator

      it.foldLeft(Json.arr()) {
        (acc: JsArray, e: BSONElement) => acc :+ toTuple(e)._2
      }
    }
  }

  object JsObjectReader extends BSONReader[JsObject] {
    def fromBSON(doc: BSONDocument): JsObject = {
      JsObject(doc.toTraversable.bsonIterator.foldLeft(List[(String, JsValue)]()) {
        (acc, e) => acc :+ toTuple(e)
      })
    }
  }

  object PrettyJsValueReader extends BSONReader[JsValue] {
    def fromBSON(doc: BSONDocument): JsValue = JsObjectReader.fromBSON(doc)
  }
}