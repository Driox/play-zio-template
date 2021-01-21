package domain

sealed trait Gender extends Product with Serializable

object Gender {

  final case object MEN   extends Gender
  final case object WOMEN extends Gender

  val values = Set(MEN, WOMEN)
  def parse(s: String): Option[Gender] = values.filter(_.productPrefix == s).headOption
}
