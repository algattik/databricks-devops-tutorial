// Databricks notebook source
dbutils.widgets.text("dataset", "/bikesharing-tutorial/holdout.parquet", "Input dataset")
dbutils.widgets.text("model", "/bikesharing-tutorial/bikesharing.model", "Model")
// Use e.g. abfss://bikeshare@algattikerdevopsstore.dfs.core.windows.net/predictions/scratch
dbutils.widgets.text("output", "/bikesharing-tutorial/predictions", "Output")

// COMMAND ----------

val outputDir = dbutils.widgets.get("output")
spark.conf.set("fs.azure.account.key", dbutils.secrets.get(scope = "bikeshare", key = "storagekey"))
spark.conf.set("fs.azure.createRemoteFileSystemDuringInitialization", "true")
dbutils.fs.mkdirs(outputDir)
spark.conf.set("fs.azure.createRemoteFileSystemDuringInitialization", "false")


// COMMAND ----------

import spark.implicits._
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._

import org.apache.spark.ml.PipelineModel

val model = PipelineModel.load(dbutils.widgets.get("model"))

val testData = spark.read.parquet(dbutils.widgets.get("dataset"))

val predictions = model.transform(testData)



predictions.createOrReplaceGlobalTempView("bikesharing_predictions")

display(predictions)

// COMMAND ----------

predictions.write.mode("overwrite").format("json").save(outputDir)

// COMMAND ----------

dbutils.notebook.exit("SUCCESS")