import java.io.*;
import java.util.ArrayList;

/**
 * Main Execution of the Comparator starts at this Class.
 */
public class RunComparator{
    public static final String DEFAULT_CONFIG_FILE_PATH = "./target/comparator.config";

    /**
     * Runs the Comparator on the Given paths
     * @param pathOriginalFile Path to the folder of original files convered by V0 (generated folder)
     * @param pathRoundtrippedFile Path to the folder of Roundtripped files convered by V0 (generated folder)
     * @param outputPath Path to generate the Reports and Diffs at
     */
    public static void runComparator(String pathOriginalFile, String pathRoundtrippedFile, String outputPath) {
        DiffGenerator diffGenerator = new DiffGenerator(pathOriginalFile, pathRoundtrippedFile, outputPath);
        try {
            diffGenerator.prepareFolderRequired();
            diffGenerator.prepareFolderAndRun();
        } catch (Exception e) {
            StatusLogger.addRecordWarningExec(e.getMessage());
        }
    }

    /**
     * The Main execution of the comparator starts here.
     * @param args Pass in the path of
     */
    public static void main(String[] args) {
        try{
            File fileConfigPath;
            if(args.length==0) {
                fileConfigPath = new File(DEFAULT_CONFIG_FILE_PATH);
            }else{
                fileConfigPath = new File(args[0]);
            }
            FileReader fileReader=new FileReader(fileConfigPath);
            BufferedReader bufferedReader=new BufferedReader(fileReader);
            ArrayList<String> Contents = new ArrayList<>();
            String lineData;
            while((lineData=bufferedReader.readLine())!=null){
                Contents.add(lineData);
            }
            fileReader.close();
            runComparator(Contents.get(0),Contents.get(1),Contents.get(2));
        }catch(IOException e) {
            StatusLogger.addRecordWarningExec(e.getMessage());
        }
    }
}
