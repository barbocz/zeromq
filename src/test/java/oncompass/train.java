package oncompass;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.JRip;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomTree;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.classifiers.Classifier;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;

public class train {
    public static void main(String[] args) throws Exception {
        String mutationList = "APC-Q1367*,KRAS-G13D,TP53-S215R";
        System.out.println("Mutations: "+mutationList);
        String mutations[] = mutationList.split(",");
        //System.out.println(s[0] + "," + s[2]);

        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3300/calculatornew?useSSL=false";
        String username = "root";
        String password = "lopikula";

// there's probably a better way to do this
        Connection conn = null;
        Statement statement = null;

// make the connection
        Class.forName(driver);
        conn = DriverManager.getConnection(url, username, password);
        statement = conn.createStatement();
// create the statement, and run the select query
        int mutationCounter=0;

        ResultSet resultSet = statement.executeQuery("SELECT max(id) as mutationCounter FROM tmp_mutation");
        if (resultSet.next()) mutationCounter=resultSet.getInt("mutationCounter");
        double[] parameters = new double[mutationCounter];
        Arrays.fill(parameters, 1.0);

        for (int i = 0; i < mutations.length; i++) {
            resultSet = statement.executeQuery("SELECT id FROM tmp_mutation where name='"+mutations[i]+"'");
            //if (resultSet.next()) System.out.println("ID: "+resultSet.getInt("id"));
            if (resultSet.next()) parameters[resultSet.getInt("id")]=0.0;
        }

        double prediction=classify(parameters);
        //System.out.println("prediction: "+prediction);
        resultSet = statement.executeQuery("SELECT Concat(tumor_name,\"-\",histology_name) as name FROM tmp_tumorset where id="+(prediction+1));
        //if (resultSet.next()) System.out.println("Prediction: "+resultSet.getString("name"));
        if (resultSet.next()) System.out.println("Prediction: "+resultSet.getString("name"));
        conn.close();




    }

    public static double classify(double[] parameters) throws Exception {

//        Arrays.fill(parameters, 1.0);
//        parameters[23] = 0.0;
//        parameters[122]=0.0;
//        parameters[749]=0.0;
        Instances dataSet;
        ConverterUtils.DataSource source = new ConverterUtils.DataSource("tumorset_base.arff");
        dataSet = source.getDataSet();
        dataSet.setClassIndex(dataSet.numAttributes() - 1);
        Classifier classifier = (Classifier) weka.core.SerializationHelper.read("PART.model");

//        Instance newInst = dataSet.instance(0);
//        double predictedValue = classifier.classifyInstance(newInst);

        DenseInstance denseInstance1 = new DenseInstance(0.0, parameters);
        //System.out.println("pre num: " + dataSet.numInstances());
        dataSet.add(denseInstance1);
        //System.out.println("after num: " + dataSet.numInstances());
//            DenseInstance denseInstance1 = new DenseInstance(1.0, instanceValue1);
        double newPredictedValue = classifier.classifyInstance(dataSet.instance(0));
        //System.out.println("New prediction: " + newPredictedValue);
        //System.out.println("New prediction: " + newPredictedValue);
        return(newPredictedValue);
    }

    public static void train() throws Exception {
        String trainingSetFile = "tumorset.arff";
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(trainingSetFile);
        Instances trainDataset = source.getDataSet();

        trainDataset.setClassIndex(trainDataset.numAttributes() - 1);
        //PART classifier = new PART();
        DecisionTable classifier = new DecisionTable();
        classifier.buildClassifier(trainDataset);


        String trainingSetModelFile = classifier.getClass().getSimpleName() + ".model";
        weka.core.SerializationHelper.write(trainingSetModelFile, classifier);


        int correctPrediction = 0;
        int falsePrediction = 0;

        for (int i = 0; i < trainDataset.numInstances(); i++) {
            //get class double value for current instance
            Instance newInst = trainDataset.instance(i);

            double actualValue = newInst.classValue();

            //get Instance object of current instance

            //call classifyInstance, which returns a double value for the class
            double predictedValue = classifier.classifyInstance(newInst);
            if (actualValue == predictedValue) correctPrediction++;
            else falsePrediction++;

        }
//            writer.close();
        int totalPrediction = correctPrediction + falsePrediction;
        double percent = 100 * (double) correctPrediction / totalPrediction;

        System.out.println("correctPrediction: " + correctPrediction + "  falsePrediction: " + falsePrediction + "  hit ratio: " + percent);


    }

    public static void createTrainingSet() throws Exception {

        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3300/calculatornew?useSSL=false";
        String username = "root";
        String password = "lopikula";

// there's probably a better way to do this
        Connection conn = null;
        Statement statement = null;
        Statement statement1 = null;

// make the connection
        Class.forName(driver);
        conn = DriverManager.getConnection(url, username, password);

// create the statement, and run the select query
        statement = conn.createStatement();
        statement1 = conn.createStatement();

//        ResultSet resultSet  = statement.executeQuery("SELECT id, login FROM USER");
//        while (resultSet.next()) {
//            Double host = resultSet.getDouble("id");
//            String user = resultSet.getString("login");
//            System.out.println("host, user = " + host + ", " + user);
//        }


        PrintWriter tumorsetFile = new PrintWriter("tumorset.arff");
        tumorsetFile.println("@relation selection");
        tumorsetFile.println("");

        ResultSet resultSet = statement.executeQuery("SELECT name FROM tmp_mutation");
        int mutationCounter = 0;
        while (resultSet.next()) {
            String mutationName = resultSet.getString("name");
            mutationName = mutationName.replace(";", "-");
            mutationName = mutationName.replace("(", "");
            mutationName = mutationName.replace(")", "");
            mutationName = mutationName.replace(" ", "-");
            mutationName = mutationName.replace("--", "-");
            tumorsetFile.println("@attribute " + mutationName + " {y,n}");
            mutationCounter++;
            //System.out.println(i+". "+resultSet.getString("name"));

        }

        String[] tumorSets = new String[400];
        tumorsetFile.print("@attribute tumorset {");
        resultSet = statement.executeQuery("SELECT id,Concat(tumor_name,\"-\",histology_name) as name FROM tmp_tumorset");
        while (resultSet.next()) {
            String separator = ",";
            if (resultSet.isLast()) separator = "";
            String tumorSetName = resultSet.getString("name");
            tumorSetName = tumorSetName.replace(" ", "_");
            tumorSetName = tumorSetName.replace(",", "_");
            tumorSets[resultSet.getInt("id")] = tumorSetName;
            tumorsetFile.print(tumorSetName + separator);
        }
        tumorsetFile.println("}");
        tumorsetFile.println("");
        tumorsetFile.println("@data");

        String Query = "SELECT TMP_MUTATION.id as mutation_id,TMP_TUMORSET.id as tumorset_id FROM MOLECULARPROFILE,MOLECULARPROFILE_MUTATION,MUTATION_NAME,PATIENT_TUMOR,TMP_MUTATION,TMP_TUMORSET where " +
                "MOLECULARPROFILE.id=MOLECULARPROFILE_MUTATION.molecularprofile_id and MUTATION_NAME.id=MOLECULARPROFILE_MUTATION.mutation_name_id and " +
                "MOLECULARPROFILE.tumor_id=PATIENT_TUMOR.id and TMP_MUTATION.mutation_id=MUTATION_NAME.mutation_id and TMP_TUMORSET.tumor_id=PATIENT_TUMOR.tumor_type_id and " +
                "TMP_TUMORSET.histology_id=PATIENT_TUMOR.histology_type_id and molecularprofile_id=";

        String[] features = new String[mutationCounter + 1];


        int tumorset_id = 0;
        resultSet = statement.executeQuery("SELECT molecularprofile_id FROM MOLECULARPROFILE_MUTATION");
        while (resultSet.next()) {
            ResultSet resultSetMolProf = statement1.executeQuery(Query + resultSet.getInt("molecularprofile_id"));
            Arrays.fill(features, "n");
            //System.out.println(resultSet.getInt("molecularprofile_id")+" ---------------------------------");
            while (resultSetMolProf.next()) {
                //System.out.println(resultSet.getInt("molecularprofile_id")+":   " + resultSetMolProf.getInt("mutation_id") + " - " + resultSetMolProf.getInt("tumorset_id"));
                features[resultSetMolProf.getInt("mutation_id")] = "y";
                tumorset_id = resultSetMolProf.getInt("tumorset_id");
            }

            String rw = "";
            for (int i = 0; i < mutationCounter; i++) {
                rw += features[i] + ",";
            }
            rw += tumorSets[tumorset_id];
            tumorsetFile.println(rw);
        }


        tumorsetFile.close();

        conn.close();
    }
}