import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * DiffGenerator class finds the Difference between files in two folders
 * using the class FileComparator and Generates the Diff report in 3 different cases
 * Individual comparision report, Global run Summary and All diff compilation
 */
public class DiffGenerator {
    String pathOrigFile;
    String pathRoundTripFile;
    String finalOutputPath;

    /**
     * Constructor takes the various path to bes used in preparation phase to compare
     *
     * @param pathOrig   Path for folder containing the generated original file's JSON
     * @param pathRound  Path for folder containing the generated round tripped file's JSON
     * @param outputPath Path to store the
     */
    public DiffGenerator(String pathOrig, String pathRound, String outputPath) {
        pathOrigFile = pathOrig;
        pathRoundTripFile = pathRound;
        finalOutputPath = outputPath;
    }

    /**
     * Fucntion to write the general XLSX diff report for individual and All diff report
     *
     * @param data     The row data for the table formed
     * @param header   The Headers for the table
     * @param filePath Path to the XLSX file to be stored
     */
    private void writeCSVFile(ArrayList<String[]> data, String[] header, String filePath) {
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
            for (int i = 0; i < entry.length; i++) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function returning the names of all JSON folders present in the folder
     *
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
    public void prepareFolderAndRun() {

        Path finalFolder = Paths.get(finalOutputPath);
        if (Files.notExists(finalFolder)) {
            // the output path does not exist
            StatusLogger.addRecordWarningExec("NOT A VALID FOLDER PATH : " + finalOutputPath);
            return;
        }

        // clear the output path to have no prior data
        try {
            FileUtils.cleanDirectory(new File(finalOutputPath));
        } catch (IOException ioException) {
            StatusLogger.addRecordWarningExec(ioException.getMessage());
        }

        // Create the individual Diff folder.
        File indivialDiffFolder = new File(finalOutputPath + "/IndividualDiff");
        if (!indivialDiffFolder.mkdir()) {
            StatusLogger.addRecordWarningExec("Failed to create the Directory : " + finalOutputPath);
        }

        List<String> foldersOrig = findFoldersInDirectory(pathOrigFile);
        List<String> foldersRound = findFoldersInDirectory(pathRoundTripFile);

        TreeSet<String> folderPresentRound = new TreeSet<>(foldersRound);

        ArrayList<String[]> allDiffs = new ArrayList<>();
        ArrayList<String[]> summaryData = new ArrayList<>();

        // Will be used in runDataGeneration
        RunReportData generalReport = new RunReportData("Global");
        RunReportData docxReport = new RunReportData("Docx");
        RunReportData pptxReport = new RunReportData("Pptx");
        RunReportData xlsxReport = new RunReportData("Xlsx");

        for (String origFileName : foldersOrig) {
            if (!folderPresentRound.contains(origFileName)) {
                StatusLogger.addRecordWarningExec("File Not Present in Round Tripped Folder : " + origFileName);
                continue;
            }

            StatusLogger.addRecordInfoExec("Comparing the file : " + origFileName);
            StatusLogger.addRecordInfoDebug("Before Going into Comparator");

            // add time logger for this file comparision.
            long startTime = System.nanoTime();
            FileComparator fileComparator = new FileComparator(pathOrigFile + "/" + origFileName, pathRoundTripFile + "/" + origFileName);
            ArrayList<DiffObject> temp = fileComparator.CompareText();
            long elapsedTime = System.nanoTime() - startTime;

            if (!fileComparator.fileExtension.equals("invalid")) {

                // Report data collection starts
                RunReportData fileTypeReport = null;
                switch (fileComparator.fileExtension) {
                    case "docx":
                        fileTypeReport = docxReport;
                        break;
                    case "pptx":
                        fileTypeReport = pptxReport;
                        break;
                    case "xlsx":
                        fileTypeReport = xlsxReport;
                        break;
                }

                generalReport.totalFilesMatched++;
                assert fileTypeReport != null;
                fileTypeReport.totalFilesMatched++;
                if (temp.isEmpty()) {
                    generalReport.numberOfFileNoDiff++;
                    fileTypeReport.numberOfFileNoDiff++;
                }
                generalReport.totalDiff += temp.size();
                fileTypeReport.totalDiff += temp.size();
                int commentDiff = 0;
                int textDiff = 0;
                for (DiffObject diffObject : temp) {
                    generalReport.addTag(diffObject.tag);
                    generalReport.addType(diffObject.type);
                    fileTypeReport.addTag(diffObject.tag);
                    fileTypeReport.addType(diffObject.type);
                    if (diffObject.type.equals("0")) {
                        textDiff++;
                    } else {
                        commentDiff++;
                    }
                }
                if (textDiff > 0) {
                    if (commentDiff == 0) {
                        generalReport.filesWithOnlyTextDiff++;
                        fileTypeReport.filesWithOnlyTextDiff++;
                    }
                    fileTypeReport.filesContainingTextDiffs++;
                    generalReport.filesContainingTextDiffs++;
                }
                if (commentDiff > 0) {
                    if (textDiff == 0) {
                        generalReport.filesWithOnlyCommentDiff++;
                        fileTypeReport.filesWithOnlyCommentDiff++;
                    }
                    generalReport.filesContainingCommentDiffs++;
                    fileTypeReport.filesContainingCommentDiffs++;
                }
                generalReport.addTime((int) (elapsedTime / 1000000));
                fileTypeReport.addTime((int) (elapsedTime / 1000000));
                // Repord Data collection ends


                StatusLogger.addRecordInfoDebug("Returned from Comparator");
                // Add the name of the folder in the front for all the diffs found
                for (DiffObject diffObject : temp) {

                    String[] diffObjectCsvEntry = diffObject.getCsvEntry();
                    String[] fileNameCsvEntry = new String[diffObjectCsvEntry.length + 1];
                    fileNameCsvEntry[0] = origFileName;
                    System.arraycopy(diffObjectCsvEntry, 0, fileNameCsvEntry, 1, diffObjectCsvEntry.length);
                    allDiffs.add(fileNameCsvEntry);
                }

                generateIndividualDiffReport(origFileName, temp);
                summaryData.add(new String[]{origFileName, String.valueOf(temp.isEmpty()), String.valueOf(temp.size()), String.valueOf(elapsedTime / 1000000)});
            }
        }
        generateComparisionSummary(summaryData);
        generateGlobalDiffReport(allDiffs);

        ArrayList<RunReportData> allPages = new ArrayList<>();
        allPages.add(generalReport);
        allPages.add(docxReport);
        allPages.add(pptxReport);
        allPages.add(xlsxReport);
        createRunSummary(allPages);
    }

    private void createXLSXHeading(Workbook workbook, Sheet sheet, String cellContent){
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 20);
        titleFont.setColor(IndexedColors.BLUE.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

        Row headerRow = sheet.getRow(0);
        Cell cell = headerRow.createCell(0);
        cell.setCellValue(cellContent);
        headerCellStyle.setFont(titleFont);
        cell.setCellStyle(headerCellStyle);
    }

    private void createXLSXSection(Workbook workbook,Sheet sheet,int rowNum,int colNum,String cellContent){
        Font sectionFont = workbook.createFont();
        sectionFont.setBold(true);
        sectionFont.setFontHeightInPoints((short) 16);
        sectionFont.setColor(IndexedColors.RED.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

        Row headerRow = sheet.getRow(rowNum);
        Cell cell = headerRow.createCell(colNum);
        cell.setCellValue(cellContent);
        headerCellStyle.setFont(sectionFont);
        cell.setCellStyle(headerCellStyle);
    }

    private void createXLSXSubSection(Workbook workbook, Sheet sheet, int rowNum, String cellContent) {
        Font subSectionFont = workbook.createFont();
        subSectionFont.setBold(true);
        subSectionFont.setFontHeightInPoints((short) 12);
        subSectionFont.setColor(IndexedColors.GREEN.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();

        Row headerRow = sheet.getRow(rowNum);
        Cell cell = headerRow.createCell(0);
        cell.setCellValue(cellContent);
        headerCellStyle.setFont(subSectionFont);
        cell.setCellStyle(headerCellStyle);
    }

    private void createXLSXDataSection(Workbook workbook,Sheet sheet,int rowNum,int colNum,String cellContent) {
        Font subSectionFont = workbook.createFont();
        subSectionFont.setBold(true);
        subSectionFont.setFontHeightInPoints((short) 12);
        subSectionFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();

        Row row = sheet.getRow(rowNum);
        Cell cell = row.createCell(colNum);
        cell.setCellValue(cellContent);
        headerCellStyle.setFont(subSectionFont);
        cell.setCellStyle(headerCellStyle);
    }

    private void createRunSummary(ArrayList<RunReportData> pages) {
        Workbook workbook = new XSSFWorkbook();
        for (RunReportData curData : pages) {
            Sheet sheet = workbook.createSheet(curData.fileType);

            for(int row=0;row<25;row++){ // 25 row summary data present
                sheet.createRow(row);
            }

            createXLSXHeading(workbook,sheet, "File type : " +curData.fileType);

            createXLSXSection(workbook,sheet,2,0,"File Metrics");

            createXLSXSubSection(workbook,sheet,3, "Total number of files compared");
            createXLSXDataSection(workbook,sheet,3,1, String.valueOf(curData.totalFilesMatched));

            createXLSXSubSection(workbook,sheet,4, "Number of files with no diffs");
            createXLSXDataSection(workbook,sheet,4,1, String.valueOf(curData.numberOfFileNoDiff));

            createXLSXSubSection(workbook,sheet,5, "% of files with no diffs");
            createXLSXDataSection(workbook,sheet,5,1, String.valueOf(curData.getPercentageNoDiff()));


            createXLSXSection(workbook,sheet,7,0,"Diff Metrics");

            createXLSXSubSection(workbook,sheet,8, "Total number of diffs across all the files");
            createXLSXDataSection(workbook,sheet,8,1, String.valueOf(curData.totalDiff));

            createXLSXSubSection(workbook,sheet,9, "Most common tag which is causing a difference among all files");
            createXLSXDataSection(workbook,sheet,9,1, curData.getMostFreqTagCausingDiff());

            createXLSXSubSection(workbook,sheet,10, "Total number of text diffs across all the files");
            createXLSXDataSection(workbook,sheet,10,1, String.valueOf(curData.typeCausingDiff.get("0")));

            createXLSXSubSection(workbook,sheet,11, "Total number of comment diffs across all the files");
            createXLSXDataSection(workbook,sheet,11,1, String.valueOf(curData.typeCausingDiff.get("1")));


            createXLSXSection(workbook,sheet,13,0,"Diff + file metrics");

            createXLSXSubSection(workbook,sheet,14, "Total number of files with atleast one text diffs");
            createXLSXDataSection(workbook,sheet,14,1, String.valueOf(curData.filesContainingTextDiffs));

            createXLSXSubSection(workbook,sheet,15, "Total number of files with atleast one comment diffs");
            createXLSXDataSection(workbook,sheet,15,1, String.valueOf(curData.filesContainingCommentDiffs));

            createXLSXSubSection(workbook,sheet,16, "Total number of files with only text diffs");
            createXLSXDataSection(workbook,sheet,16,1, String.valueOf(curData.filesWithOnlyTextDiff));

            createXLSXSubSection(workbook,sheet,17, "Total number of files with only comment diffs");
            createXLSXDataSection(workbook,sheet,17,1, String.valueOf(curData.filesWithOnlyCommentDiff));


            createXLSXSection(workbook,sheet,19,0,"Latency metrics");
            createXLSXSection(workbook,sheet,19,1,"in ms");
            createXLSXSection(workbook,sheet,19,2,"in sec");
            createXLSXSection(workbook,sheet,19,3,"in mins");

            createXLSXSubSection(workbook,sheet,20, "Total time to run for all files");
            double val = curData.totalTimeTaken();
            createXLSXDataSection(workbook,sheet,20,1, String.valueOf(val));
            createXLSXDataSection(workbook,sheet,20,2, String.valueOf(val/1000));
            createXLSXDataSection(workbook,sheet,20,3, String.valueOf(val/60000));

            createXLSXSubSection(workbook,sheet,21, "Average time to run per files");
            val = curData.avgTimeTaken();
            createXLSXDataSection(workbook,sheet,21,1, String.valueOf(val));
            createXLSXDataSection(workbook,sheet,21,2, String.valueOf(val/1000));
            createXLSXDataSection(workbook,sheet,21,3, String.valueOf(val/60000));

            createXLSXSubSection(workbook,sheet,22, "Maximum time taken compared to all files");
            val = curData.maxTimeTaken();
            createXLSXDataSection(workbook,sheet,22,1, String.valueOf(val));
            createXLSXDataSection(workbook,sheet,22,2, String.valueOf(val/1000));
            createXLSXDataSection(workbook,sheet,22,3, String.valueOf(val/60000));

            createXLSXSubSection(workbook,sheet,23, "99th percentile latency");
            val = curData.get99Percentile();
            createXLSXDataSection(workbook,sheet,23,1, String.valueOf(val));
            createXLSXDataSection(workbook,sheet,23,2, String.valueOf(val/1000));
            createXLSXDataSection(workbook,sheet,23,3, String.valueOf(val/60000));

            createXLSXSubSection(workbook,sheet,24, "50th percentile latency");
            val = curData.get50Percentile();
            createXLSXDataSection(workbook,sheet,24,1, String.valueOf(val));
            createXLSXDataSection(workbook,sheet,24,2, String.valueOf(val/1000));
            createXLSXDataSection(workbook,sheet,24,3, String.valueOf(val/60000));

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            if (curData.fileType.equals("Global")) {

                for(int row=26;row<=28;row++){ // create the extra rows needed
                    sheet.createRow(row);
                }

                createXLSXSection(workbook,sheet,26,0,"Path of file generated");

                createXLSXSubSection(workbook,sheet,27, "Summary/AllDiff file Path : ");
                createXLSXDataSection(workbook,sheet,27,1, finalOutputPath + "/summary.xlsx");

                createXLSXSubSection(workbook,sheet,28, "Individual Diff file path : ");
                createXLSXDataSection(workbook,sheet,28,1, finalOutputPath + "/IndividualDiff/");
            }
        }

        // Write the output to a file
        try {
            FileOutputStream fileOut = new FileOutputStream(finalOutputPath + "/RunReport.xlsx");
            workbook.write(fileOut);
            fileOut.close();
        } catch (IOException e) {
            StatusLogger.addRecordWarningExec(e.getMessage());
        }
    }

    private void generateComparisionSummary(ArrayList<String[]> summaryData) {
        StatusLogger.addRecordInfoExec("Creating Summary CSV Data");
        writeCSVFile(summaryData, new String[]{"File Name", "Exactly Same", "Number of Differences", "timeTaken(in ms)"}, finalOutputPath + "/summary.xlsx");
    }

    private void generateGlobalDiffReport(ArrayList<String[]> allDiffs) {
        StatusLogger.addRecordInfoExec("Creating All Diff CSV Data");
        StatusLogger.addRecordInfoExec("Total number of Diffs found : " + allDiffs.size());
        writeCSVFile(allDiffs, new String[]{"File Name", "tag_name","diff_type (0-Text,1-Comment)", "content1", "content2", "details"}, finalOutputPath + "/allDiff.xlsx");
    }

    private void generateIndividualDiffReport(String fileName, ArrayList<DiffObject> fileDiffEntry) {
        StatusLogger.addRecordInfoExec("Creating Individual CSV Data in " + fileName);
        ArrayList<String[]> entry = new ArrayList<>();
        for (DiffObject diffObject : fileDiffEntry) {
            entry.add(diffObject.getCsvEntry());
        }
        writeCSVFile(entry, new String[]{"tag_name", "diff_type (0-Text,1-Comment)", "content1", "content2", "details"}, finalOutputPath + "/IndividualDiff/" + fileName + ".xlsx");
    }

}
