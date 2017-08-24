package artificial.intelligence;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.JRip;
import weka.classifiers.rules.PART;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.classifiers.Classifier;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;


public class Classification {

    Classifier classifier;
    Instances dataSet;
    String terminalDirectory;
    String symbol;
    String period;
    String strategy;
    String model;
    String trainingSetFile;
    String trainingSetBaseFile;
    String trainingSetModelFile;

    Classification(String terminalDirectory, String symbol, String period) {
        this.terminalDirectory = terminalDirectory;
        this.symbol = symbol;
        this.period = period;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
        //System.out.println("Strategy set:"+strategy);
    }

    public void setModel(String model) {
        this.model = model;
        //System.out.println("model set:"+model);
    }

    public static void main(String[] args) throws Exception {

        String tDir = "C:\\Program Files\\Global Prime";
        String tFile = "XAUUSD_15_log.arff";
        String tModel = "C:\\Program Files\\Global Prime\\MQL4\\Files\\TrainingSets\\XAUUSD_15_log_JRip.model";
//        long startTime = System.currentTimeMillis();
        Classification cl = new Classification("C:\\Program Files\\Global Prime", "XAUUSD", "15");
        cl.setStrategy("log");
        cl.setModel("JRip");
        //cl.getIndicatorName("Adm");
        cl.train();
        cl.setClassifier();

        double[] instanceValue1 = new double[2];
        instanceValue1[0] = 1265.87100;
        instanceValue1[1] = 2.50000;

        cl.classify(instanceValue1);
//        long stopTime = System.currentTimeMillis();
//        long elapsedTime = stopTime - startTime;
//        System.out.println("elapsedTime: "+elapsedTime);
    }

    public String train() throws Exception {

        String returnMessage = "";
        trainingSetFile = terminalDirectory + "\\MQL4\\Files\\TrainingSets\\" + symbol + "_" + period + "_" + strategy + ".arff";
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(trainingSetFile);
        Instances trainDataset = source.getDataSet();
        //set class index to the last attribute
        trainDataset.setClassIndex(trainDataset.numAttributes() - 1);

        //build model
        PART partClassifier = new PART();
        partClassifier.buildClassifier(trainDataset);


        DecisionTable dtnbClassifier = new DecisionTable();
        dtnbClassifier.buildClassifier(trainDataset);

        JRip jRipClassifier = new JRip();
        jRipClassifier.buildClassifier(trainDataset);

//        ClassificationViaRegression cvrClassifier=new ClassificationViaRegression();
//        cvrClassifier.buildClassifier(trainDataset);

        HashMap<String, AbstractClassifier> classifiers = new HashMap<String, AbstractClassifier>();
        classifiers.put(partClassifier.getClass().getSimpleName(), partClassifier);
        classifiers.put(dtnbClassifier.getClass().getSimpleName(), dtnbClassifier);
        classifiers.put(jRipClassifier.getClass().getSimpleName(), jRipClassifier);
//        classifiers.put(cvrClassifier.getClass().getSimpleName(), cvrClassifier);
        //output model
        //System.out.println(classifier);


        //loop through the new dataset and make predictions

        for (AbstractClassifier classifier : classifiers.values()) {
            String classifierKey = classifier.getClass().getSimpleName();
            trainingSetModelFile = terminalDirectory + "\\MQL4\\Files\\TrainingSets\\" + symbol + "_" + period + "_" + strategy + "_" + classifierKey + ".model";
            weka.core.SerializationHelper.write(trainingSetModelFile, classifier);

            int correctPrediction = 0;
            int falsePrediction = 0;

            for (int i = 0; i < trainDataset.numInstances(); i++) {
                //get class double value for current instance
                double actualValue = trainDataset.instance(i).classValue();

                //get Instance object of current instance
                Instance newInst = trainDataset.instance(i);
                //call classifyInstance, which returns a double value for the class
                double predictedValue = classifier.classifyInstance(newInst);
                if ((actualValue == 0 && predictedValue == 0) || (actualValue == 1 && predictedValue == 1))
                    correctPrediction++;
                if ((actualValue == 0 && predictedValue == 1) || (actualValue == 1 && predictedValue == 0) ||
                        (actualValue == 2 && predictedValue == 0) || (actualValue == 2 && predictedValue == 1))
                    falsePrediction++;
//                System.out.println(actualValue+" - "+predictedValue+"   ---- "+correctPrediction+"/"+falsePrediction);


            }
            int totalPrediction = correctPrediction + falsePrediction;
            double percent = 100 * (double) correctPrediction / totalPrediction;
            if (returnMessage.length() > 0) returnMessage += "|";
            returnMessage += String.format("%s: %d/%d (%.1f%%)", classifierKey, correctPrediction, totalPrediction, percent);
        }
        System.out.println(returnMessage);
        return (returnMessage);
    }

    public void setClassifier() throws Exception {

//        switch (classificationType) {
//            case "PART":
//                PART partClassifier = (PART) weka.core.SerializationHelper.read(modelFile);
//                //System.out.println(action+" command received");
//                break;
//            case "SMO":
//                SMO smoClassifier = (SMO) weka.core.SerializationHelper.read(modelFile);
//                //System.out.println(action+" command received");
//                break;
//        }
        trainingSetModelFile = terminalDirectory + "\\MQL4\\Files\\TrainingSets\\" + symbol + "_" + period + "_" + strategy + "_" + model + ".model";
        classifier = (Classifier) weka.core.SerializationHelper.read(trainingSetModelFile);
        trainingSetFile = terminalDirectory + "\\MQL4\\Files\\TrainingSets\\" + symbol + "_" + period + "_" + strategy + "_base.arff";
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(trainingSetFile);
        dataSet = source.getDataSet();
        //set class index to the last attribute
        dataSet.setClassIndex(dataSet.numAttributes() - 1);

//        for (int i = 0; i < dataSet.numInstances(); i++) {
//            //get class double value for current instance
//            double actualValue = dataSet.instance(i).classValue();
//
//            //get Instance object of current instance
//            Instance newInst = dataSet.instance(i);
//            //call classifyInstance, which returns a double value for the class
//            double predictedValue = classifier.classifyInstance(newInst);
//
//            System.out.println(actualValue+" - "+predictedValue);
//        }
//        System.out.println("--------------------------------------------");


    }


    public String classify(double[] parameters) throws Exception {

        DenseInstance denseInstance1 = new DenseInstance(0.0, parameters);
        //System.out.println("pre num: " + dataSet.numInstances());
        dataSet.add(denseInstance1);
        //System.out.println("after num: " + dataSet.numInstances());

//            DenseInstance denseInstance1 = new DenseInstance(1.0, instanceValue1);
        double newPredictedValue = classifier.classifyInstance(dataSet.instance(dataSet.numInstances() - 1));
        System.out.println("New prediction: " + newPredictedValue);
        return (Double.toString(newPredictedValue));
    }

    public String getIndicatorName(String prefix) {

        String indicatorDirectory = terminalDirectory + "\\MQL4\\Indicators";

        String directoryPrefix = "";
        String items[] = prefix.split("\\/");

        if (items.length > 1) {
            for (int i = 0; i < items.length - 1; i++) {
                indicatorDirectory += "\\" + items[i];
                directoryPrefix = items[i] + "/";
            }
            prefix = items[items.length - 1];
        }
        File dir = new File(indicatorDirectory);
        FilenameFilter select = new MT4FileAccess.FileListFilter(prefix, "ex4");
        File[] files = dir.listFiles(select);

        String dirList = "";
        for (File file : files) {
            String fileName = directoryPrefix + file.getName().replace(".ex4", "");
            if (dirList.length() == 0) dirList += fileName;
            else dirList += "|" + fileName;
        }
//        System.out.println(dirList);
        return (dirList);
    }

    static class FileListFilter implements FilenameFilter {
        private String name;
        private String extension;

        public FileListFilter(String name, String extension) {
            this.name = name;
            this.extension = extension;
        }

        public boolean accept(File directory, String filename) {

            return (filename.startsWith(name) && filename.endsWith('.' + extension));
        }
    }


}
