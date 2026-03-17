#!/bin/bash
# Exit immediately if a command exits with a non-zero status.
set -e

# Script to run Angular frontend tests
echo "🎨 Starting Frontend (Angular) tests..."

# Navigate to the frontend directory relative to this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/../frontend"

# Run npm test in CI mode (non-watching) if possible, or standard test
# Note: Angular's 'ng test' usually requires a browser. 
# We use 'watch=false' to ensure it runs once and exits.
npm test -- --watch=false --browsers=ChromeHeadlessNoSandbox

echo "✅ Frontend tests passed successfully!"
