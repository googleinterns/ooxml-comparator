import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * DiffGenerator class finds the Difference between files in two folders
 * using the class FileComparator and Generates the Diff report in 3 different cases
 * Individual comparision report, Global run Summary and All diff compilation
 */
public class DiffGenerator {
    String path1;
    String path2;
    String finalPath;

    /**
     * Constructor takes the various path to bes used in preparation phase to compare
     * @param pathOrig Path for folder containing the generated original file's JSON
     * @param pathRound Path for folder containing the generated round tripped file's JSON
     * @param outputPath Path to store the
     */
    public DiffGenerator(String pathOrig,String pathRound,String outputPath){
        path1 = pathOrig;
        path2 = pathRound;
        finalPath = outputPath;
    }

    /**
     * Fucntion to write the general XLSX diff report for individual and All diff report
     * @param data The row data for the table formed
     * @param header The Headers for the table
     * @param filePath Path to the XLSX file to be stored
     */
    private void writeCSVFile(ArrayList<String[]> data,String[] header,String filePath) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.RED.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        // Create a Row
        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < header.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(header[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Create Other rows and cells with contacts data
        int rowNum = 1;

        for (String[] entry : data) {
            Row row = sheet.createRow(rowNum++);
            for(int i = 0; i < entry.length; i++){
                row.createCell(i).setCellValue(entry[i]);
            }
        }

        // Resize all columns to fit the content size
        for (int i = 0; i < header.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write the output to a file
        try {
            FileOutputStream fileOut = new FileOutputStream(filePath);
            workbook.write(fileOut);
            fileOut.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Function returning the names of all JSON folders present in the folder
     * @param directoryPath path to load the folder list from.
     * @return List of the folders present at the path.
     */
    public List<String> findFoldersInDirectory(String directoryPath) {
        File directory = new File(directoryPath);

        var directoryFileFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };

        File[] directoryListAsFile = directory.listFiles(directoryFileFilter);
        assert directoryListAsFile != null;
        List<String> foldersInDirectory = new ArrayList<>(directoryListAsFile.length);
        for (File directoryAsFile : directoryListAsFile) {
            foldersInDirectory.add(directoryAsFile.getName());
        }

        return foldersInDirectory;
    }

    /**
     * This prepares and run the comparator on folder and files.
     */
    public void prepareFolderAndRun(){

        Path finalFolder = Paths.get(finalPath);
        if(Files.notExists(finalFolder)){
            // the output path does not exist
            StatusLogger.AddRecordWarningExec("NOT A VALID FOLDER PATH : "+finalPath);
            return;
        }

        // clear the output path to have no prior data
        try {
            FileUtils.cleanDirectory(new File(finalPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create the individual Diff folder.
        File file = new File(finalPath+ "/IndividualDiff" );
        if(!file.mkdir()){
            StatusLogger.AddRecordWarningExec("Failed to create the Directory : "+finalPath);
        }

        List<String> foldersOrig = findFoldersInDirectory(path1);
        List<String> foldersRound = findFoldersInDirectory(path2);

        TreeSet<String> folderPresentRound = new TreeSet<>(foldersRound);

        ArrayList<String[]> allDiffs = new ArrayList<>();
        ArrayList<String[]> summaryData = new ArrayList<>();

        for (String s : foldersOrig) {
            if (!folderPresentRound.contains(s)) {
                StatusLogger.AddRecordWarningExec("File Not Present in Round Tripped Folder : " + s);
                continue;
            }

            StatusLogger.AddRecordInfoExec("Comparing the file : " + s);
            StatusLogger.AddRecordInfoDebug("Before Going into Comparator");

            // add time logger for this file comparision.
            FileComparator cmp = new FileComparator(path1 + "/" + s, path2 + "/" + s);
            ArrayList<DiffObject> temp = cmp.CompareText();

            StatusLogger.AddRecordInfoDebug("Returned from Comparator");

            // Add the name of the folder in the front for all the diffs found
            for (DiffObject x : temp) {
                String[] a = x.getCsvEntry();
                String[] a2 = new String[a.length + 1];
                a2[0] = s;
                System.arraycopy(a, 0, a2, 1, a.length);
                allDiffs.add(a2);
            }

            GenerateIndividualDiffReport(s, temp);
            summaryData.add(new String[]{s, String.valueOf(temp.isEmpty()), String.valueOf(temp.size())});
        }
        GenerateComparisionSummary(summaryData);
        GenerateGlobalDiffReport(allDiffs);
    }

    private void GenerateComparisionSummary(ArrayList<String[]> summaryData) {
        StatusLogger.AddRecordInfoExec("Creating Summary CSV Data");
        writeCSVFile(summaryData,new String[]{"File Name","Exactly Same","Number of Differences"},finalPath+"/summary.xlsx");
    }

    private void GenerateGlobalDiffReport(ArrayList<String[]> allDiffs) {
        StatusLogger.AddRecordInfoExec("Creating All Diff CSV Data");
        StatusLogger.AddRecordInfoExec("Total number of Diffs found : "+String.valueOf(allDiffs.size()));
        writeCSVFile(allDiffs,new String[]{"File Name","tag_name","content1","content2","details"},finalPath+"/allDiff.xlsx");
    }

    private void GenerateIndividualDiffReport(String s, ArrayList<DiffObject> temp) {
        StatusLogger.AddRecordInfoExec("Creating Individual CSV Data in "+s);
        ArrayList<String[]> entry = new ArrayList<>();
        for (DiffObject diffObject : temp) {
            entry.add(diffObject.getCsvEntry());
        }
        writeCSVFile(entry,new String[]{"tag_name","content1","content2","details"},finalPath+"/IndividualDiff/"+s+".xlsx");
    }

}
