import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.*;
import org.hsqldb.util.CSVWriter;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
            StatusLogger.AddRecordWARNING("NOT A VALID FOLDER PATH : "+finalPath);
            return 0;
        }

        FileUtils.cleanDirectory(new File(finalPath));

        File file = new File(finalPath+ "/IndividualDiff" );
        if(!file.mkdir()){
            StatusLogger.AddRecordWARNING("Failed to create the Directory : "+finalPath);
        }

        List<String> foldersOrig = findFoldersInDirectory(path1);
        List<String> foldersRound = findFoldersInDirectory(path2);

        if(foldersOrig.size()!=foldersRound.size()){
            //TODO: HANDLE THISSSS!!!
            return 0;
        }

        Collections.sort(foldersOrig);
        Collections.sort(foldersRound);

        ArrayList<String[]> allDiffs = new ArrayList<String[]>();
        ArrayList<String[]> summaryData = new ArrayList<String[]>();
        for(int i=0;i<foldersOrig.size();i++) {
            if (!foldersOrig.get(i).equals(foldersRound.get(i))) {
                StatusLogger.AddRecordWARNING("FOLDERS NOT SAME\n " + foldersOrig.toString() + " : "+foldersRound.toString());
                return -1;
            }
            StatusLogger.AddRecordINFO("Comparing the file : "+foldersOrig);
            Comparator cmp = new Comparator(path1 + "/" + foldersOrig.get(i), path2 + "/" + foldersRound.get(i));
            System.out.println(foldersOrig.get(i));
            ArrayList<DiffObject> temp = cmp.CompareText();

            for(DiffObject x:temp){
                String[] a = x.getCsvEntry();
                String[] a2 = new String[a.length + 1];
                a2[0] = foldersOrig.get(i);
                System.arraycopy(a, 0, a2, 1, a.length);
                allDiffs.add(a2);
            }
            GenerateIndividualDiffReport(foldersOrig.get(i), temp);
            summaryData.add(new String[]{foldersOrig.get(i), String.valueOf(temp.isEmpty()), String.valueOf(temp.size())});
        }
        GenerateGlobalDiffReport(allDiffs);
        GenerateComparisionSummary(summaryData);
        return 1;
    }

    private void GenerateComparisionSummary(ArrayList<String[]> summaryData) {
        StatusLogger.AddRecordINFO("Creating Summary CSV Data");
        writeCSVFile(summaryData,new String[]{"File Name","Exactly Same","Number of Differences"},finalPath+"/summary.xlsx");
    }

    private void GenerateGlobalDiffReport(ArrayList<String[]> allDiffs) {
        StatusLogger.AddRecordINFO("Creating All Diff CSV Data");
        writeCSVFile(allDiffs,new String[]{"File Name","tag_name","content1","content2","details"},finalPath+"/allDiff.xlsx");
    }

    private void GenerateIndividualDiffReport(String s, ArrayList<DiffObject> temp) {
        StatusLogger.AddRecordINFO("Creating Individual CSV Data in "+s);
        ArrayList<String[]> entry = new ArrayList<String[]>();
        for (DiffObject diffObject : temp) {
            entry.add(diffObject.getCsvEntry());
        }
        writeCSVFile(entry,new String[]{"tag_name","content1","content2","details"},finalPath+"/IndividualDiff/"+s+".xlsx");
    }

}
