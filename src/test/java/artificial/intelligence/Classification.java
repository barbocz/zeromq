package artificial.intelligence;

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

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.HashMap;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Random;


public class Classification {

    Classifier classifier;
    Instances dataSet;
    String terminalDirectory;
    String symbol;
    String period;
    String strategy;
    String model;
    String aiType; // Classification=0, Regression=1
    String trainingSetFile;
    String trainingSetBaseFile;
    String trainingSetModelFile;

    Classification(String terminalDirectory, String symbol, String period) throws Exception {
        this.terminalDirectory = terminalDirectory;
        this.symbol = symbol;
        this.period = period;
        Path path = Paths.get(terminalDirectory + "\\MQL4\\Files\\Strategies");
        Files.createDirectories(path);
        path = Paths.get(terminalDirectory + "\\MQL4\\Files\\Predictions");
        Files.createDirectories(path);
        path = Paths.get(terminalDirectory + "\\MQL4\\Files\\TrainingSets");
        Files.createDirectories(path);
        path = Paths.get(terminalDirectory + "\\tester\\files\\Predictions");
        Files.createDirectories(path);

    }

    public static void main(String[] args) throws Exception {

        String tDir = "C:\\Program Files\\Global Prime";
        String tFile = "XAUUSD_15_log.arff";
        String tModel = "C:\\Program Files\\Global Prime1\\MQL4\\Files\\TrainingSets\\XAUUSD_15_log_JRip.model";
//        long startTime = System.currentTimeMillis();
        Classification cl = new Classification("C:\\Program Files\\Global Prime", "EURUSD", "15");
        cl.setStrategy("cluster");
        //cl.setModel("IBk");
        cl.train();
//        cl.copyStrategyToTester("conjid");
        //cl.setClassifier();
        //cl.tempClassify();

        //cl.getIndicatorName("Adm");
        //cl.train();
        cl.testClassify("PART");
//        cl.setClassifier();
//        cl.getIndicatorName("AI/Conjunction/H");
//
//        double[] instanceValue1 = new double[2];
//        instanceValue1[0] = 1265.87100;
//        instanceValue1[1] = 2.50000;

//        cl.classify(instanceValue1);
//        long stopTime = System.currentTimeMillis();
//        long elapsedTime = stopTime - startTime;
//        System.out.println("elapsedTime: "+elapsedTime);
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
        //System.out.println("Strategy set:"+strategy);
    }

    public void copyPredictionsToTester(String strategy) throws Exception {
        Path path = Paths.get(terminalDirectory + "\\tester\\files\\Trainings");
        Files.createDirectories(path);

        Path FROM = Paths.get(terminalDirectory + "\\MQL4\\Files\\Trainings\\" + strategy + ".txt");
        Path TO = Paths.get(terminalDirectory + "\\tester\\files\\Trainings\\" + strategy + ".txt");
        //overwrite existing file, if exists
        CopyOption[] options = new CopyOption[]{
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES
        };
        Files.copy(FROM, TO, options);
    }


    public void setModel(String model) {
        this.model = model;
        //System.out.println("model set:"+model);
    }


    public String train() throws Exception {

        String returnMessage = "";
        trainingSetFile = terminalDirectory + "\\MQL4\\Files\\TrainingSets\\" + symbol + "_" + period + "_" + strategy + ".arff";
        //System.out.println("trainingSetFile: "+trainingSetFile);
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(trainingSetFile);
        Instances trainDataset = source.getDataSet();


        //set class index to the last attribute
//
        trainDataset.setClassIndex(trainDataset.numAttributes() - 1);
        int dateTimeIndex = trainDataset.numAttributes() - 2;
//        trainDataset.remove(dateTimeIndex);
        trainDataset.deleteAttributeAt(dateTimeIndex);


        //build model
        PART partClassifier = new PART();
        partClassifier.buildClassifier(trainDataset);


        DecisionTable dtnbClassifier = new DecisionTable();
        dtnbClassifier.buildClassifier(trainDataset);

        JRip jRipClassifier = new JRip();
        jRipClassifier.buildClassifier(trainDataset);

//        Evaluation eval = new Evaluation(trainDataset);
//        Random rand = new Random(1);
//        eval.crossValidateModel(jRipClassifier, trainDataset, 10, rand);
//        System.out.println(eval.toSummaryString("Evaluation results:\n", false));

        J48 j48Classifier = new J48();
        j48Classifier.buildClassifier(trainDataset);

//        RandomTree randomTreeClassifier = new RandomTree();
//        randomTreeClassifier.buildClassifier(trainDataset);


//        ClassificationViaRegression cvrClassifier=new ClassificationViaRegression();
//        cvrClassifier.buildClassifier(trainDataset);

        HashMap<String, AbstractClassifier> classifiers = new HashMap<String, AbstractClassifier>();
        classifiers.put(partClassifier.getClass().getSimpleName(), partClassifier);
        classifiers.put(dtnbClassifier.getClass().getSimpleName(), dtnbClassifier);
        classifiers.put(jRipClassifier.getClass().getSimpleName(), jRipClassifier);
        classifiers.put(j48Classifier.getClass().getSimpleName(), j48Classifier);
//        classifiers.put(cvrClassifier.getClass().getSimpleName(), cvrClassifier);
        //output model
        //System.out.println(classifier);


        //loop through the new dataset and make predictions

        for (AbstractClassifier classifier : classifiers.values()) {
            String classifierKey = classifier.getClass().getSimpleName();
            trainingSetModelFile = terminalDirectory + "\\MQL4\\Files\\TrainingSets\\" + symbol + "_" + period + "_" + strategy + "_" + classifierKey + ".model";
            weka.core.SerializationHelper.write(trainingSetModelFile, classifier);
//            PrintWriter writer = new PrintWriter(terminalDirectory+"\\tester\\files\\"
//                    + symbol + "_" + period + "_" + strategy + "_" + classifierKey+".txt");

            int correctPrediction = 0;
            int falsePrediction = 0;

            for (int i = 0; i < trainDataset.numInstances(); i++) {
                //get class double value for current instance
                Instance newInst = trainDataset.instance(i);

                double actualValue = newInst.classValue();

                //get Instance object of current instance

                //call classifyInstance, which returns a double value for the class
                double predictedValue = classifier.classifyInstance(newInst);
                if ((actualValue == 0 && predictedValue == 0) || (actualValue == 1 && predictedValue == 1))
                    correctPrediction++;
                if ((actualValue == 0 && predictedValue == 1) || (actualValue == 1 && predictedValue == 0) ||
                        (actualValue == 2 && predictedValue == 0) || (actualValue == 2 && predictedValue == 1))
                    falsePrediction++;
//                System.out.println(actualValue+" - "+predictedValue+"   ---- "+correctPrediction+"/"+falsePrediction);
//                writer.println(String.format("%s|%s|%s",dateInst.value(dateTimeIndex),actualValue,predictedValue));
                //System.out.println(String.format("%s|%s|%s%%",new Double(newInst.value(dateTimeIndex)).toString(),actualValue,predictedValue));


            }
//            writer.close();
            int totalPrediction = correctPrediction + falsePrediction;
            double percent = 100 * (double) correctPrediction / totalPrediction;
            if (returnMessage.length() > 0) returnMessage += "|";
            returnMessage += String.format("%s: %d/%d (%.1f%%)", classifierKey, correctPrediction, totalPrediction, percent);
        }

        System.out.println(returnMessage);
        return (returnMessage);

    }


    public String trainReg() throws Exception {

        String returnMessage = "";
        trainingSetFile = terminalDirectory + "\\MQL4\\Files\\TrainingSets\\" + symbol + "_" + period + "_" + strategy + ".arff";
        //System.out.println("trainingSetFile: "+trainingSetFile);
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(trainingSetFile);
        Instances trainDataset = source.getDataSet();

        //set class index to the last attribute
//
        trainDataset.setClassIndex(trainDataset.numAttributes() - 1);
        int dateTimeIndex = trainDataset.numAttributes() - 2;
        //        trainDataset.remove(dateTimeIndex);
        trainDataset.deleteAttributeAt(dateTimeIndex);


        //build model
        IBk ibkClassifier = new IBk();
        ibkClassifier.buildClassifier(trainDataset);


//        RandomTree randomTreeClassifier = new RandomTree();
//        randomTreeClassifier.buildClassifier(trainDataset);


//        ClassificationViaRegression cvrClassifier=new ClassificationViaRegression();
//        cvrClassifier.buildClassifier(trainDataset);

        HashMap<String, AbstractClassifier> classifiers = new HashMap<String, AbstractClassifier>();
        classifiers.put(ibkClassifier.getClass().getSimpleName(), ibkClassifier);

//        classifiers.put(cvrClassifier.getClass().getSimpleName(), cvrClassifier);
        //output model
        //System.out.println(classifier);


        //loop through the new dataset and make predictions

        trainingSetFile = terminalDirectory + "\\MQL4\\Files\\TrainingSets\\" + symbol + "_" + period + "_" + strategy + "_test.arff";
        //System.out.println("trainingSetFile: "+trainingSetFile);
        source = new ConverterUtils.DataSource(trainingSetFile);
        trainDataset = source.getDataSet();
        Instances trainDatasetWithDate = source.getDataSet();
        trainDataset.setClassIndex(trainDataset.numAttributes() - 1);

        //        trainDataset.remove(dateTimeIndex);
        trainDataset.deleteAttributeAt(dateTimeIndex);

        for (AbstractClassifier classifier : classifiers.values()) {
            String classifierKey = classifier.getClass().getSimpleName();
            trainingSetModelFile = terminalDirectory + "\\MQL4\\Files\\TrainingSets\\" + symbol + "_" + period + "_" + strategy + "_" + classifierKey + ".model";
            weka.core.SerializationHelper.write(trainingSetModelFile, classifier);
            PrintWriter writerForIndicator = new PrintWriter(terminalDirectory + "\\MQL4\\Files\\Predictions\\"
                    + symbol + "_" + period + "_" + strategy + "_" + classifierKey + ".txt");
            PrintWriter writerForBackTester = new PrintWriter(terminalDirectory + "\\tester\\files\\Predictions\\"
                    + symbol + "_" + period + "_" + strategy + "_" + classifierKey + ".txt");

            int correctPrediction = 0;
            int falsePrediction = 0;

            for (int i = 0; i < trainDataset.numInstances(); i++) {
                //get class double value for current instance
                Instance newInst = trainDataset.instance(i);
                Instance dateInst = trainDatasetWithDate.instance(i);

                double actualValue = newInst.classValue();

                //get Instance object of current instance

                //call classifyInstance, which returns a double value for the class
                double predictedValue = classifier.classifyInstance(newInst);
                if ((actualValue == 0 && predictedValue == 0) || (actualValue == 1 && predictedValue == 1))
                    correctPrediction++;
                if ((actualValue == 0 && predictedValue == 1) || (actualValue == 1 && predictedValue == 0) ||
                        (actualValue == 2 && predictedValue == 0) || (actualValue == 2 && predictedValue == 1))
                    falsePrediction++;
//                System.out.println(actualValue+" - "+predictedValue+"   ---- "+correctPrediction+"/"+falsePrediction);

                String output = String.format("%s|%s|%s", dateInst.value(dateTimeIndex), actualValue, predictedValue);
                writerForIndicator.println(output);
                writerForBackTester.println(output);

                //System.out.println(String.format("%s|%s|%s%%",new Double(newInst.value(dateTimeIndex)).toString(),actualValue,predictedValue));


            }
            writerForIndicator.close();
            writerForBackTester.close();
//            int totalPrediction = correctPrediction + falsePrediction;
//            double percent = 100 * (double) correctPrediction / totalPrediction;
//            if (returnMessage.length() > 0) returnMessage += "|";
//            returnMessage += String.format("%s: %d/%d (%.1f%%)", classifierKey, correctPrediction, totalPrediction, percent);
            returnMessage = "OK";
        }

        System.out.println(returnMessage);
        return (returnMessage);

    }


    public String testClassify(String testModel) throws Exception {


        String returnMessage = "";
        trainingSetFile = terminalDirectory + "\\MQL4\\Files\\TrainingSets\\" + symbol + "_" + period + "_" + strategy + "_test.arff";
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(trainingSetFile);
        Instances trainDataset = source.getDataSet();
        Instances trainDatasetWithDate = source.getDataSet();


        //set class index to the last attribute
//        trainDataset.remove(1);
        trainDataset.setClassIndex(trainDataset.numAttributes() - 1);
        int dateTimeIndex = trainDataset.numAttributes() - 2;
        trainDataset.deleteAttributeAt(dateTimeIndex);

        HashMap<String, Classifier> classifiers = new HashMap<String, Classifier>();

        if (testModel.equals("ALL") || testModel.equals("DecisionTable")) {
            trainingSetModelFile = terminalDirectory + "\\MQL4\\Files\\TrainingSets\\" + symbol + "_" + period + "_" + strategy + "_DecisionTable.model";
            classifier = (Classifier) weka.core.SerializationHelper.read(trainingSetModelFile);
            classifiers.put(classifier.getClass().getSimpleName(), classifier);
        }

        if (testModel.equals("ALL") || testModel.equals("PART")) {
            trainingSetModelFile = terminalDirectory + "\\MQL4\\Files\\TrainingSets\\" + symbol + "_" + period + "_" + strategy + "_PART.model";
            classifier = (Classifier) weka.core.SerializationHelper.read(trainingSetModelFile);
            classifiers.put(classifier.getClass().getSimpleName(), classifier);
        }

        if (testModel.equals("ALL") || testModel.equals("JRip")) {
            trainingSetModelFile = terminalDirectory + "\\MQL4\\Files\\TrainingSets\\" + symbol + "_" + period + "_" + strategy + "_JRip.model";
            classifier = (Classifier) weka.core.SerializationHelper.read(trainingSetModelFile);
            classifiers.put(classifier.getClass().getSimpleName(), classifier);
        }

        if (testModel.equals("ALL") || testModel.equals("J48")) {
            trainingSetModelFile = terminalDirectory + "\\MQL4\\Files\\TrainingSets\\" + symbol + "_" + period + "_" + strategy + "_J48.model";
            classifier = (Classifier) weka.core.SerializationHelper.read(trainingSetModelFile);
            classifiers.put(classifier.getClass().getSimpleName(), classifier);
        }


        for (Classifier classifier : classifiers.values()) {

            String classifierKey = classifier.getClass().getSimpleName();

            PrintWriter writerForIndicator = new PrintWriter(terminalDirectory + "\\MQL4\\Files\\Predictions\\"
                    + symbol + "_" + period + "_" + strategy + "_" + classifierKey + ".txt");
            PrintWriter writerForBackTester = new PrintWriter(terminalDirectory + "\\tester\\files\\Predictions\\"
                    + symbol + "_" + period + "_" + strategy + "_" + classifierKey + ".txt");

            int correctPrediction = 0;
            int falsePrediction = 0;
            int totalBuyPrediction = 0;
            int totalSellprediction = 0;
            int correctBuyPrediction = 0;
            int correctSellPrediction = 0;

            for (int i = 0; i < trainDataset.numInstances(); i++) {
                //get class double value for current instance
                Instance newInst = trainDataset.instance(i);
                double actualValue = newInst.classValue();
                Instance dateInst = trainDatasetWithDate.instance(i);

                double predictedValue = classifier.classifyInstance(newInst);

//                double[] predictionDistribution =
//                        classifier.distributionForInstance(trainDataset.instance(i));


                if ((actualValue == 0 && predictedValue == 0) || (actualValue == 1 && predictedValue == 1))
                    correctPrediction++;
                if ((actualValue == 0 && predictedValue == 1) || (actualValue == 1 && predictedValue == 0) ||
                        (actualValue == 2 && predictedValue == 0) || (actualValue == 2 && predictedValue == 1))
                    falsePrediction++;


                if (predictedValue == 0) totalBuyPrediction++;
                if (predictedValue == 0 && actualValue == 0) correctBuyPrediction++;
                if (predictedValue == 1) totalSellprediction++;
                if (predictedValue == 1 && actualValue == 1) correctSellPrediction++;
//                System.out.println(actualValue+" - "+predictedValue+"   ---- "+correctPrediction+"/"+falsePrediction);
                String output = String.format("%s|%s|%s", dateInst.value(dateTimeIndex), actualValue, predictedValue);
                writerForIndicator.println(output);
                writerForBackTester.println(output);
            }
            //System.out.println(String.format("%s|%s|%s%%",new Double(newInst.value(dateTimeIndex)).toString(),actualValue,predictedValue));


            writerForIndicator.close();
            writerForBackTester.close();
            int totalPrediction = correctPrediction + falsePrediction;
            double percent = 100 * (double) correctPrediction / totalPrediction;
            if (returnMessage.length() > 0) returnMessage += "|";
//            returnMessage += String.format("%s: %d/%d (%.1f%%)", classifierKey, correctPrediction, totalPrediction, percent);
            returnMessage += String.format("%s: %d/%d (%.1f%%) -> Buy: %d/%d, Sell: %d/%d", classifierKey, correctPrediction, totalPrediction, percent,
                    correctBuyPrediction, totalBuyPrediction, correctSellPrediction, totalSellprediction);
        }

        System.out.println(returnMessage);
        return (returnMessage);

    }

    public String tempClassify() throws Exception {

        String returnMessage = "";
//        trainingSetModelFile = terminalDirectory + "\\MQL4\\Files\\TrainingSets\\" + symbol + "_" + period + "_" + strategy + "_" + model + ".model";
//        classifier = (Classifier) weka.core.SerializationHelper.read(trainingSetModelFile);
        trainingSetFile = terminalDirectory + "\\MQL4\\Files\\TrainingSets\\" + symbol + "_" + period + "_" + strategy + ".arff";
        System.out.println("temp trainingSetFile:" + trainingSetFile);
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(trainingSetFile);
        Instances trainDataset = source.getDataSet();
        //set class index to the last attribute
        trainDataset.setClassIndex(trainDataset.numAttributes() - 1);
        int dateTimeIndex = trainDataset.numAttributes() - 2;

        //loop through the new dataset and make predictions
        PrintWriter writer = new PrintWriter(terminalDirectory + "\\tester\\files\\"
                + symbol + "_" + period + "_" + strategy + "_" + model + ".txt");

        int correctPrediction = 0;
        int falsePrediction = 0;

        for (int i = trainDataset.numInstances() - 1; i > -1; i--) {
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
            //System.out.println(i+" - "+actualValue + " - " + predictedValue + "   ---- " + correctPrediction + "/" + falsePrediction);
            returnMessage += predictedValue + "|";
            writer.println(String.format("%s|%s|%s", newInst.value(dateTimeIndex), actualValue, predictedValue));

        }
        writer.close();
        int totalPrediction = correctPrediction + falsePrediction;
        double percent = 100 * (double) correctPrediction / totalPrediction;
        System.out.println(String.format("result: %d/%d (%.1f%%)", correctPrediction, totalPrediction, percent));
        //System.out.println("ms size: "+returnMessage.length());
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
                directoryPrefix += items[i] + "/";
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
        //System.out.println(dirList);
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
