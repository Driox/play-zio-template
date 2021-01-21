package domain

import ai.x.play.json.Encoders._
import ai.x.play.json.Jsonx
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import tagging.AnyTypeclassTaggingCompat
import utils.json.AdtFormat
import utils.json.JsonOWritesOps._

trait JsonParser extends AnyTypeclassTaggingCompat {

  implicit def searchWithTotalSizeWrites[T](implicit f: Writes[T]): Writes[SearchWithTotalSize[T]] = (
    (__ \ "total_size").write[Int] and
      (__ \ "data").write[List[T]]
  )((s: SearchWithTotalSize[T]) => (s.total_size, s.data))

  implicit def searchWithTotalSizeReads[T](implicit f:  Reads[T]): Reads[SearchWithTotalSize[T]]   = (
    (__ \ "total_size").read[Int] and
      (__ \ "data").read[List[T]]
  )((a, b) => SearchWithTotalSize[T](a, b))

  implicit val emailFormat   = Json.valueFormat[Email]
  implicit val gender_format = AdtFormat.format(Gender.values)

  implicit val table_search_format = Jsonx.formatCaseClassUseDefaults[TableSearch]

  private[this] val user_reads  = Jsonx.formatCaseClassUseDefaults[User]
  private[this] val user_writes = user_reads.removeField("password")
  implicit val user_format      = Format(user_reads, user_writes)
}
