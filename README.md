# nasa4s

A functional Scala library providing type-safe wrappers around [NASA's Open APIs](https://api.nasa.gov/).

[![Scala Version](https://img.shields.io/badge/scala-2.12.10-red.svg)](https://www.scala-lang.org/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

## Overview

`nasa4s` brings NASA's wealth of space data to the Scala ecosystem with a functional, type-safe API built on modern libraries like Cats Effect, http4s, and FS2. Currently supports the Astronomy Picture of the Day (APOD) API with plans to expand to additional NASA services.

### Features

- **Type-Safe**: Leverages Scala's type system and Circe for automatic JSON parsing
- **Functional**: Built with Cats Effect for composable, referentially transparent effects
- **Streaming**: Uses FS2 for efficient, resource-safe streaming downloads
- **Concurrent**: Built-in support for parallel downloads with configurable concurrency
- **Flexible Storage**: Integration with fs2-blobstore for local, SFTP, and S3 exports

## Installation

Add the following to your `build.sbt`:

```scala
libraryDependencies += "nasa4s" %% "nasa4s" % "0.1"
```

> **Note**: This library is currently in early development (v0.1). Not yet published to Maven Central.

## Getting Started

### NASA API Key

You'll need a NASA API key to use this library:

1. Visit [https://api.nasa.gov/](https://api.nasa.gov/)
2. Fill out the simple signup form
3. Your API key will be emailed to you instantly
4. For testing, you can use the demo key: `DEMO_KEY` (rate-limited)

## Usage

### Basic APOD Metadata Retrieval

Fetch metadata for a specific Astronomy Picture of the Day:

```scala
import cats.effect.IO
import nasa4s.apod.{ApiKey, Apod}
import org.http4s.client.Client
import io.circe.syntax._

val client: Client[IO] = ??? // Your http4s client
val apiKey: ApiKey = ApiKey("your-nasa-api-key")

// Fetch APOD metadata for November 2, 2019
Apod[IO](client, apiKey)
  .call(date = "2019-11-02")
  .flatMap { response: Apod.Response =>
    IO(println(response.asJson.spaces2))
  }.unsafeRunSync()

// Output:
// {
//   "copyright" : "DSS",
//   "date" : "2019-11-02",
//   "explanation" : "The Flame Nebula stands out in this optical image...",
//   "hdurl" : "https://apod.nasa.gov/apod/image/1405/Flamessc2014-04a_Med.jpg",
//   "media_type" : "image",
//   "service_version" : "v1",
//   "title" : "Inside the Flame Nebula",
//   "url" : "https://apod.nasa.gov/apod/image/1405/flame_optical.jpg"
// }
```

### Downloading APOD Images

Download the actual image as a byte stream:

```scala
import fs2.Stream

val client: Client[IO] = ???
val apiKey: ApiKey = ApiKey("your-nasa-api-key")

// Download APOD image for November 2, 2019
val bytes: Stream[IO, Byte] =
  Apod[IO](client, apiKey).download(date = "2019-11-02")

// Write to file
bytes
  .through(fs2.io.file.writeAll(java.nio.file.Paths.get("apod-2019-11-02.jpg")))
  .compile
  .drain
  .unsafeRunSync()
```

### Parallel Downloads with ApodExporter

For bulk operations, use the built-in exporter utilities:

```scala
import nasa4s.apps.ApodExporter
import cats.effect.ConcurrentEffect

val apod: Apod[IO] = Apod[IO](client, apiKey)
val dates = List("2020-03-01", "2020-03-02", "2020-03-03")

// Download up to 3 APODs concurrently
ApodExporter
  .parDownloadApodsWithIndex[IO](
    apod,
    dates,
    maxConcurrentDownloads = 3
  )
  .compile
  .toVector
  .unsafeRunSync()
```

### Complete Application Example

See `nasa4s.apps.ApodLocalExporterApp` for a complete, runnable application that demonstrates:
- Setting up an http4s Blaze client
- Configuring concurrent downloads and exports
- Writing APODs to the local filesystem
- Proper resource management with Cats Effect

## Project Structure

```
nasa4s/
├── src/main/scala/nasa4s/
│   ├── apod/
│   │   ├── Apod.scala          # Core APOD API wrapper
│   │   └── ApiKey.scala        # NASA API key wrapper
│   └── apps/
│       ├── ApodExporter.scala  # Parallel download utilities
│       └── ApodLocalExporterApp.scala  # Example application
├── project/
│   ├── Dependencies.scala      # Library dependencies
│   └── build.properties
├── build.sbt
└── README.md
```

## API Coverage

### Currently Supported
- ✅ **APOD** (Astronomy Picture of the Day)
  - Metadata retrieval
  - Image/video downloads
  - HD/standard quality selection

### Planned
- ⏳ **Mars Rover Photos** - Images from Curiosity, Opportunity, and Spirit
- ⏳ **NEO** (Near Earth Objects) - Asteroid and comet data
- ⏳ **EPIC** (Earth Polychromatic Imaging Camera) - Earth imagery
- ⏳ **NASA Image and Video Library** - Search NASA's media archive
- ⏳ **Exoplanet Archive** - Planetary system data

## Dependencies

- **Scala**: 2.12.10
- **http4s**: 0.21.0-M5 (DSL, Blaze client, Circe integration)
- **Cats Effect**: 2.0.0 (Functional effects)
- **Circe**: 0.12.3 (JSON parsing)
- **FS2**: (Streaming)
- **fs2-blobstore**: 0.6.+ (Storage abstractions for local, SFTP, S3)

## Contributing

Contributions are welcome! Here's how you can help:

1. **Report bugs**: Open an issue describing the problem
2. **Suggest features**: Propose new NASA APIs to wrap or features to add
3. **Submit PRs**: Fork the repo, make your changes, and submit a pull request

### Development Setup

```bash
# Clone the repository
git clone https://github.com/brandon-powers/nasa4s.git
cd nasa4s

# Compile the project
sbt compile

# Run tests
sbt test

# Run the example app (update API key first)
sbt run
```

## Known Issues & TODOs

- Snake case field names in `Apod.Response` (see Apod.scala:32)
- File naming in local exporter could be more meaningful (see ApodLocalExporterApp.scala:29)
- Test coverage needs expansion
- Documentation for error handling

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- [NASA Open APIs](https://api.nasa.gov/) for providing free access to space data
- The Typelevel community for the excellent functional programming libraries

## Resources

- [NASA API Documentation](https://api.nasa.gov/)
- [APOD API Docs](https://github.com/nasa/apod-api)
- [http4s Documentation](https://http4s.org/)
- [Cats Effect Documentation](https://typelevel.org/cats-effect/)

---

**Explore the cosmos, functionally.** 🚀✨
