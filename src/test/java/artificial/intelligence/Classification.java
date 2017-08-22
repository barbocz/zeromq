package artificial.intelligence;

import weka.classifiers.functions.SMO;
import weka.classifiers.rules.PART;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class Classification {

    public static void getIndicatorName (String trainingSetFile) throws Exception{

        DataSource source = new DataSource(trainingSetFile);
        Instances trainDataset = source.getDataSet();
        //set class index to the last attribute
        trainDataset.setClassIndex(trainDataset.numAttributes()-1);

        //build model
        PART classifier = new PART();
        classifier.buildClassifier(trainDataset);

        SMO smo = new SMO();
        smo.buildClassifier(trainDataset);
        //output model
        //System.out.println(classifier);


        //loop through the new dataset and make predictions

        int correctPrediction=0;
        int falsePrediction=0;
        for (int i = 0; i < trainDataset.numInstances(); i++) {
            //get class double value for current instance
            double actualValue = trainDataset.instance(i).classValue();

            //get Instance object of current instance
            Instance newInst = trainDataset.instance(i);
            //call classifyInstance, which returns a double value for the class
            double predictedValue = classifier.classifyInstance(newInst);
            if ((actualValue==0 && predictedValue==0) || (actualValue==1 && predictedValue==1)) correctPrediction++;
            if ((actualValue==0 && predictedValue==1) || (actualValue==1 && predictedValue==0) ||
                (actualValue==0 && predictedValue==2) || (actualValue==1 && predictedValue==2)   ) falsePrediction++;

            System.out.println(actualValue+" - "+predictedValue+"   ---- "+correctPrediction+"/"+falsePrediction);
        }


    }
}
