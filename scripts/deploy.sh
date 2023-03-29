#!/usr/bin/env bash

# Usage: ./scripts/deploy.sh
# Deploys the plugin to all the IDEs

set -e

# get the script's path
SCRIPT_PATH="$( cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"

cd "$SCRIPT_PATH"/.. || exit 1

./gradlew buildPlugin

for dir in /home/hassen/.local/share/JetBrains/*2022*; do
  if [[ -d "$dir" ]]; then
    echo "Installing IdeaVim in $dir"
    cd "$SCRIPT_PATH"/.. || exit 1
    cp build/distributions/IdeaVim-SNAPSHOT.zip "$dir"/ideavim.zip
    cd "$dir" || exit 1
    rm -rf "$dir"/IdeaVim &> /dev/null
    unzip ideavim.zip
  fi
done

# // TODO(hbt) NEXT add the build zip to the release in github
