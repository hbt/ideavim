#!/usr/bin/env bash

# Usage: ./scripts/deploy.sh

set -e

./gradlew buildPlugin


cp build/distributions/IdeaVim-SNAPSHOT.zip /home/hassen/.local/share/JetBrains/IntelliJIdea2022.3/ideavim.zip
cd /home/hassen/.local/share/JetBrains/IntelliJIdea2022.3 || exit 1
rm -rf IdeaVim &> /dev/null

unzip ideavim.zip


# // TODO(hbt) NEXT apply to all IDEs
# // TODO(hbt) NEXT add the build zip to the release in github
