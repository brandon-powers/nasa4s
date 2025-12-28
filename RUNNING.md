# Running nasa4s

## Quick Start - Demo Script

The easiest way to see the APOD downloader in action is using the demo script:

```bash
./demo-apod-download.sh [DATE]
```

Example:
```bash
./demo-apod-download.sh 2020-03-01
```

This script demonstrates the core functionality of `ApodLocalExporterApp` by:
1. Fetching APOD metadata from NASA's API for the specified date
2. Extracting the image URL from the JSON response
3. Downloading and saving the APOD image as `apod-export-0.jpg`

### Setting a NASA API Key

For better rate limits, get a free API key from https://api.nasa.gov/ and use it:

```bash
NASA_API_KEY=your_key_here ./demo-apod-download.sh 2020-03-01
```

## Running the Full Scala Application

To run the complete Scala application with all its features:

### Prerequisites

- Java 11 or higher
- sbt 1.3.3 or higher
- Access to Maven Central and Scala repositories

### Build and Run

```bash
# Compile the project
sbt compile

# Run the application
sbt run
```

### Configuring the API Key

Edit `src/main/scala/nasa4s/apps/ApodLocalExporterApp.scala` and replace:

```scala
val apiKey = ApiKey("<omitted>")
```

with your NASA API key:

```scala
val apiKey = ApiKey("your_nasa_api_key_here")
```

### Customizing Downloads

In `ApodLocalExporterApp.scala`, you can modify:

- **Dates to download**: Change the list in `exporter.export(List("2020-03-01"))`
- **Concurrency**: Adjust `maxConcurrentDownloads` and `maxConcurrentExports`
- **Output location**: Modify the file path in `ApodLocalExporter`

## Project Structure

- `src/main/scala/nasa4s/apod/Apod.scala` - APOD API client
- `src/main/scala/nasa4s/apod/ApiKey.scala` - API key wrapper
- `src/main/scala/nasa4s/apps/ApodLocalExporterApp.scala` - Main application
- `src/main/scala/nasa4s/apps/ApodExporter.scala` - Export functionality
- `demo-apod-download.sh` - Standalone demo script

## Troubleshooting

### Network/Proxy Issues

If you encounter proxy authentication errors with sbt/Gradle:

1. Check your network proxy settings
2. Configure sbt proxy in `~/.sbt/repositories` or `.jvmopts`
3. Use the demo script as a workaround - it uses curl which handles proxies better

### Missing Dependencies

If compilation fails due to missing dependencies, ensure you have internet access to:
- https://repo1.maven.org/maven2/
- https://repo.scala-sbt.org/
- https://repo.typesafe.com/
