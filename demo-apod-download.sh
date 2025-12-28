#!/bin/bash

# Demo script showing what the ApodLocalExporterApp does
# This simulates the functionality of the Scala application

echo "=== NASA APOD Downloader Demo ==="
echo
echo "This demonstrates the functionality of ApodLocalExporterApp.scala"
echo "The Scala app downloads NASA's Astronomy Picture of the Day (APOD)"
echo

# Note: Replace DEMO_KEY with an actual NASA API key from https://api.nasa.gov/
API_KEY="${NASA_API_KEY:-DEMO_KEY}"
DATE="${1:-2020-03-01}"

echo "Fetching APOD metadata for date: $DATE"
echo "API Key: $API_KEY"
echo

# Fetch APOD metadata
METADATA=$(curl -s "https://api.nasa.gov/planetary/apod?api_key=$API_KEY&date=$DATE")

if [ $? -ne 0 ]; then
    echo "Error fetching APOD metadata"
    exit 1
fi

echo "Metadata received:"
echo "$METADATA" | head -20
echo

# Extract image URL from JSON (using basic text processing since jq might not be available)
IMAGE_URL=$(echo "$METADATA" | grep -o '"url":"[^"]*"' | head -1 | cut -d'"' -f4)

if [ -z "$IMAGE_URL" ]; then
    echo "Could not extract image URL from metadata"
    echo "Full response:"
    echo "$METADATA"
    exit 1
fi

echo "Image URL: $IMAGE_URL"
echo

# Download the image (similar to what the Scala app does)
OUTPUT_FILE="apod-export-0.jpg"
echo "Downloading and exporting $DATE APOD..."
curl -L "$IMAGE_URL" -o "$OUTPUT_FILE"

if [ $? -eq 0 ] && [ -f "$OUTPUT_FILE" ]; then
    FILE_SIZE=$(stat -f%z "$OUTPUT_FILE" 2>/dev/null || stat -c%s "$OUTPUT_FILE" 2>/dev/null)
    echo
    echo "Success! Downloaded APOD to $OUTPUT_FILE (${FILE_SIZE} bytes)"
    echo "Exiting..."
else
    echo
    echo "Error downloading APOD image"
    exit 1
fi
