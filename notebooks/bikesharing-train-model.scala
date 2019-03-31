// Databricks notebook source
dbutils.widgets.text("train", "/bikesharing-tutorial/train.parquet", "Train dataset")
dbutils.widgets.text("test", "/bikesharing-tutorial/test.parquet", "Test dataset")
dbutils.widgets.text("model", "/bikesharing-tutorial/bikesharing.model", "Output model")

// COMMAND ----------

import spark.implicits._
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._

// Load training data.
val dataset = spark.read.parquet(dbutils.widgets.get("train"))

val modelFactory = new com.cloudarchitected.databricks_devops_tutorial.model.BikeShareModelFactory
val pipeline = modelFactory.buildModel(dataset)

// COMMAND ----------

import  org.apache.spark.ml.evaluation.RegressionEvaluator
val train = spark.read.parquet(dbutils.widgets.get("train"))
val predictions = pipeline.transform(train)
val evaluator = new RegressionEvaluator()
  .setMetricName("rmse")
  .setLabelCol("cnt")
  .setPredictionCol("prediction")
val rmse = evaluator.evaluate(predictions)

println("RMSE on test data = " + rmse)

pipeline.write.overwrite().save(dbutils.widgets.get("model"))

// COMMAND ----------

display(predictions)

// COMMAND ----------

dbutils.notebook.exit("SUCCESS")