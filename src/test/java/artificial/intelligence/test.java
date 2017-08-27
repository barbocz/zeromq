package artificial.intelligence;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class test {
    public static void main(String[] args) throws Exception
    {

        String tDir="C:\\Program Files\\Global Prime\\MQL4\\Files\\TrainingSets";
        String tFile="XAUUSD_15_log.arff";
        String tModel="C:\\Program Files\\Global Prime\\MQL4\\Files\\TrainingSets\\XAUUSD_15_log_JRip.model";

        try{
            PrintWriter writer = new PrintWriter("C:\\Program Files\\Global Prime1\\MQL4\\Files\\TrainingSets\\test.txt", "UTF-8");
            writer.println("The first line");
            writer.println("The second line");
            writer.close();
        } catch (IOException e) {
            // do something
        }

//        Classification cl=new Classification();
//        cl.setClassifier(tModel);
         //System.out.println("Result: "+cl.classify(1255.46750,1.0) );

        String t="2.1  ";
        

//        System.out.println(Classification.train(tDir,tFile));

//        TestClass t23=new TestClass(2,3);
//        TestClass t24=new TestClass(11,3);
//
//        HashMap<String, TestClass> classifiers = new HashMap<String, TestClass>();
//
//        classifiers.put("t23",t23);
//        classifiers.put("t24",t24);
//
//        for (TestClass value : classifiers.values()) {
//            System.out.println("Value = " + value.x);
//        }

//        Set set = classifiers.entrySet();
//        Iterator iterator = set.iterator();
//        while(iterator.hasNext()) {
//            Map.Entry mentry = (Map.Entry)iterator.next();
//            System.out.print("key is: "+ mentry.getKey() + " & Value is: ");
//            System.out.println(mentry.getValue());
//        }

//        String line="Attis/sub/Conji";
//        String items[]=line.split("\\/");
//        String baseDir="C:\\Program Files\\Global Prime\\MQL4\\Indicators";
//
//        if (items.length>1) {
//            for(int i=0; i<items.length; i++){
//                baseDir+="\\"+items[i];
//
//            }
//
//        }
//        System.out.println(baseDir);
//        System.out.println(items[items.length-1]);
//        for (String item : items) {
//            System.out.println(item);
//
//        }


    }
}
