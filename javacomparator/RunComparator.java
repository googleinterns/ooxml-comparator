import java.io.*;
import java.util.ArrayList;

public class RunComparator{
    public static void runComparator(String pathOrig, String pathRound, String outputPath) {
        DiffGenerator diff = new DiffGenerator(pathOrig, pathRound, outputPath);
        try {
            diff.prepareFolderAndRun();
        } catch (Exception e) {
            StatusLogger.addRecordWarningExec(e.getMessage());
        }
    }
    public static void main(String[] args) {
        try{
            File file=new File(args[0]);
            FileReader fileReader=new FileReader(file);
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
    //runComparator("/home/vivgupta/OOXMLcomp/generate/original","/home/vivgupta/OOXMLcomp/generate/roundtripped","/home/vivgupta/OOXMLcomp/generate/output");
}
