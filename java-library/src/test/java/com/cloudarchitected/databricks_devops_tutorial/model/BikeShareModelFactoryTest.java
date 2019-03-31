package com.cloudarchitected.databricks_devops_tutorial.model;

import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.ml.regression.LinearRegressionModel;
import org.apache.spark.ml.regression.LinearRegressionTrainingSummary;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.Assert.assertEquals;


public class BikeShareModelFactoryTest {

    @Test
    public void modelBuilding() throws IOException {
        //Class under test
        BikeShareModelFactory tester = new BikeShareModelFactory();
        tester.setMaxIterations(1);

        SparkSession spark = SparkSession
                .builder()
                .appName("Java Spark SQL Example")
                .config("spark.master", "local")
                .config("spark.driver.bindAddress", "127.0.0.1")
                .getOrCreate();

        Path tmpFile = Files.createTempFile("bike_data", ".csv");
        // This tells JVM to delete the file on JVM exit.
        tmpFile.toFile().deleteOnExit();
        // writing sample data
        Files.copy(
                getClass().getClassLoader().getResourceAsStream("hour.csv"),
                tmpFile,
                StandardCopyOption.REPLACE_EXISTING);


        // Load training data.
        Dataset<Row> training = spark.read().option("header", true).option("inferSchema", true).csv(tmpFile.toString());
        PipelineModel pipeline = tester.buildModel(training);

        PipelineStage[] stages = pipeline.stages();
        LinearRegressionModel lrModel = (LinearRegressionModel) stages[stages.length - 1];

        // Print the coefficients and intercept for linear regression.
        System.out.println("Coefficients: "
                + lrModel.coefficients() + " Intercept: " + lrModel.intercept());

        // Summarize the model over the training set and print out some metrics.
        LinearRegressionTrainingSummary trainingSummary = lrModel.summary();
        System.out.println("numIterations: " + trainingSummary.totalIterations());
        System.out.println("objectiveHistory: " + Vectors.dense(trainingSummary.objectiveHistory()));
        trainingSummary.residuals().show();
        System.out.println("RMSE: " + trainingSummary.rootMeanSquaredError());
        System.out.println("r2: " + trainingSummary.r2());

        // assert statements
        assertEquals(10, trainingSummary.rootMeanSquaredError(), 10);
    }
}
