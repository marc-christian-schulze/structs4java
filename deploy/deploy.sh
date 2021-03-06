#!/bin/bash

set -e

echo "Starting Deployment..."

openssl aes-256-cbc -pass pass:$ENCRYPTION_PASSWORD -in $GPG_DIR/pubring.gpg.enc -out $GPG_DIR/pubring.gpg -d
openssl aes-256-cbc -pass pass:$ENCRYPTION_PASSWORD -in $GPG_DIR/secring.gpg.enc -out $GPG_DIR/secring.gpg -d

echo "Deploying Structs4Java Core..."
cd structs4java-core          && "$GPG_DIR/publish.sh"         && cd ..

echo "Deploying Structs4Java Maven Plugin..."
cd structs4java-maven-plugin  && "$GPG_DIR/publish.sh"         && cd ..

echo "Deployment done."
