#!/bin/bash -eux

pushd dp-dataset-exporter-xlsx
  mvn -Dossindex.skip=true test
popd
