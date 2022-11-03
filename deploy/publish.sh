#!/bin/bash

if [[ $TRAVIS_PULL_REQUEST == "false" ]]; then
    export MAVEN_OPTS="--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.desktop/java.awt.font=ALL-UNNAMED"
    mvn deploy --settings $GPG_DIR/settings.xml -DperformRelease=true -DskipTests=true -Drelease-composite=true
    exit $?
fi
