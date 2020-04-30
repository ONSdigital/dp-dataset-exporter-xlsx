#!/bin/bash -eux

pushd dp-dataset-exporter-xlsx
    mvn make audit
popd