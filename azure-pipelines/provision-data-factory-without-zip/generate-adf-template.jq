{
  "$schema": "https://schema.management.azure.com/schemas/2015-01-01/deploymentTemplate.json#",
  "contentVersion": "1.0.0.0",
  "resources": [
    .[]
    | .name = env.DATA_FACTORY_NAME + "/" + .name
    | .apiVersion = "2018-06-01"
    | if(.properties.type=="AzureKeyVault") then
        .properties.typeProperties.baseUrl="https://" + env.KEY_VAULT_NAME + ".vault.azure.net/"
      else .
      end
  ]
}
