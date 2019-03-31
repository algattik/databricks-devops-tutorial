// Databricks notebook source
// Use e.g. abfss://bikeshare@algattikerdevopsstore.dfs.core.windows.net/predictions/scratch
dbutils.widgets.text("output", "/bikesharing-tutorial/inttest-predictions", "Output")

// COMMAND ----------

spark.conf.set("fs.azure.account.key", dbutils.secrets.get(scope = "bikeshare", key = "storagekey"))


// COMMAND ----------

val test_dir = "/bikesharing-tutorial/integration-test/"
dbutils.fs.mkdirs(test_dir)

// COMMAND ----------

import spark.implicits._
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._

// Load training data.
val schema = StructType(Seq(
  StructField("instant",IntegerType),
  StructField("dteday",TimestampType),
  StructField("season",IntegerType),
  StructField("yr",IntegerType),
  StructField("mnth",IntegerType),
  StructField("hr",IntegerType),
  StructField("holiday",IntegerType),
  StructField("weekday",IntegerType),
  StructField("workingday",IntegerType),
  StructField("weathersit",IntegerType),
  StructField("temp",DoubleType),
  StructField("atemp",DoubleType),
  StructField("hum",DoubleType),
  StructField("windspeed",DoubleType),
  StructField("casual",IntegerType),
  StructField("registered",IntegerType),
  StructField("cnt",IntegerType)
))

// Load training data.
val dataset = spark.read
  .schema(schema)
  .option("header", true)
  .csv("/databricks-datasets/bikeSharing/data-001/hour.csv")
  .cache

val lines = dataset.withColumn("line", monotonically_increasing_id).cache

lines.where('line.between( 0, 9)).write.mode("overwrite").save(test_dir + "train.parquet")
lines.where('line.between(10,19)).write.mode("overwrite").save(test_dir + "test.parquet")
lines.where('line.between(20,29)).write.mode("overwrite").save(test_dir + "holdout.parquet")

// COMMAND ----------

dbutils.notebook.run("bikesharing-train-model", /* timeout_seconds = */ 120, Map(
    "train" -> (test_dir + "train.parquet"),
    "test" -> (test_dir + "test.parquet"),
    "model" -> (test_dir + "bikesharing.model")
  )
)

// COMMAND ----------

val predictionsDir = dbutils.widgets.get("output")
dbutils.notebook.run("bikesharing-apply-model", /* timeout_seconds = */ 120, Map(
    "model" -> (test_dir + "bikesharing.model"),
    "dataset" -> (test_dir + "holdout.parquet"),
    "output" -> predictionsDir
  )
)

// COMMAND ----------

import  org.apache.spark.ml.evaluation.RegressionEvaluator

val predictions = spark.read.json(predictionsDir)

val evaluator = new RegressionEvaluator()
  .setMetricName("rmse")
  .setLabelCol("cnt")
  .setPredictionCol("prediction")
val rmse = evaluator.evaluate(predictions)

println("RMSE on test data = " + rmse)

assert(predictions.count == 10)
assert(rmse < 100)

// COMMAND ----------

