#!/bin/bash -eux
mvn -Dmaven.test.skip package
java -jar target/dp-dataset-exporter-xlsx-*.jar 
