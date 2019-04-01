#!/bin/bash
set -euo pipefail

SvcPrincipalApplicationId=$servicePrincipalId
SvcPrincipalObjectId=$(az ad sp show --id $SvcPrincipalApplicationId | jq -r .objectId)


az group create --name $RESOURCE_GROUP_NAME --location $LOCATION
az group deployment create -g $RESOURCE_GROUP_NAME --template-file arm-templates/azuredeploy.json --parameters dataFactoryName=$DATA_FACTORY_NAME keyVaultName=$KEY_VAULT_NAME storageAccountName=$STORAGE_ACCOUNT_NAME keyVaultWriterPrincipalId=$SvcPrincipalObjectId

