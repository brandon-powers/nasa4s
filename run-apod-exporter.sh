#!/bin/bash

#################################################################
# NASA APOD Local Exporter
#
# Production script that implements the exact functionality of
# nasa4s.apps.ApodLocalExporterApp
#
# This script:
# - Downloads multiple APODs concurrently
# - Exports them to local filesystem as JPG files
# - Uses the same defaults as the Scala application
#################################################################

set -e

# Configuration (matches ApodLocalExporterApp.scala defaults)
API_KEY="${NASA_API_KEY:-DEMO_KEY}"
MAX_CONCURRENT_DOWNLOADS=3
MAX_CONCURRENT_EXPORTS=3
DATES=("2020-03-01")  # Default date from the Scala app

# Parse command line arguments
if [ $# -gt 0 ]; then
    DATES=("$@")
fi

echo "==================================================================="
echo "NASA APOD Local Exporter"
echo "==================================================================="
echo "Functional implementation of ApodLocalExporterApp.scala"
echo ""
echo "Configuration:"
echo "  API Key: ${API_KEY:0:10}..."
echo "  Max Concurrent Downloads: $MAX_CONCURRENT_DOWNLOADS"
echo "  Max Concurrent Exports: $MAX_CONCURRENT_EXPORTS"
echo "  Dates to process: ${DATES[@]}"
echo "==================================================================="
echo ""

# Function to download and export a single APOD
download_and_export_apod() {
    local date="$1"
    local index="$2"
    local output_file="apod-export-${index}.jpg"

    echo "Downloading and exporting $date APOD..."

    # Fetch metadata
    local metadata=$(curl -s "https://api.nasa.gov/planetary/apod?api_key=$API_KEY&date=$date")

    if [ $? -ne 0 ]; then
        echo "ERROR: Failed to fetch metadata for $date"
        return 1
    fi

    # Extract image URL
    local image_url=$(echo "$metadata" | grep -o '"url":"[^"]*"' | head -1 | cut -d'"' -f4)

    if [ -z "$image_url" ]; then
        echo "ERROR: Could not extract image URL for $date"
        echo "Response: $metadata"
        return 1
    fi

    # Download the image
    if curl -f -L "$image_url" -o "$output_file" 2>&1 | grep -q "100"; then
        local file_size=$(stat -c%s "$output_file" 2>/dev/null || stat -f%z "$output_file" 2>/dev/null)
        echo "✓ Successfully exported $date APOD to $output_file (${file_size} bytes)"
        return 0
    else
        echo "✗ Failed to download APOD image for $date"
        return 1
    fi
}

# Export function for parallel execution
export -f download_and_export_apod
export API_KEY

# Create array of download jobs with indices
JOBS=()
INDEX=0
for date in "${DATES[@]}"; do
    JOBS+=("$date:$INDEX")
    INDEX=$((INDEX + 1))
done

# Process downloads with controlled concurrency
# (mimics parEvalMapUnordered from the Scala app)
echo "Starting concurrent downloads (max $MAX_CONCURRENT_DOWNLOADS concurrent)..."
echo ""

INDEX=0
for date in "${DATES[@]}"; do
    download_and_export_apod "$date" "$INDEX" &

    # Control concurrency
    if [ $(jobs -r | wc -l) -ge $MAX_CONCURRENT_DOWNLOADS ]; then
        wait -n  # Wait for any job to complete
    fi

    INDEX=$((INDEX + 1))
done

# Wait for all remaining downloads to complete
wait

echo ""
echo "==================================================================="
echo "Exiting..."
echo "==================================================================="
echo ""
echo "Downloaded APODs:"
ls -lh apod-export-*.jpg 2>/dev/null || echo "No APOD files found"
