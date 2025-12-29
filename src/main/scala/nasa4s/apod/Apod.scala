package nasa4s.apod

import cats.effect.Sync
import fs2.Stream
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.{EntityDecoder, Method, Request, Uri}

/** Wraps the Astronomy Picture of the Day (APOD) API
 *
 * @see [[https://api.nasa.gov/]] APOD */
trait Apod[F[_]] {
  /** Fetches APOD metadata for a given date.
   *
   * @param date The date of an APOD
   * @param hd   To work with an HD or non-HD version of an APOD; defaults to true */
  def call(date: String, hd: Boolean = true): F[Apod.Response]

  /** Downloads the bytes of an APOD as a stream, removing
   * the need to request the metadata before downloading.
   *
   * @param date The date of an APOD
   * @param hd   To work with an HD or non-HD version of an APOD; defaults to true
   * */
  def download(date: String, hd: Boolean = true): Stream[F, Byte]
}

object Apod {
  private val baseUri = Uri.uri("https://api.nasa.gov/planetary/apod")

  /** APOD API response with idiomatic Scala camelCase field names.
   * JSON snake_case fields are automatically mapped via custom codecs. */
  case class Response(
    copyright: Option[String],
    date: String,
    explanation: String,
    hdUrl: Option[String],
    mediaType: String,
    serviceVersion: String,
    title: String,
    url: String
  )

  object Response {
    implicit val decoder: Decoder[Response] = Decoder.forProduct8(
      "copyright", "date", "explanation", "hdurl", "media_type", "service_version", "title", "url"
    )(Response.apply)

    implicit val encoder: Encoder[Response] = deriveEncoder[Response]
  }

  def apply[F[_] : Sync](client: Client[F], apiKey: ApiKey): Apod[F] = new ApodImpl[F](client, apiKey)

  private class ApodImpl[F[_] : Sync](client: Client[F], apiKey: ApiKey) extends Apod[F] {
    implicit val responseEntityDecoder: EntityDecoder[F, Response] = jsonOf[F, Response]

    override def call(date: String, hd: Boolean): F[Response] = {
      val uri = baseUri
        .withQueryParam("date", date)
        .withQueryParam("hd", hd)
        .withQueryParam("api_key", apiKey.value)
      val request = Request[F](Method.GET, uri)

      client.expect[Response](request)
    }

    override def download(date: String, hd: Boolean): Stream[F, Byte] = {
      Stream
        .eval(call(date, hd))
        .filter(_.mediaType == "image")
        .flatMap { response: Response =>
          val imageUrl = response.hdUrl.filter(_ => hd).getOrElse(response.url)
          val request = Request[F](Method.GET, Uri.unsafeFromString(imageUrl))

          client.stream(request).flatMap(_.body)
        }
    }
  }

}