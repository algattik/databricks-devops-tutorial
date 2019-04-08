#!/bin/bash
set -euo pipefail

az keyvault secret set --vault-name $KEY_VAULT_NAME --name AzureDatabricksToken --value $DATABRICKS_TOKEN

storageAccountKey=$(az storage account keys list -g $RESOURCE_GROUP_NAME -n $STORAGE_ACCOUNT_NAME --query "[0].value" | tr -d '"')

if ! databricks secrets list-scopes --output JSON | jq -e '.scopes[] | select (.name == "bikeshare")'; then
    databricks secrets create-scope --scope bikeshare --initial-manage-principal "users"
fi
databricks secrets write --scope bikeshare --key storagekey --string-value $storageAccountKey

databricks fs cp --overwrite java-library/target/*.jar dbfs:/model-factory.jar


