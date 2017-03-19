#!/bin/bash

if [[ $TRAVIS_PULL_REQUEST == "false" ]]; then
    mvn verify --settings $GPG_DIR/settings.xml -DperformUpdateSiteRelease=true
    exit $?
fi
