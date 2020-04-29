#!/bin/bash -eux

pushd dp-dataset-exporter-xlsx
  mvn -Dmaven.test.skip -Dossindex.skip=true clean package dependency:copy-dependencies
  cp -r Dockerfile.concourse target ../build/
popd
