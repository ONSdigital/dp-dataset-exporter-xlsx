SHELL=bash

APP=dp-dataset-exporter-xlsx

VAULT_ADDR?='http://127.0.0.1:8200'

# The following variables are used to generate a vault token for the app. The reason for declaring variables, is that
# its difficult to move the token code in a Makefile action. Doing so makes the Makefile more difficult to
# read and starts introduction if/else statements.
VAULT_POLICY:="$(shell vault policy write -address=$(VAULT_ADDR) write-psk policy.hcl)"
TOKEN_INFO:="$(shell vault token create -address=$(VAULT_ADDR) -policy=write-psk -period=24h -display-name=$(APP))"
APP_TOKEN:="$(shell echo $(TOKEN_INFO) | awk '{print $$6}')"

build:
	mvn -Dmaven.test.skip -Dossindex.skip=true clean package dependency:copy-dependencies
debug: build
	HUMAN_LOG=1 VAULT_TOKEN=$(APP_TOKEN) VAULT_ADDR=$(VAULT_ADDR) java -jar target/$(APP)-*.jar
acceptance:
	HUMAN_LOG=1 VAULT_TOKEN=$(APP_TOKEN) VAULT_ADDR=$(VAULT_ADDR) java -jar target/$(APP)-*.jar
test:
	mvn test -Dossindex.skip=true
audit:
	mvn ossindex:audit
vault:
	@echo "$(VAULT_POLICY)"
	@echo "$(TOKEN_INFO)"
	@echo "$(APP_TOKEN)"
.PHONY: build debug test vault acceptance audit
