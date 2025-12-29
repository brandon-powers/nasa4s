package nasa4s.apod

/** Represents a NASA developer key.
 * The toString method masks the value to prevent accidental exposure in logs.
 *
 * @see [[https://api.nasa.gov/]] Authentication */
final case class ApiKey(value: String) {
  override def toString: String = "ApiKey(****)"
}
