#!/bin/bash

# Load .env file if it exists
if [ -f .env ]; then
    # shellcheck disable=SC2046
    export $(grep -v '^#' .env | grep -v '^$' | xargs)
fi

PORT=8080
HEALTH_URL="http://localhost:$PORT/health"
MAX_WAIT=60   # seconds before giving up on server startup

cleanup() {
    echo ""
    echo "Stopping all processes..."
    [ -n "$SERVER_PID" ] && kill "$SERVER_PID" 2>/dev/null
    fuser -k "${PORT}/tcp" 2>/dev/null
    echo "Done. See you later!"
    exit 0
}

trap cleanup SIGINT SIGTERM

# Kill any zombie from a previous run
echo "Checking port $PORT..."
fuser -k "${PORT}/tcp" 2>/dev/null

# Start the server — plain console so Gradle progress bars don't pollute the terminal.
# Output is tee'd to server.log so you can inspect it at any time.
echo "Starting Ktor server (output -> server.log)..."
./gradlew :server:run --console=plain > server.log 2>&1 &
SERVER_PID=$!

# Wait for the health endpoint to respond
echo "Waiting for server at $HEALTH_URL (timeout: ${MAX_WAIT}s)..."
COUNT=0
while ! curl -s --connect-timeout 2 "$HEALTH_URL" > /dev/null 2>&1; do
    if [ "$COUNT" -ge "$MAX_WAIT" ]; then
        echo ""
        echo "ERROR: Server did not start within ${MAX_WAIT}s."
        echo "--- Last 30 lines of server.log ---"
        tail -30 server.log
        echo "-----------------------------------"
        cleanup
        exit 1
    fi
    ((COUNT++))
    printf "\r  Waiting... %02d / %02d s" "$COUNT" "$MAX_WAIT"
    sleep 1
done

echo ""
echo "Server is up and running!"
echo ""

# Start the desktop app (plain console, output goes straight to terminal)
echo "Starting desktop app..."
./gradlew :composeApp:run --console=plain

cleanup
