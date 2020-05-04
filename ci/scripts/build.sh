#!/bin/bash -eux

pushd dp-dataset-exporter-xlsx
  make build
  cp -r Dockerfile.concourse target ../build/
popd
