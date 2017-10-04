#!/bin/bash -eux

pushd dp-dataset-exporter-xlsx
  mvn test
popd
