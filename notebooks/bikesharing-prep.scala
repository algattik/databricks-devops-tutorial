// Databricks notebook source
import spark.implicits._
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._

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

// Split into training, test and holdout set
val splitsRDD = dataset.rdd.randomSplit(Array(0.8, 0.1, 0.1), 0)
val splits = splitsRDD.map(spark.createDataFrame(_, dataset.schema))

// Save away the holdout set for future prediction
splits(0).write.mode("overwrite").save("/bikesharing-tutorial/train.parquet")
splits(1).write.mode("overwrite").save("/bikesharing-tutorial/test.parquet")
splits(2).write.mode("overwrite").save("/bikesharing-tutorial/holdout.parquet")

// COMMAND ----------

dataset.schema

// COMMAND ----------

