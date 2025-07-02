## build from scratch

- clone
- download ultimate idea
- go to Help > About > copy version e.g (223.8836.41)
- open gradle.properties and fix `ideaVersion=223.8836.41` to target platform. This important because for most commits, the build will fail if you don't specify the target platform.
- ./gradlew buildPlugin

Note: If you pick a different revision, it might fail until target ideaVersion is specified. 

## quick build from scratch

- dc up --build

It doesn't cache the gradle stuff, so super slow. But useful on VMs.


## for dev

- ./gradlew runIde
- or Run > editConfiguration > runIde 



## changes

- changes flagged under hbtXXX for future merge conflicts resolution
- use live reload in debug mode alt+r D -- careful, not consistent

## logging

- add code debugging -- view d61178a598f6a9d0df7d128ccf82e360989e255e as example
- fire up ide instance
- check IDE log 
- use tail -f /home/hassen/workspace/ideavim/build/idea-sandbox/system/log/idea.log
  - or for golang, 
    - `cd /home/hassen/.cache/JetBrains/GoLand2022.3/log` 
    - `tail -f idea.log | grep "ideavim.hbt"`


## build zip file

- build > build module ideavim



# install

- copy build > distributions > ideavim-snapshot.zip
- open another ide
- settings > plugins > install from disk


or install automatically and restart ide 
- unzip 
- copy dir to ~/.local/share/(IDE)/IdeaVim

## deploy to all IDEs

- ./scripts/deploy.sh
- automatically builds and installs to all 2022 IDEs
- restarts required to load new plugin

