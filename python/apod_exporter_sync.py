#!/usr/bin/env python3
"""
NASA APOD Local Exporter - Python Implementation (Synchronous)

This is a functionally equivalent implementation of nasa4s.apps.ApodLocalExporterApp
using Python's standard library for maximum compatibility.

Features:
- Concurrent APOD downloads (using threading)
- Exports to local filesystem as JPG files
- Mirrors the Scala application's behavior exactly
"""

import urllib.request
import urllib.parse
import json
import sys
import os
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path


class ApiKey:
    """Wrapper for NASA API key"""
    def __init__(self, key: str):
        self.key = key


class ApodExporter:
    """Exports APODs to the local filesystem"""

    def __init__(self, api_key: ApiKey, max_concurrent_downloads: int = 3, max_concurrent_exports: int = 3):
        self.api_key = api_key
        self.max_concurrent_downloads = max_concurrent_downloads
        self.max_concurrent_exports = max_concurrent_exports

    def download_apod(self, date: str, index: int):
        """Download and export a single APOD"""
        print(f"Downloading and exporting {date} APOD...")

        try:
            # Fetch metadata
            metadata_url = f"https://api.nasa.gov/planetary/apod?api_key={self.api_key.key}&date={date}"
            with urllib.request.urlopen(metadata_url, timeout=30) as response:
                metadata = json.loads(response.read().decode('utf-8'))

            # Extract image URL
            if 'url' not in metadata:
                print(f"✗ No image URL in metadata for {date}")
                return False

            image_url = metadata['url']

            # Download the image
            with urllib.request.urlopen(image_url, timeout=30) as response:
                image_data = response.read()

            # Export to file (mimics the Scala app's naming convention)
            output_file = f"apod-export-{index}.jpg"
            with open(output_file, 'wb') as f:
                f.write(image_data)

            file_size = len(image_data)
            print(f"✓ Successfully exported {date} APOD to {output_file} ({file_size:,} bytes)")
            return True

        except Exception as e:
            print(f"✗ Error processing {date}: {str(e)}")
            return False

    def export(self, dates: list):
        """Export a batch of APODs to local filesystem with controlled concurrency"""
        print("="*67)
        print("NASA APOD Local Exporter (Python)")
        print("="*67)
        print(f"Configuration:")
        print(f"  API Key: {self.api_key.key[:10]}...")
        print(f"  Max Concurrent Downloads: {self.max_concurrent_downloads}")
        print(f"  Max Concurrent Exports: {self.max_concurrent_exports}")
        print(f"  Dates to process: {', '.join(dates)}")
        print("="*67)
        print()

        # Download APODs concurrently using ThreadPoolExecutor
        # (mimics parEvalMapUnordered from the Scala app)
        results = []
        with ThreadPoolExecutor(max_workers=self.max_concurrent_downloads) as executor:
            future_to_date = {
                executor.submit(self.download_apod, date, index): (date, index)
                for index, date in enumerate(dates)
            }

            for future in as_completed(future_to_date):
                date, index = future_to_date[future]
                try:
                    result = future.result()
                    results.append(result)
                except Exception as e:
                    print(f"✗ Exception while processing {date}: {str(e)}")
                    results.append(False)

        print()
        print("="*67)
        print("Exiting...")
        print("="*67)
        print()

        # Show downloaded files
        print("Downloaded APODs:")
        apod_files = sorted(Path('.').glob('apod-export-*.jpg'))
        if apod_files:
            for f in apod_files:
                size = f.stat().st_size
                print(f"  {f.name}: {size:,} bytes")
        else:
            print("  No APOD files found")

        return all(results)


def main():
    """Main application entry point (mimics ApodLocalExporterApp)"""
    # Configuration (matches the Scala application)
    api_key = ApiKey(os.getenv("NASA_API_KEY", "DEMO_KEY"))

    # Default dates (matches ApodLocalExporterApp.scala)
    dates = ["2020-03-01"]

    # Parse command line arguments
    if len(sys.argv) > 1:
        dates = sys.argv[1:]

    # Create exporter with same concurrency settings as Scala app
    exporter = ApodExporter(
        api_key=api_key,
        max_concurrent_downloads=3,
        max_concurrent_exports=3
    )

    # Export APODs
    success = exporter.export(dates)

    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()
