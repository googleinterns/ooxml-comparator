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
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

/**
 * DiffGenerator class finds the Difference between files in two folders
 * using the class FileComparator and Generates the Diff report in 3 different cases
 * Individual comparision report, Global run Summary and All diff compilation
 */
public class DiffGenerator {

    public static final String GENERAL_SHEET_NAME = "Data";
    public static final short TITLE_REPORT_SHEET_FONT_SIZE = 20;
    public static final short SECTION_REPORT_SHEET_FONT_SIZE = 16;
    public static final short SUBSECTION_DATA_REPORT_SHEET_FONT_SIZE = 12;
    public static final short HEADER_GENERAL_SHEET_FONT_SIZE = 14;
    public static final String DIFF_FOUND_TAG_TYPE_TEXT = "0";
    public static final String DIFF_FOUND_TAG_TYPE_COMMENT = "1";

    public static final String DOCX_FILE_TYPE = "docx";
    public static final String PPTX_FILE_TYPE = "pptx";
    public static final String XLSX_FILE_TYPE = "xlsx";
    public static final String INVALID_FILE_TYPE = "invalid";

    public static final String GLOBAL_REPORT_SHEET_NAME = "Global";
    public static final String DOCX_REPORT_SHEET_NAME = "Docx";
    public static final String PPTX_REPORT_SHEET_NAME = "Pptx";
    public static final String XLSX_REPORT_SHEET_NAME = "Xlsx";

    String pathOriginalFile;
    String pathRoundTripFile;
    String finalOutputPath;

    /**
     * Constructor takes the various path to bes used in preparation phase to compare
     *
     * @param pathOriginalFile  Path for folder containing the generated original file's JSON
     * @param pathRoundTripFile  Path for folder containing the generated round tripped file's JSON
     * @param finalOutputPath Path to store the
     */
    public DiffGenerator(String pathOriginalFile,String pathRoundTripFile, String finalOutputPath) {
        this.pathOriginalFile = pathOriginalFile;
        this.pathRoundTripFile = pathRoundTripFile;
        this.finalOutputPath = finalOutputPath;
    }

    /**
     * Function to write the general XLSX diff report for individual and All diff report
     *
     * @param rowTableEntry     The row data for the table formed
     * @param headerTable   The Headers for the table
     * @param outputFilePath Path to the XLSX file to be stored
     */
    private void writeCSVFile(ArrayList<String[]> rowTableEntry, String[] headerTable, String outputFilePath) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheetData = workbook.createSheet(GENERAL_SHEET_NAME);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints(HEADER_GENERAL_SHEET_FONT_SIZE);
        headerFont.setColor(IndexedColors.RED.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        // Create a Row.
        Row headerRow = sheetData.createRow(0);

        for (int i = 0; i < headerTable.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headerTable[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Create Other rows and cells with contacts data.
        int rowNum = 1;

        for (String[] entry : rowTableEntry) {
            Row row = sheetData.createRow(rowNum);
            rowNum ++;
            for (int i = 0; i < entry.length; i++) {
                row.createCell(i).setCellValue(entry[i]);
            }
        }

        // Resize all columns to fit the content size
        for (int i = 0; i < headerTable.length; i++) {
            sheetData.autoSizeColumn(i);
        }

        // Write the output to a file.
        try {
            FileOutputStream fileOut = new FileOutputStream(outputFilePath);
            workbook.write(fileOut);
            fileOut.close();
        } catch (IOException e) {
            StatusLogger.addRecordWarningExec(e.getMessage());
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
     * Checks existence for the Folder required to create the output and prepare structure.
     */
    public void prepareFolderRequired(){
        Path finalFolder = Paths.get(finalOutputPath);
        if (Files.notExists(finalFolder)) {
            // The output path does not exist.
            StatusLogger.addRecordWarningExec("NOT A VALID FOLDER PATH : " + finalOutputPath);
            return;
        }

        // Clear the output path to have no prior data.
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
    }

    /**
     * Generates the report data by adding contribution of Diffs for a particular file.
     * @param generalReportData Global space report item that has to be updated.
     * @param fileTypeSpecificReportData The file type specific info that needs to be updated.
     * @param diffObjectsFound List of Diffs found for the particular file.
     * @param elapsedTime Latency metric for the file run.
     */
    private void runReportDataCreation(RunReportData generalReportData, RunReportData fileTypeSpecificReportData, ArrayList<DiffObject> diffObjectsFound, long elapsedTime){
        generalReportData.totalFilesMatched++;
        fileTypeSpecificReportData.totalFilesMatched++;
        if (diffObjectsFound.isEmpty()) {
            generalReportData.numberOfFileNoDiff++;
            fileTypeSpecificReportData.numberOfFileNoDiff++;
        }
        generalReportData.totalDiff += diffObjectsFound.size();
        fileTypeSpecificReportData.totalDiff += diffObjectsFound.size();
        int commentDiffInFile = 0;
        int textDiffInFile = 0;
        for (DiffObject diffObject : diffObjectsFound) {

            generalReportData.addTagCount(diffObject.tagCausingDiff);
            generalReportData.addTypeCount(diffObject.typeOfTag);
            fileTypeSpecificReportData.addTagCount(diffObject.tagCausingDiff);
            fileTypeSpecificReportData.addTypeCount(diffObject.typeOfTag);

            if (diffObject.typeOfTag.equals(DIFF_FOUND_TAG_TYPE_TEXT)) {
                textDiffInFile++;
            } else {
                commentDiffInFile++;
            }
        }
        if (textDiffInFile > 0) {

            if (commentDiffInFile == 0) {
                generalReportData.filesWithOnlyTextDiff++;
                fileTypeSpecificReportData.filesWithOnlyTextDiff++;
            }
            fileTypeSpecificReportData.filesContainingTextDiffs++;
            generalReportData.filesContainingTextDiffs++;
        }
        if (commentDiffInFile > 0) {

            if (textDiffInFile == 0) {
                generalReportData.filesWithOnlyCommentDiff++;
                fileTypeSpecificReportData.filesWithOnlyCommentDiff++;
            }
            generalReportData.filesContainingCommentDiffs++;
            fileTypeSpecificReportData.filesContainingCommentDiffs++;
        }

        // Adding time taken to compute differences in ms precision.
        generalReportData.addTimeForFile((int) (elapsedTime / 1000000));
        fileTypeSpecificReportData.addTimeForFile((int) (elapsedTime / 1000000));
    }

    /**
     * Adds the relevent data to run report and Summary files for every file.
     * @param origFileName File that generates the Diff.
     * @param allDiffs List of Diffs found in all the files till now.
     * @param summaryData The List of Summary Data of all files.
     * @param generalReportData Report Generation data to get the XLSX output for Globals space.
     * @param fileTypeReport  Report Generation data to get the XLSX output for File Specific type.
     * @param diffObjectsFound List of Diffs found in the current file.
     * @param elapsedTime Time taken in comparing the given file.
     */
    private void addNecessaryDataToCreateRunReportAndSummary(String origFileName,
                                                             ArrayList<String[]> allDiffs,
                                                             ArrayList<String[]> summaryData,
                                                             RunReportData generalReportData,
                                                             RunReportData fileTypeReport,
                                                             ArrayList<DiffObject> diffObjectsFound,
                                                             long elapsedTime){
        runReportDataCreation(generalReportData,fileTypeReport,diffObjectsFound,elapsedTime);

        // Add the name of the folder in the front for all the diffs found.
        for (DiffObject diffObject : diffObjectsFound) {
            String[] diffObjectCsvEntry = diffObject.getCsvEntry();
            String[] fileNameCsvEntry = new String[diffObjectCsvEntry.length + 1];
            fileNameCsvEntry[0] = origFileName;
            System.arraycopy(diffObjectCsvEntry, 0, fileNameCsvEntry, 1, diffObjectCsvEntry.length);
            allDiffs.add(fileNameCsvEntry);
        }
        summaryData.add(new String[]{origFileName, String.valueOf(diffObjectsFound.isEmpty()), String.valueOf(diffObjectsFound.size()), String.valueOf(elapsedTime / 1000000)});
    }

    /**
     * This prepares and run the comparator on folder and files.
     */
    public void prepareFolderAndRun() {

        List<String> foldersOrig = findFoldersInDirectory(pathOriginalFile);
        List<String> foldersRound = findFoldersInDirectory(pathRoundTripFile);

        TreeSet<String> folderPresentRound = new TreeSet<>(foldersRound);
        ArrayList<String[]> allDiffs = new ArrayList<>();
        ArrayList<String[]> summaryData = new ArrayList<>();

        // Will be used in runDataGeneration.
        RunReportData generalReportData = new RunReportData(GLOBAL_REPORT_SHEET_NAME);
        RunReportData docxReportData = new RunReportData(DOCX_REPORT_SHEET_NAME);
        RunReportData pptxReportData = new RunReportData(PPTX_REPORT_SHEET_NAME);
        RunReportData xlsxReportData = new RunReportData(XLSX_REPORT_SHEET_NAME);

        for (String origFileName : foldersOrig) {
            if (!folderPresentRound.contains(origFileName)) {
                StatusLogger.addRecordWarningExec("File Not Present in Round Tripped Folder : " + origFileName);
                continue;
            }
            StatusLogger.addRecordInfoExec("Comparing the file : " + origFileName);
            StatusLogger.addRecordInfoDebug("Before Going into Comparator");

            // add time logger for this file comparision.
            long startTime = System.nanoTime();
            FileComparator fileComparator = new FileComparator(pathOriginalFile + "/" + origFileName, pathRoundTripFile + "/" + origFileName);
            ArrayList<DiffObject> diffObjectsFound = fileComparator.compareText();
            long elapsedTime = System.nanoTime() - startTime;

            if (!fileComparator.fileExtension.equals(INVALID_FILE_TYPE)) {
                // Report data collection starts.
                RunReportData fileTypeReport;
                switch (fileComparator.fileExtension) {
                    case DOCX_FILE_TYPE:
                        fileTypeReport = docxReportData;
                        break;
                    case PPTX_FILE_TYPE:
                        fileTypeReport = pptxReportData;
                        break;
                    case XLSX_FILE_TYPE:
                        fileTypeReport = xlsxReportData;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + fileComparator.fileExtension);
                }
                addNecessaryDataToCreateRunReportAndSummary(origFileName,allDiffs,summaryData,generalReportData,fileTypeReport,diffObjectsFound,elapsedTime);
                generateIndividualDiffReport(origFileName, diffObjectsFound);
            }
        }
        generateComparisionSummary(summaryData);
        generateGlobalDiffReport(allDiffs);

        ArrayList<RunReportData> allPages = new ArrayList<>(Arrays.asList(generalReportData, docxReportData, pptxReportData, xlsxReportData));
        createRunSummary(allPages);
    }

    /**
     * Create the XLSX Heading Cell in the Final generated Report.
     */
    private void createXLSXHeading(Workbook workbook, Sheet sheet, String cellContent){
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints(TITLE_REPORT_SHEET_FONT_SIZE);
        titleFont.setColor(IndexedColors.BLUE.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

        Row headerRow = sheet.getRow(0);
        Cell cell = headerRow.createCell(0);
        cell.setCellValue(cellContent);
        headerCellStyle.setFont(titleFont);
        cell.setCellStyle(headerCellStyle);
    }

    /**
     * Create the XLSX Section Cells in the Final generated Report.
     */
    private void createXLSXSection(Workbook workbook,Sheet sheet,int rowNum,int colNum,String cellContent){
        Font sectionFont = workbook.createFont();
        sectionFont.setBold(true);
        sectionFont.setFontHeightInPoints(SECTION_REPORT_SHEET_FONT_SIZE);
        sectionFont.setColor(IndexedColors.RED.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

        Row headerRow = sheet.getRow(rowNum);
        Cell cell = headerRow.createCell(colNum);
        cell.setCellValue(cellContent);
        headerCellStyle.setFont(sectionFont);
        cell.setCellStyle(headerCellStyle);
    }

    /**
     * Create the XLSX SubSection Cells in the Final generated Report.
     */
    private void createXLSXSubSection(Workbook workbook, Sheet sheet, int rowNum, String cellContent) {
        Font subSectionFont = workbook.createFont();
        subSectionFont.setBold(true);
        subSectionFont.setFontHeightInPoints(SUBSECTION_DATA_REPORT_SHEET_FONT_SIZE);
        subSectionFont.setColor(IndexedColors.GREEN.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();

        Row headerRow = sheet.getRow(rowNum);
        Cell cell = headerRow.createCell(0);
        cell.setCellValue(cellContent);
        headerCellStyle.setFont(subSectionFont);
        cell.setCellStyle(headerCellStyle);
    }

    /**
     * Create the XLSX Data Section Cells in the Final generated Report.
     */
    private void createXLSXDataSection(Workbook workbook,Sheet sheet,int rowNum,int colNum,String cellContent) {
        Font subSectionFont = workbook.createFont();
        subSectionFont.setBold(true);
        subSectionFont.setFontHeightInPoints(SUBSECTION_DATA_REPORT_SHEET_FONT_SIZE);
        subSectionFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();

        Row row = sheet.getRow(rowNum);
        Cell cell = row.createCell(colNum);
        cell.setCellValue(cellContent);
        headerCellStyle.setFont(subSectionFont);
        cell.setCellStyle(headerCellStyle);
    }

    /**
     * Create the File Metric Section Cells in the Final generated Report.
     */
    private void createFileMetricSection(Workbook workbook, Sheet sheet, RunReportData currentData){
        createXLSXSection(workbook,sheet,2,0,"File Metrics");
        createXLSXSubSection(workbook,sheet,3, "Total number of files compared");
        createXLSXDataSection(workbook,sheet,3,1, String.valueOf(currentData.totalFilesMatched));
        createXLSXSubSection(workbook,sheet,4, "Number of files with no diffs");
        createXLSXDataSection(workbook,sheet,4,1, String.valueOf(currentData.numberOfFileNoDiff));
        createXLSXSubSection(workbook,sheet,5, "% of files with no diffs");
        createXLSXDataSection(workbook,sheet,5,1, String.valueOf(currentData.getPercentageNoDiff()));
    }

    /**
     * Create the Diffs related Metric Section Cells in the Final generated Report.
     */
    private void createDiffMetricSection(Workbook workbook,Sheet sheet, RunReportData currentData){
        createXLSXSection(workbook,sheet,7,0,"Diff Metrics");
        createXLSXSubSection(workbook,sheet,8, "Total number of diffs across all the files");
        createXLSXDataSection(workbook,sheet,8,1, String.valueOf(currentData.totalDiff));
        createXLSXSubSection(workbook,sheet,9, "Most common tag which is causing a difference among all files");
        createXLSXDataSection(workbook,sheet,9,1, currentData.getMostFreqTagCausingDiff());
        createXLSXSubSection(workbook,sheet,10, "Total number of text diffs across all the files");
        createXLSXDataSection(workbook,sheet,10,1, String.valueOf(currentData.typeCausingDiff.get("0")));
        createXLSXSubSection(workbook,sheet,11, "Total number of comment diffs across all the files");
        createXLSXDataSection(workbook,sheet,11,1, String.valueOf(currentData.typeCausingDiff.get("1")));
    }

    /**
     * Create the File Metric combined with Diffs Section Cells in the Final generated Report.
     */
    private void createDiffPlusFileMetricSection(Workbook workbook,Sheet sheet, RunReportData currentData){
        createXLSXSection(workbook,sheet,13,0,"Diff + file metrics");
        createXLSXSubSection(workbook,sheet,14, "Total number of files with atleast one text diffs");
        createXLSXDataSection(workbook,sheet,14,1, String.valueOf(currentData.filesContainingTextDiffs));
        createXLSXSubSection(workbook,sheet,15, "Total number of files with atleast one comment diffs");
        createXLSXDataSection(workbook,sheet,15,1, String.valueOf(currentData.filesContainingCommentDiffs));
        createXLSXSubSection(workbook,sheet,16, "Total number of files with only text diffs");
        createXLSXDataSection(workbook,sheet,16,1, String.valueOf(currentData.filesWithOnlyTextDiff));
        createXLSXSubSection(workbook,sheet,17, "Total number of files with only comment diffs");
        createXLSXDataSection(workbook,sheet,17,1, String.valueOf(currentData.filesWithOnlyCommentDiff));
    }

    /**
     * Create the Latency Metrics Section Cells in the Final generated Report.
     */
    private void createLatencyMetricSection(Workbook workbook,Sheet sheet, RunReportData currentData){
        createXLSXSection(workbook,sheet,19,0,"Latency metrics");
        createXLSXSection(workbook,sheet,19,1,"in ms");
        createXLSXSection(workbook,sheet,19,2,"in sec");
        createXLSXSection(workbook,sheet,19,3,"in mins");

        createXLSXSubSection(workbook,sheet,20, "Total time to run for all files");
        double val = currentData.totalTimeTaken();
        createXLSXDataSection(workbook,sheet,20,1, String.valueOf(val));
        createXLSXDataSection(workbook,sheet,20,2, String.valueOf(val/1000));
        createXLSXDataSection(workbook,sheet,20,3, String.valueOf(val/60000));

        createXLSXSubSection(workbook,sheet,21, "Average time to run per files");
        val = currentData.averageTimeTaken();
        createXLSXDataSection(workbook,sheet,21,1, String.valueOf(val));
        createXLSXDataSection(workbook,sheet,21,2, String.valueOf(val/1000));
        createXLSXDataSection(workbook,sheet,21,3, String.valueOf(val/60000));

        createXLSXSubSection(workbook,sheet,22, "Maximum time taken compared to all files");
        val = currentData.maximumTimeTaken();
        createXLSXDataSection(workbook,sheet,22,1, String.valueOf(val));
        createXLSXDataSection(workbook,sheet,22,2, String.valueOf(val/1000));
        createXLSXDataSection(workbook,sheet,22,3, String.valueOf(val/60000));

        createXLSXSubSection(workbook,sheet,23, "99th percentile latency");
        val = currentData.percentileLatency(currentData.timeTakenPerFile,99.0);
        createXLSXDataSection(workbook,sheet,23,1, String.valueOf(val));
        createXLSXDataSection(workbook,sheet,23,2, String.valueOf(val/1000));
        createXLSXDataSection(workbook,sheet,23,3, String.valueOf(val/60000));

        createXLSXSubSection(workbook,sheet,24, "50th percentile latency");
        val = currentData.percentileLatency(currentData.timeTakenPerFile,50.0);
        createXLSXDataSection(workbook,sheet,24,1, String.valueOf(val));
        createXLSXDataSection(workbook,sheet,24,2, String.valueOf(val/1000));
        createXLSXDataSection(workbook,sheet,24,3, String.valueOf(val/60000));
    }

    /**
     * Create the Global path of Output Section Cells in the Final generated Report.
     */
    private void createPathGlobalsSection(Workbook workbook,Sheet sheet){
        createXLSXSection(workbook,sheet,26,0,"Path of file generated");
        createXLSXSubSection(workbook,sheet,27, "Summary/AllDiff file Path : ");
        createXLSXDataSection(workbook,sheet,27,1, finalOutputPath + "/summary.xlsx");
        createXLSXSubSection(workbook,sheet,28, "Individual Diff file path : ");
        createXLSXDataSection(workbook,sheet,28,1, finalOutputPath + "/IndividualDiff/");
    }

    /**
     * Creates the Run report XLSX for the Whole execution
     * @param pages The various RunReport objects summarising the data found for all 4 scopes.
     */
    private void createRunSummary(ArrayList<RunReportData> pages) {
        Workbook workbook = new XSSFWorkbook();
        for (RunReportData currentData : pages) {
            Sheet sheet = workbook.createSheet(currentData.fileTypeName);

            for(int row=0;row<25;row++){ // In the Sheet containing report, 25 row summary data present.
                sheet.createRow(row);
            }

            createXLSXHeading(workbook,sheet, "File type : " +currentData.fileTypeName);
            createFileMetricSection(workbook,sheet,currentData);
            createDiffMetricSection(workbook,sheet,currentData);
            createDiffPlusFileMetricSection(workbook,sheet,currentData);
            createLatencyMetricSection(workbook,sheet,currentData);

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            if (currentData.fileTypeName.equals(GLOBAL_REPORT_SHEET_NAME)) {
                for(int row=26;row<=28;row++){ // Create the extra rows needed.
                    sheet.createRow(row);
                }
                createPathGlobalsSection(workbook,sheet);
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

    /**
     * Genrates the Summary of all the files.
     * @param summaryData The collected about various metrics while comparing files.
     */
    private void generateComparisionSummary(ArrayList<String[]> summaryData) {
        StatusLogger.addRecordInfoExec("Creating Summary CSV Data");
        writeCSVFile(summaryData, new String[]{"File Name", "Exactly Same", "Number of Differences", "timeTaken(in ms)"}, finalOutputPath + "/summary.xlsx");
    }

    /**
     * Generates XLSX containing all the Diffs found in the comparision across all files.
     * @param allDiffs List of all the diffs found.
     */
    private void generateGlobalDiffReport(ArrayList<String[]> allDiffs) {
        StatusLogger.addRecordInfoExec("Creating All Diff CSV Data");
        StatusLogger.addRecordInfoExec("Total number of Diffs found : " + allDiffs.size());
        writeCSVFile(allDiffs, new String[]{"File Name", "tag_name","diff_type (0-Text,1-Comment)", "content1", "content2", "details"}, finalOutputPath + "/allDiff.xlsx");
    }

    /**
     * Generates the XLSX containing the Individual Diffs for each of the files.
     * @param fileName Name of the file for which the Diffs are being reported.
     * @param fileDiffEntry The list of Diffs that are found in the file.
     */
    private void generateIndividualDiffReport(String fileName, ArrayList<DiffObject> fileDiffEntry) {
        StatusLogger.addRecordInfoExec("Creating Individual CSV Data in " + fileName);
        ArrayList<String[]> entry = new ArrayList<>();
        for (DiffObject diffObject : fileDiffEntry) {
            entry.add(diffObject.getCsvEntry());
        }
        writeCSVFile(entry, new String[]{"tag_name", "diff_type (0-Text,1-Comment)", "content1", "content2", "details"}, finalOutputPath + "/IndividualDiff/" + fileName + ".xlsx");
    }

}
