#!/bin/bash
set -euo pipefail

databricks workspace import_dir -o notebooks /devops-deployed

run=$(databricks runs submit --json '{ 
  "name": "IntegrationTest",
    "new_cluster": {
      "spark_version": "5.2.x-scala2.11",
      "node_type_id": "Standard_DS3_v2",
      "num_workers": 1
    },
    "libraries": [
      {
        "jar": "dbfs:/model-factory.jar"
      }
    ],
  "timeout_seconds": 1200,
  "notebook_task": { 
    "notebook_path": "/devops-deployed/bikesharing-inttest",
    "base_parameters": {
        "output": "abfss://bikeshare@'$STORAGE_ACCOUNT_NAME'.dfs.core.windows.net/predictions/int-test"
      }
  }
}')
run_id=$(echo $run | jq .run_id)

until [ "$(echo $run | jq -r .state.life_cycle_state)" = "TERMINATED" ]; do echo Waiting for run completion...; sleep 5; run=$(databricks runs get --run-id $run_id); echo $run | jq .run_page_url; done

#Output to log
echo $run | jq .

# Fail stage if not successful
test $(echo $run | jq -r .state.result_state) = "SUCCESS"

