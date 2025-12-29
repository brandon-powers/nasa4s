package nasa4s.apps

import java.io.{BufferedOutputStream, FileOutputStream, OutputStream}

import cats.effect.{Blocker, ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Resource}
import cats.implicits._
import nasa4s.apod.{ApiKey, Apod}
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext

/** Exports APODs to the local filesystem
 *
 * @param apod                   downloads APODs
 * @param maxConcurrentDownloads number of concurrent APOD downloads
 * @param maxConcurrentExports   number of concurrent local APOD exports
 * @param blocker                blocking execution context */
class ApodLocalExporter[F[_]](apod: Apod[F], maxConcurrentDownloads: Int, maxConcurrentExports: Int, blocker: Blocker)(
  implicit F: ConcurrentEffect[F],
  cs: ContextShift[F]
) extends ApodExporter[F] {
  override def export(dates: List[String]): F[Unit] = {
    ApodExporter
      .parDownloadApodsWithDate[F](apod, dates, maxConcurrentDownloads)
      .parEvalMapUnordered(maxConcurrentExports) { case (apodData: Vector[Byte], date) =>
        val filename = s"apod-$date.jpg"
        val createOutputStream: F[OutputStream] = F.delay {
          new BufferedOutputStream(new FileOutputStream(filename))
        }

        F.delay(println(s"Exporting $filename...")) *>
          fs2.Stream
            .emits(apodData)
            .covary[F]
            .through {
              fs2.io.writeOutputStream[F](createOutputStream, blocker)
            }
            .compile
            .drain
      }.compile.drain
  }
}

object ApodLocalExporterApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val builder = BlazeClientBuilder[IO](ExecutionContext.global)
    val apiKey = ApiKey("<omitted>")

    val resources: Resource[IO, (org.http4s.client.Client[IO], Blocker)] = for {
      client <- builder.resource
      blocker <- Blocker[IO]
    } yield (client, blocker)

    resources
      .use { case (client, blocker) =>
        val apod = Apod[IO](client, apiKey)
        val exporter = new ApodLocalExporter[IO](apod, maxConcurrentDownloads = 3, maxConcurrentExports = 3, blocker)

        exporter.export(List("2020-03-01"))
      }
      .flatTap(_ => IO(println("Exiting...")))
      .as(ExitCode.Success)
  }
}
