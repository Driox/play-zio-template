package domain

class Email(val underlying: String) extends AnyVal {
  override def toString: String = underlying
}
