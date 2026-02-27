#!/bin/bash
# Exit immediately if a command exits with a non-zero status.
set -e

# Professional test runner script for Market Data Pipeline
echo "🚀 Starting full project verification..."

# Navigate to the project root directory relative to this script
PROJECT_ROOT="$(dirname "$0")/.."
cd "$PROJECT_ROOT"

# Run clean to ensure no stale artifacts and execute all tests
echo "🧹 Cleaning and running tests..."
./gradlew clean test

echo "✅ All tests passed successfully!"
