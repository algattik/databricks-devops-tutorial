#!/bin/bash
set -euo pipefail

export KEY_VAULT_NAME=databrickscicdtutd06test
export RESOURCE_GROUP_NAME=databrickscicdtutd06test
export DATA_FACTORY_NAME=databrickscicdtutd06test

COUNTER=0
for template in \
  "datafactory/linkedService/AzureKeyVault.json" \
  "datafactory/linkedService/AzureDatabricks.json" \
  "datafactory/pipeline/Apply ML model pipeline.json" \
  ; do
    let COUNTER=COUNTER+1
    mkdir -p datafactory-generated
    basename=$(basename "$template")
    generated_file="datafactory-generated/$basename"
    jq -sf azure-pipelines/provision-data-factory-without-zip/generate-adf-template.jq "$template" > "$generated_file"
    az group deployment create -g $RESOURCE_GROUP_NAME --template-file "$generated_file" --name "$DATA_FACTORY_NAME-$COUNTER"
done
