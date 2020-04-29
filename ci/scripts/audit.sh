#!/bin/bash -eux

pushd dp-dataset-exporter-xlsx
    mvn ossindex:audit
popd