package artificial.intelligence;

import java.io.File;
import java.io.FilenameFilter;

public class MT4FileAccess {

     public static String getIndicatorName (String directory,String prefix) {

         //String indicatorDir="C:\\Program Files\\Global Prime\\MQL4\\Indicators";

         String directoryPrefix="";
         String items[]=prefix.split("\\/");

         if (items.length>1) {
             for(int i=0; i<items.length-1; i++){
                 directory+="\\"+items[i];
                 directoryPrefix=items[i]+"/";
             }
             prefix=items[items.length-1];
         }



         File dir = new File(directory);
         FilenameFilter select = new FileListFilter(prefix, "ex4");
         File[] files = dir.listFiles(select);

         String dirList="";
         for (File file : files) {
             String fileName=directoryPrefix+file.getName().replace(".ex4","");
             if (dirList.length()==0) dirList+=fileName;
             else dirList+="|"+fileName;
         }
         //System.out.println(dirList);
         return(dirList);
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
