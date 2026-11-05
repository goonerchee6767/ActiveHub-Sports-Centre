#!/bin/sh
# Compile and run the ActiveHub System (macOS / Linux)
cd "$(dirname "$0")" || exit 1

if ! command -v javac >/dev/null 2>&1; then
    echo "Java JDK not found. Please install a JDK from https://adoptium.net"
    exit 1
fi

echo "Compiling..."
javac -d out *.java || { echo "Compile failed."; exit 1; }
java -cp out ActiveHubSystem
