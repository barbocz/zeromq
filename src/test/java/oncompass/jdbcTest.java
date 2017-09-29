package oncompass;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.Arrays;

public class jdbcTest {
    public static void main(String[] args) throws Exception {

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
        int mutationCounter=0;
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
            tumorSets[resultSet.getInt("id")]=tumorSetName;
            tumorsetFile.print(tumorSetName + separator);
        }
        tumorsetFile.println("}");
        tumorsetFile.println("");
        tumorsetFile.println("@data");

        String Query="SELECT TMP_MUTATION.id as mutation_id,TMP_TUMORSET.id as tumorset_id FROM MOLECULARPROFILE,MOLECULARPROFILE_MUTATION,MUTATION_NAME,PATIENT_TUMOR,TMP_MUTATION,TMP_TUMORSET where "+
                "MOLECULARPROFILE.id=MOLECULARPROFILE_MUTATION.molecularprofile_id and MUTATION_NAME.id=MOLECULARPROFILE_MUTATION.mutation_name_id and "+
                "MOLECULARPROFILE.tumor_id=PATIENT_TUMOR.id and TMP_MUTATION.mutation_id=MUTATION_NAME.mutation_id and TMP_TUMORSET.tumor_id=PATIENT_TUMOR.tumor_type_id and "+
                "TMP_TUMORSET.histology_id=PATIENT_TUMOR.histology_type_id and molecularprofile_id=";

        String[] features = new String[mutationCounter+1];


        int tumorset_id=0;
        resultSet = statement.executeQuery("SELECT molecularprofile_id FROM MOLECULARPROFILE_MUTATION");
        while (resultSet.next()) {
            ResultSet resultSetMolProf = statement1.executeQuery(Query+resultSet.getInt("molecularprofile_id"));
            Arrays.fill(features,"n");
            //System.out.println(resultSet.getInt("molecularprofile_id")+" ---------------------------------");
            while (resultSetMolProf.next()) {
                //System.out.println(resultSet.getInt("molecularprofile_id")+":   " + resultSetMolProf.getInt("mutation_id") + " - " + resultSetMolProf.getInt("tumorset_id"));
                features[resultSetMolProf.getInt("mutation_id")]="y";
                tumorset_id=resultSetMolProf.getInt("tumorset_id");
            }

            String rw="";
            for (int i = 0; i < mutationCounter; i++) {
                rw+=features[i]+",";
            }
            rw+=tumorSets[tumorset_id];
            tumorsetFile.println( rw);
        }


        tumorsetFile.close();

        conn.close();
    }
}