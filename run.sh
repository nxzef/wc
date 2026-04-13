#!/bin/bash

# --- CONFIGURATION ---
PORT=8080
# The URL to check. If your server has a specific health path, use that.
HEALTH_URL="http://localhost:$PORT"

# --- CLEANUP LOGIC ---
cleanup() {
    echo -e "\n\e[31mStopping all processes...\e[0m"
    # Kill the server process group
    if [ -n "$SERVER_PID" ]; then
        kill $SERVER_PID 2>/dev/null
    fi
    # Force kill anything remaining on the port
    fuser -k $PORT/tcp 2>/dev/null
    echo "Cleaned up. See you later!"
    exit
}

# Catch stop signals (Android Studio Stop button or Ctrl+C)
trap cleanup SIGINT SIGTERM

# --- EXECUTION ---

# 1. Kill any zombie processes from previous failed runs
echo "Pre-run cleanup on port $PORT..."
fuser -k $PORT/tcp 2>/dev/null

# 2. Start the Server
echo -e "\e[32mStarting Ktor Server...\e[0m"
./gradlew :server:run &
SERVER_PID=$!

# 3. Intelligent Wait (The Claude + Safety Method)
echo "Waiting for server to respond at $HEALTH_URL..."
MAX_RETRIES=30
COUNT=0

while ! curl -s --connect-timeout 2 $HEALTH_URL > /dev/null 2>&1; do
    if [ $COUNT -ge $MAX_RETRIES ]; then
        echo -e "\e[31mError: Server took too long to start. Check your logs.\e[0m"
        cleanup
    fi
    echo -n "."
    sleep 1
    ((COUNT++))
done

echo -e "\n\e[32m✅ Server is active!\e[0m"

# 4. Start the Desktop App
echo -e "\e[34mStarting Desktop App...\e[0m"
# Note: Using :composeApp:run as it's the standard for Desktop
./gradlew :composeApp:run

# 5. Final cleanup when the app is closed
cleanup