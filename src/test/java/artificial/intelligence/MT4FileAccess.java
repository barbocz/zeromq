package artificial.intelligence;

import java.io.File;
import java.io.FilenameFilter;

public class MT4FileAccess {

     public static String getIndicatorName (String directory,String prefix) {

         //String indicatorDir="C:\\Program Files\\Global Prime\\MQL4\\Indicators";
         File dir = new File(directory);
         FilenameFilter select = new FileListFilter(prefix, "ex4");
         File[] files = dir.listFiles(select);

         String dirList="";
         for (File file : files) {
             dirList+=","+file.getName().replace(".ex4","");
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
