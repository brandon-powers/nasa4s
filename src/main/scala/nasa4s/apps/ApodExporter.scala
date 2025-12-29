package nasa4s.apps

import cats.effect.ConcurrentEffect
import nasa4s.apod.Apod

trait ApodExporter[F[_]] {
  /** Exports a batch of APODs to a target destination */
  def export(dates: List[String]): F[Unit]
}

object ApodExporter {
  /** Downloads APODs in parallel, returning bytes paired with their original date.
   *
   * @param apod                   the APOD client
   * @param dates                  list of dates to download
   * @param maxConcurrentDownloads maximum concurrent downloads
   * @return stream of (bytes, date) pairs */
  def parDownloadApodsWithDate[F[_]](
    apod: Apod[F],
    dates: List[String],
    maxConcurrentDownloads: Int
  )(implicit F: ConcurrentEffect[F]): fs2.Stream[F, (Vector[Byte], String)] =
    fs2.Stream
      .emits(dates)
      .evalTap(date => F.delay(println(s"Downloading APOD for $date...")))
      .covary[F]
      .parEvalMapUnordered(maxConcurrentDownloads) { date =>
        apod.download(date).compile.toVector.map(bytes => (bytes, date))
      }
}