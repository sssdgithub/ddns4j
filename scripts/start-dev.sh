#!/bin/bash
# DDNS4J Development Environment Start Script (Linux/Mac)
# This script starts both Spring Boot backend and Vite frontend

set -euo pipefail

# Get script directory and navigate to project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo ""
echo "========================================"
echo "   DDNS4J Development Environment"
echo "========================================"
echo ""
echo "Project root: $PROJECT_ROOT"
echo ""

# Function to handle errors
handle_error() {
    echo ""
    echo "[ERROR] $1"
    echo ""
    exit 1
}

# Check dependencies
echo "[1/4] Checking dependencies..."

command -v node >/dev/null 2>&1 || handle_error "Node.js not found. Please install from https://nodejs.org/"
echo "  [OK] Node.js: $(node -v)"

command -v java >/dev/null 2>&1 || handle_error "Java not found. Please install JDK 8+"
echo "  [OK] Java: $(java -version 2>&1 | head -n 1)"

command -v mvn >/dev/null 2>&1 || handle_error "Maven not found. Please install from https://maven.apache.org/"
echo "  [OK] Maven: $(mvn -version 2>&1 | head -n 1)"

# Navigate to frontend directory
FRONTEND_DIR="$PROJECT_ROOT/ddns4j-web/src/main/resources/static"
cd "$FRONTEND_DIR" || handle_error "Failed to navigate to frontend directory"

if [ ! -f "package.json" ]; then
    handle_error "package.json not found in $FRONTEND_DIR"
fi

echo ""
echo "[2/4] Checking frontend dependencies..."

if [ ! -d "node_modules" ]; then
    echo "  [INSTALL] Installing frontend dependencies (this may take a while)..."
    npm install || handle_error "npm install failed"
    echo "  [OK] Dependencies installed"
else
    echo "  [OK] Dependencies already installed"
fi

echo ""
echo "[3/4] Starting backend service..."
echo ""
echo "+---------------------------------------------+"
echo "|  Backend: http://localhost:10000            |"
echo "|  Frontend: http://localhost:3000            |"
echo "+---------------------------------------------+"
echo ""

# Start backend in background and capture output
LOG_FILE="/tmp/ddns4j-backend-$$.log"
cd "$PROJECT_ROOT" || handle_error "Failed to navigate to project root"

echo "[INFO] Starting Spring Boot backend (check log: $LOG_FILE)"
mvn spring-boot:run -pl ddns4j-web > "$LOG_FILE" 2>&1 &
BACKEND_PID=$!

echo "[INFO] Backend PID: $BACKEND_PID"
echo "[INFO] Waiting for backend to be ready..."
echo ""

# Wait for backend to be ready (check if port 10000 is listening)
MAX_WAIT=90
WAIT_COUNT=0
while [ $WAIT_COUNT -lt $MAX_WAIT ]; do
    if curl -s http://localhost:10000/publicAccess/publicAccessDisabled >/dev/null 2>&1; then
        echo "[OK] Backend is ready! (took ${WAIT_COUNT}s)"
        break
    fi
    
    # Check if backend process is still running
    if ! kill -0 $BACKEND_PID 2>/dev/null; then
        echo ""
        echo "[ERROR] Backend process exited unexpectedly"
        echo "Check log file: $LOG_FILE"
        echo ""
        tail -n 30 "$LOG_FILE"
        exit 1
    fi
    
    sleep 2
    WAIT_COUNT=$((WAIT_COUNT + 2))
    
    # Show progress every 10 seconds
    if [ $((WAIT_COUNT % 10)) -eq 0 ]; then
        echo "  Waiting... ($WAIT_COUNT/$MAX_WAIT seconds)"
    fi
done

if [ $WAIT_COUNT -ge $MAX_WAIT ]; then
    echo ""
    echo "[WARNING] Backend may not be ready after $MAX_WAIT seconds"
    echo "Check log file: $LOG_FILE"
    echo "You can still try to access the backend manually"
fi

echo ""
echo "[4/4] Starting frontend development server..."
echo ""

cd "$FRONTEND_DIR"

# Determine browser command based on OS
if [[ "$OSTYPE" == "darwin"* ]]; then
    export BROWSER="open"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Try common Linux browsers
    if command -v xdg-open >/dev/null 2>&1; then
        export BROWSER="xdg-open"
    elif command -v gnome-open >/dev/null 2>&1; then
        export BROWSER="gnome-open"
    fi
fi

# Start frontend
npm run dev:open

echo ""
echo "========================================"
echo "   Frontend stopped"
echo "========================================"
echo ""
echo "Backend is still running (PID: $BACKEND_PID)"
echo "To stop backend, run: kill $BACKEND_PID"
echo "Log file: $LOG_FILE"
echo ""

# Clean up backend process on exit
cleanup() {
    echo "Stopping backend..."
    kill $BACKEND_PID 2>/dev/null || true
    rm -f "$LOG_FILE"
}
trap cleanup EXIT
