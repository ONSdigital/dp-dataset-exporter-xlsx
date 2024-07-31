SHELL=bash

APP=dp-dataset-exporter-xlsx


build:
	mvn -Dmaven.test.skip -Dossindex.skip=true clean package dependency:copy-dependencies
debug: build
	HUMAN_LOG=1 java -jar target/$(APP)-*.jar
acceptance:
	HUMAN_LOG=1 java -jar target/$(APP)-*.jar
test:
	mvn test -Dossindex.skip=true
audit:
	mvn ossindex:audit
.PHONY: build debug test acceptance audit
