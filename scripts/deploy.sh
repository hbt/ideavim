#!/usr/bin/env bash

# Usage: ./scripts/deploy.sh

set -e

./gradlew buildPlugin
ction com.hbtlabs.SwitchTab3Action


cp build/distributions/IdeaVim-SNAPSHOT.zip /home/hassen/.local/share/JetBrains/IntelliJIdea2022.3/ideavim.zip
cd /home/hassen/.local/share/JetBrains/IntelliJIdea2022.3 || exit 1
rm -rf IdeaVim &> /dev/null

unzip ideavim.zip

