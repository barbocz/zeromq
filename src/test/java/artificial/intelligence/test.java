package artificial.intelligence;

public class test {
    public static void main(String[] args) throws Exception
    {
        String line="Andorra la Vella|ad|Andorra la Vella|20430|42.51|1.51";
        String items[]=line.split("\\|");

        for (String item : items) {
            System.out.println(item);

        }
    }
}
