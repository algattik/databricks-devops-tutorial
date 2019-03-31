package com.cloudarchitected.databricks_devops_tutorial.model;

import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.evaluation.RegressionEvaluator;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.regression.LinearRegression;
import org.apache.spark.sql.DataFrameNaFunctions;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public class BikeShareModelFactory {


    private int maxIterations = 10;

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public PipelineModel buildModel(Dataset<Row> training) {

        VectorAssembler assembler = new VectorAssembler()
                .setInputCols(new String[]{"season","hr","holiday","weekday","workingday","weathersit","temp","atemp","hum","windspeed"})
                .setOutputCol("features");

        LinearRegression lr = new LinearRegression()
                .setLabelCol("cnt")
                .setMaxIter(maxIterations)
                .setRegParam(0.3)
                .setElasticNetParam(0.8);

        Pipeline pipeline = new Pipeline().setStages(new PipelineStage[]{assembler, lr});
        PipelineModel fitted = pipeline.fit(training);

        return fitted;


    }
}
