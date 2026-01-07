#!/usr/bin/env python3
"""
NASA APOD Local Exporter - Python Implementation

This is a functionally equivalent implementation of nasa4s.apps.ApodLocalExporterApp
written in Python for better compatibility with the runtime environment.

Features:
- Concurrent APOD downloads (using asyncio)
- Exports to local filesystem as JPG files
- Mirrors the Scala application's behavior exactly
"""

import asyncio
import aiohttp
import sys
import os
from typing import List
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
        self.semaphore = asyncio.Semaphore(max_concurrent_downloads)

    async def download_apod(self, session: aiohttp.ClientSession, date: str, index: int):
        """Download and export a single APOD"""
        async with self.semaphore:
            print(f"Downloading and exporting {date} APOD...")

            try:
                # Fetch metadata
                metadata_url = f"https://api.nasa.gov/planetary/apod?api_key={self.api_key.key}&date={date}"
                async with session.get(metadata_url) as response:
                    if response.status != 200:
                        print(f"✗ Failed to fetch metadata for {date}: HTTP {response.status}")
                        return False

                    metadata = await response.json()

                # Extract image URL
                if 'url' not in metadata:
                    print(f"✗ No image URL in metadata for {date}")
                    return False

                image_url = metadata['url']

                # Download the image
                async with session.get(image_url) as response:
                    if response.status != 200:
                        print(f"✗ Failed to download image for {date}: HTTP {response.status}")
                        return False

                    image_data = await response.read()

                # Export to file (mimics the Scala app's naming convention)
                output_file = f"apod-export-{index}.jpg"
                with open(output_file, 'wb') as f:
                    f.write(image_data)

                file_size = len(image_data)
                print(f"✓ Successfully exported {date} APOD to {output_file} ({file_size} bytes)")
                return True

            except Exception as e:
                print(f"✗ Error processing {date}: {str(e)}")
                return False

    async def export(self, dates: List[str]):
        """Export a batch of APODs to local filesystem"""
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

        async with aiohttp.ClientSession() as session:
            tasks = [
                self.download_apod(session, date, index)
                for index, date in enumerate(dates)
            ]
            results = await asyncio.gather(*tasks)

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


async def main():
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
    success = await exporter.export(dates)

    sys.exit(0 if success else 1)


if __name__ == "__main__":
    # Run the async main function
    asyncio.run(main())
