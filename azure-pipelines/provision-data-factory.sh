#!/bin/bash
set -euo pipefail

unzip -od generated arm-templates/generated-data-factory/arm_template.zip

az group deployment create -g $RESOURCE_GROUP_NAME --template-file generated/arm_template.json --parameters factoryName=$DATA_FACTORY_NAME AzureKeyVault_properties_typeProperties_baseUrl=https://$KEY_VAULT_NAME.vault.azure.net/

