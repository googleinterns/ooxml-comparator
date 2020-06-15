import org.apache.commons.io.FileUtils;
import org.hsqldb.util.CSVWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiffGenerator {
    String path1;
    String path2;
    String finalPath;
    ArrayList<ArrayList<String>> SummaryData;
    ArrayList<ArrayList<String>> DiffData;

    public DiffGenerator(String pathOrig,String pathRound,String outputPath){
        path1 = pathOrig;
        path2 = pathRound;
        finalPath = outputPath;
    }

    public void writeCSVFile(ArrayList<String[]> data,String[] header,String filePath) {
        File file = new File(filePath);
        try {
            CSVWriter writer = new CSVWriter(file, null);
            writer.writeHeader(header);
            for (String[] entry : data) {
                writer.writeData(entry);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> findFoldersInDirectory(String directoryPath) {
        File directory = new File(directoryPath);

        FileFilter directoryFileFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };

        File[] directoryListAsFile = directory.listFiles(directoryFileFilter);
        assert directoryListAsFile != null;
        List<String> foldersInDirectory = new ArrayList<String>(directoryListAsFile.length);
        for (File directoryAsFile : directoryListAsFile) {
            foldersInDirectory.add(directoryAsFile.getName());
        }

        return foldersInDirectory;
    }

    public int PrepareFolder() throws IOException {
        Path finalFolder = Paths.get(finalPath);
        if(Files.notExists(finalFolder)){
            System.out.println("NOT A VALID FOLDER PATH");
            return 0;
        }

        FileUtils.cleanDirectory(new File(finalPath));

        File file = new File(finalPath+ "/IndividualDiff" );
        if(!file.mkdir()){
            System.out.println("Failed to create the Directory");
        }

        List<String> foldersOrig = findFoldersInDirectory(path1);
        List<String> foldersRound = findFoldersInDirectory(path2);

        if(foldersOrig.size()!=foldersRound.size()){
            return 0;
        }
        Collections.sort(foldersOrig);
        Collections.sort(foldersRound);

        ArrayList<String[]> allDiffs = new ArrayList<String[]>();
        ArrayList<String[]> summaryData = new ArrayList<String[]>();
        for(int i=0;i<foldersOrig.size();i++) {
            if (!foldersOrig.get(i).equals(foldersRound.get(i))) {
                System.out.println("FOLDERS NOT SAME\n");
                return -1;
            }
            Comparator cmp = new Comparator(path1 + "/" + foldersOrig.get(i), path2 + "/" + foldersRound.get(i));
            ArrayList<DiffObject> temp = cmp.CompareText();
            for(DiffObject x:temp){
                allDiffs.add(x.getCsvEntry());
            }
            GenerateIndividualDiffReport(foldersOrig.get(i), temp);
            summaryData.add(new String[]{foldersOrig.get(i), String.valueOf(temp.isEmpty()), String.valueOf(temp.size())});
        }
        GenerateGlobalDiffReport(allDiffs);
        GenerateComparisionSummary(summaryData);
        return 1;
    }

    private void GenerateComparisionSummary(ArrayList<String[]> summaryData) {
        writeCSVFile(summaryData,new String[]{"File Name","Exactly Same","Number of Differences"},finalPath+"/summary.csv");
    }

    private void GenerateGlobalDiffReport(ArrayList<String[]> allDiffs) {
        writeCSVFile(allDiffs,new String[]{"tag_name","content1","content2","details"},finalPath+"/allDiff.csv");
    }

    private void GenerateIndividualDiffReport(String s, ArrayList<DiffObject> temp) {
        ArrayList<String[]> entry = new ArrayList<String[]>();
        for (DiffObject diffObject : temp) {
            entry.add(diffObject.getCsvEntry());
        }
        writeCSVFile(entry,new String[]{"tag_name","content1","content2","details"},finalPath+"/IndividualDiff/"+s+".csv");
    }
}
