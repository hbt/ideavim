FROM ubuntu:22.10

RUN apt-get update && apt-get install -y openjdk-17-jdk \
  git 

COPY . /mounted

# this step should be cached if you're running this more than once.
RUN cd /mounted && ./gradlew buildPlugin


