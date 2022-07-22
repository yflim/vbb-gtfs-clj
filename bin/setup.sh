#!/usr/bin/env bash

set -o errexit
set -euo pipefail

export RESOURCE_DIR="$(pwd)/resources"

mkdir -p $RESOURCE_DIR
wget -np -nd -rP $RESOURCE_DIR --accept-regex ".*csv.gz" https://vbb-gtfs.jannisr.de/latest/
rm $RESOURCE_DIR/index.html
gunzip $RESOURCE_DIR/*
clj -X vbb-gtfs-clj.core/setup

unset RESOURCE_DIR
