package artificial.intelligence;

public class test {
    public static void main(String[] args) throws Exception
    {
        String line="Attis/sub/Conji";
        String items[]=line.split("\\/");
        String baseDir="C:\\Program Files\\Global Prime\\MQL4\\Indicators";

        if (items.length>1) {
            for(int i=0; i<items.length; i++){
                baseDir+="\\"+items[i];

            }

        }
        System.out.println(baseDir);
        System.out.println(items[items.length-1]);
//        for (String item : items) {
//            System.out.println(item);
//
//        }
    }
}
