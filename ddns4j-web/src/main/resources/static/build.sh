#!/bin/bash
# DDNS4J Frontend Build Script
# This script builds the Vue 3 frontend and copies the output to Spring Boot static resources

set -e

echo "🔨 Building DDNS4J frontend..."

# Install dependencies
echo "📦 Installing dependencies..."
npm install

# Build for production
echo "🏗️  Building for production..."
npm run build

# Copy built files to Spring Boot static resources directory
echo "📋 Copying built files to static resources..."
BUILD_DIR="dist"
STATIC_DIR="../static"

# Clean old static files (keep index.html, package.json, etc.)
rm -rf "${STATIC_DIR}/assets"

# Copy new build output
cp -r "${BUILD_DIR}/assets" "${STATIC_DIR}/"
cp "${BUILD_DIR}/index.html" "${STATIC_DIR}/index.html.built"

echo "✅ Build completed successfully!"
echo "📁 Built files are in: ${STATIC_DIR}"
echo ""
echo "Next steps:"
echo "1. Rename index.html.built to index.html if you want to use the built version"
echo "2. Or configure Spring Boot to serve the built index.html"
echo "3. Run 'mvn clean package' to build the Spring Boot application"
