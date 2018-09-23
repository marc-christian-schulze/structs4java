#!/usr/bin/env bash

docker build -t buildbox .

docker run -i --rm \
    -v "$(pwd)":/workspace \
    --user "$(id -u):$(id -g)" \
    -e DISPLAY=unix:0.0 \
    -v /tmp/.X11-unix:/tmp/.X11-unix:ro \
    buildbox \
    mvn clean install -Dmaven.repo.local=/workspace/.m2
