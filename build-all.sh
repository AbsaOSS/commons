#!/bin/sh

BASE_DIR=$(dirname "$0")

cross_build() {
  SCALA_VER=$1
  echo "Building with Scala $SCALA_VER"
  find $BASE_DIR/target/* -type d -exec rm -rf {} \;
  mvn scala-cross-build:change-version -Pscala-$SCALA_VER
  mvn install
}

# ------------------------------------------------

mvn clean

cross_build 2.11
cross_build 2.12

mvn scala-cross-build:restore-version
