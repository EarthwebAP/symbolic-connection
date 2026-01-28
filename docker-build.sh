#!/bin/bash

# Symbolic Connection Docker Build Script
# This script builds the APK in a completely isolated Docker environment

set -e

echo "=========================================="
echo "Symbolic Connection - Docker APK Build"
echo "=========================================="
echo ""
echo "Building in clean Docker container..."
echo "This will take ~10-15 minutes on first run"
echo ""

# Build Docker image
echo "[1/3] Building Docker image..."
sudo docker build -t symbolic-connection-builder:latest .

# Create output directory
mkdir -p ./build-outputs

# Run container and extract APK
echo "[2/3] Running build inside Docker..."
sudo docker run --rm \
  -v "$(pwd)/build-outputs:/build/outputs" \
  symbolic-connection-builder:latest

# Check if APK was created
echo "[3/3] Checking for APK..."
if ls build-outputs/*.apk 1> /dev/null 2>&1; then
    echo ""
    echo "=========================================="
    echo "✅ BUILD SUCCESSFUL!"
    echo "=========================================="
    echo ""
    ls -lh build-outputs/*.apk
    echo ""
    echo "APK location: ./build-outputs/"
    echo ""
    echo "Next steps:"
    echo "1. Connect your Android device or emulator"
    echo "2. Run: adb install build-outputs/*.apk"
    echo ""
else
    echo ""
    echo "=========================================="
    echo "❌ BUILD FAILED"
    echo "=========================================="
    echo ""
    echo "APK not found. Check Docker output above for errors."
    exit 1
fi
