#!/bin/bash
set -euo pipefail

az keyvault secret set --vault-name $KEY_VAULT_NAME --name AzureDatabricksToken --value $DATABRICKS_TOKEN

storageAccountKey=$(az storage account keys list -g $RESOURCE_GROUP_NAME -n $STORAGE_ACCOUNT_NAME --query "[0].value" | tr -d '"')

if ! databricks secrets list-scopes --output JSON | jq -e '.scopes[] | select (.name == "bikeshare")'; then
    databricks secrets create-scope --scope bikeshare --initial-manage-principal "users"
fi
databricks secrets write --scope bikeshare --key storagekey --string-value $storageAccountKey

databricks fs cp --overwrite java-library/target/*.jar dbfs:/model-factory.jar


if databricks clusters list  --output JSON| jq -e '.clusters[] | select (.cluster_name == "small")' ; then
	exit
fi


cluster=$(databricks clusters create --json '{
  "cluster_name": "small",
  "spark_version": "5.2.x-scala2.11",
  "node_type_id": "Standard_DS3_v2",
  "autoscale" : {
    "min_workers": 1,
    "max_workers": 3
  },
  "spark_env_vars": {
    "PYSPARK_PYTHON": "/databricks/python3/bin/python3"
  },
  "autotermination_minutes": 120
}')


cluster_id=$(echo $cluster | jq -r .cluster_id)

databricks libraries install  --cluster-id $cluster_id --jar dbfs:/model-factory.jar


