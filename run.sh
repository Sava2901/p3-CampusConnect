#!/usr/bin/env sh
set -e

# Always run from the project root
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
cd "$SCRIPT_DIR"

echo "Starting CampusConnect in the terminal..."

if command -v mvn >/dev/null 2>&1; then
  mvn -q -DskipTests -Dexec.mainClass=org.example.Main exec:java
else
  echo "Error: Apache Maven (mvn) is not installed or not on PATH."
  echo "Install Maven, or run from your IDE targeting 'org.example.Main'."
  exit 1
fi

