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
            StatusLogger.AddRecordWarningExec("NOT A VALID FOLDER PATH : " + finalOutputPath);
            return;
        }

        // clear the output path to have no prior data
        try {
            FileUtils.cleanDirectory(new File(finalOutputPath));
        } catch (IOException ioException) {
            StatusLogger.AddRecordWarningExec(ioException.getMessage());
        }

        // Create the individual Diff folder.
        File indivialDiffFolder = new File(finalOutputPath + "/IndividualDiff");
        if (!indivialDiffFolder.mkdir()) {
            StatusLogger.AddRecordWarningExec("Failed to create the Directory : " + finalOutputPath);
        }

        List<String> foldersOrig = findFoldersInDirectory(pathOrigFile);
        List<String> foldersRound = findFoldersInDirectory(pathRoundTripFile);

        TreeSet<String> folderPresentRound = new TreeSet<>(foldersRound);

        ArrayList<String[]> allDiffs = new ArrayList<>();
        ArrayList<String[]> summaryData = new ArrayList<>();

        // Will be used in runDataGeneration
        RunReportData generalRep = new RunReportData("Global");
        RunReportData docxRep = new RunReportData("Docx");
        RunReportData pptxRep = new RunReportData("Pptx");
        RunReportData xlsxRep = new RunReportData("Xlsx");

        for (String origFileName : foldersOrig) {
            if (!folderPresentRound.contains(origFileName)) {
                StatusLogger.AddRecordWarningExec("File Not Present in Round Tripped Folder : " + origFileName);
                continue;
            }

            StatusLogger.AddRecordInfoExec("Comparing the file : " + origFileName);
            StatusLogger.AddRecordInfoDebug("Before Going into Comparator");

            // add time logger for this file comparision.
            long startTime = System.nanoTime();
            FileComparator fileComparator = new FileComparator(pathOrigFile + "/" + origFileName, pathRoundTripFile + "/" + origFileName);
            ArrayList<DiffObject> temp = fileComparator.CompareText();
            long elapsedTime = System.nanoTime() - startTime;

            if (!fileComparator.fileExtension.equals("invalid")) {

                // Report data collection starts
                RunReportData typeRep = null;
                switch (fileComparator.fileExtension) {
                    case "docx":
                        typeRep = docxRep;
                        break;
                    case "pptx":
                        typeRep = pptxRep;
                        break;
                    case "xlsx":
                        typeRep = xlsxRep;
                        break;
                }

                generalRep.totalFilesMatched++;
                typeRep.totalFilesMatched++;
                if (temp.isEmpty()) {
                    generalRep.numberOfFileNoDiff++;
                    typeRep.numberOfFileNoDiff++;
                }
                generalRep.totalDiff += temp.size();
                typeRep.totalDiff += temp.size();
                int CommentDiff = 0;
                int TextDiff = 0;
                for (DiffObject x : temp) {
                    generalRep.addTag(x.tag);
                    generalRep.addType(x.type);
                    typeRep.addTag(x.tag);
                    typeRep.addType(x.type);
                    if (x.type.equals("0")) {
                        TextDiff++;
                    } else {
                        CommentDiff++;
                    }
                }
                if (TextDiff > 0) {
                    if (CommentDiff == 0) {
                        generalRep.filesWithOnlyTextDiff++;
                        typeRep.filesWithOnlyTextDiff++;
                    }
                    typeRep.filesContainingTextDiffs++;
                    generalRep.filesContainingTextDiffs++;
                }
                if (CommentDiff > 0) {
                    if (TextDiff == 0) {
                        generalRep.filesWithOnlyCommentDiff++;
                        typeRep.filesWithOnlyCommentDiff++;
                    }
                    generalRep.filesContainingCommentDiffs++;
                    typeRep.filesContainingCommentDiffs++;
                }
                generalRep.addTime((int) (elapsedTime / 1000000));
                typeRep.addTime((int) (elapsedTime / 1000000));
                // Repord Data collection ends


                StatusLogger.AddRecordInfoDebug("Returned from Comparator");
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
        allPages.add(generalRep);
        allPages.add(docxRep);
        allPages.add(pptxRep);
        allPages.add(xlsxRep);
        CreateRunSummary(allPages);
    }

    private void CreateRunSummary(ArrayList<RunReportData> pages) {
        Workbook workbook = new XSSFWorkbook();
        for (RunReportData curData : pages) {
            Sheet sheet = workbook.createSheet(curData.fileType);

            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 20);
            titleFont.setColor(IndexedColors.BLUE.getIndex());

            Font sectionFont = workbook.createFont();
            sectionFont.setBold(true);
            sectionFont.setFontHeightInPoints((short) 16);
            sectionFont.setColor(IndexedColors.RED.getIndex());

            Font subSectionFont = workbook.createFont();
            subSectionFont.setBold(true);
            subSectionFont.setFontHeightInPoints((short) 14);
            subSectionFont.setColor(IndexedColors.ORANGE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();

            // Write the file type in the cell
            Row headerRow = sheet.createRow(0);
            Cell cell = headerRow.createCell(0);
            cell.setCellValue(curData.fileType);
            headerCellStyle.setFont(titleFont);
            cell.setCellStyle(headerCellStyle);

            headerRow = sheet.createRow(2);
            cell = headerRow.createCell(0);
            cell.setCellValue("File Metrics");
            headerCellStyle.setFont(sectionFont);
            cell.setCellStyle(headerCellStyle);

            headerRow = sheet.createRow(3);
            cell = headerRow.createCell(0);
            cell.setCellValue("Total number of files compared");
            headerCellStyle.setFont(subSectionFont);
            cell.setCellStyle(headerCellStyle);

            cell = headerRow.createCell(1);
            cell.setCellValue(curData.totalFilesMatched);


            headerRow = sheet.createRow(4);
            cell = headerRow.createCell(0);
            cell.setCellValue("Number of files with no diffs");
            headerCellStyle.setFont(subSectionFont);
            cell.setCellStyle(headerCellStyle);

            cell = headerRow.createCell(1);
            cell.setCellValue(curData.numberOfFileNoDiff);


            headerRow = sheet.createRow(5);
            cell = headerRow.createCell(0);
            cell.setCellValue("% of files with no diffs");
            headerCellStyle.setFont(subSectionFont);
            cell.setCellStyle(headerCellStyle);

            cell = headerRow.createCell(1);
            cell.setCellValue(curData.getPercentageNoDiff());

            headerRow = sheet.createRow(7);
            cell = headerRow.createCell(0);
            cell.setCellValue("Diff Metrics");
            headerCellStyle.setFont(sectionFont);
            cell.setCellStyle(headerCellStyle);

            headerRow = sheet.createRow(8);
            cell = headerRow.createCell(0);
            cell.setCellValue("Total number of diffs across all the files");
            headerCellStyle.setFont(subSectionFont);
            cell.setCellStyle(headerCellStyle);

            cell = headerRow.createCell(1);
            cell.setCellValue(curData.totalDiff);


            headerRow = sheet.createRow(9);
            cell = headerRow.createCell(0);
            cell.setCellValue("Most common tag which is causing a difference among all files");
            headerCellStyle.setFont(subSectionFont);
            cell.setCellStyle(headerCellStyle);

            cell = headerRow.createCell(1);
            cell.setCellValue(curData.getMostFreqTagCausingDiff());


            headerRow = sheet.createRow(10);
            cell = headerRow.createCell(0);
            cell.setCellValue("Total number of text diffs across all the files");
            headerCellStyle.setFont(subSectionFont);
            cell.setCellStyle(headerCellStyle);

            cell = headerRow.createCell(1);
            cell.setCellValue(curData.typeCausingDiff.get("0"));// 0 means text


            headerRow = sheet.createRow(11);
            cell = headerRow.createCell(0);
            cell.setCellValue("Total number of comment diffs across all the files");
            headerCellStyle.setFont(subSectionFont);
            cell.setCellStyle(headerCellStyle);

            cell = headerRow.createCell(1);
            cell.setCellValue(curData.typeCausingDiff.get("1"));// 1 means comment


            headerRow = sheet.createRow(13);
            cell = headerRow.createCell(0);
            cell.setCellValue("Diff + file metrics");
            headerCellStyle.setFont(sectionFont);
            cell.setCellStyle(headerCellStyle);

            headerRow = sheet.createRow(14);
            cell = headerRow.createCell(0);
            cell.setCellValue("Total number of files with atleast one text diffs");
            headerCellStyle.setFont(subSectionFont);
            cell.setCellStyle(headerCellStyle);

            cell = headerRow.createCell(1);
            cell.setCellValue(curData.filesContainingTextDiffs);


            headerRow = sheet.createRow(15);
            cell = headerRow.createCell(0);
            cell.setCellValue("Total number of files with atleast one comment diffs");
            headerCellStyle.setFont(subSectionFont);
            cell.setCellStyle(headerCellStyle);

            cell = headerRow.createCell(1);
            cell.setCellValue(curData.filesContainingCommentDiffs);


            headerRow = sheet.createRow(16);
            cell = headerRow.createCell(0);
            cell.setCellValue("Total number of files with only text diffs");
            headerCellStyle.setFont(subSectionFont);
            cell.setCellStyle(headerCellStyle);

            cell = headerRow.createCell(1);
            cell.setCellValue(curData.filesWithOnlyTextDiff);


            headerRow = sheet.createRow(17);
            cell = headerRow.createCell(0);
            cell.setCellValue("Total number of files with only comment diffs");
            headerCellStyle.setFont(subSectionFont);
            cell.setCellStyle(headerCellStyle);

            cell = headerRow.createCell(1);
            cell.setCellValue(curData.filesWithOnlyCommentDiff);


            headerRow = sheet.createRow(19);
            cell = headerRow.createCell(0);
            cell.setCellValue("Latency metrics");
            headerCellStyle.setFont(sectionFont);
            cell.setCellStyle(headerCellStyle);

            cell = headerRow.createCell(1);
            cell.setCellValue("in ms");
            headerCellStyle.setFont(sectionFont);
            cell.setCellStyle(headerCellStyle);

            cell = headerRow.createCell(2);
            cell.setCellValue("in s");
            headerCellStyle.setFont(sectionFont);
            cell.setCellStyle(headerCellStyle);

            cell = headerRow.createCell(3);
            cell.setCellValue("in mins");
            headerCellStyle.setFont(sectionFont);
            cell.setCellStyle(headerCellStyle);


            headerRow = sheet.createRow(20);
            cell = headerRow.createCell(0);
            cell.setCellValue("Total time to run for all files");
            headerCellStyle.setFont(subSectionFont);
            cell.setCellStyle(headerCellStyle);

            float val = curData.totalTimeTaken();
            cell = headerRow.createCell(1);
            cell.setCellValue(val);

            cell = headerRow.createCell(2);
            cell.setCellValue(val / 1000);

            cell = headerRow.createCell(3);
            cell.setCellValue(val / 60000);


            headerRow = sheet.createRow(21);
            cell = headerRow.createCell(0);
            cell.setCellValue("Total time to run for all files");
            headerCellStyle.setFont(subSectionFont);
            cell.setCellStyle(headerCellStyle);

            val = curData.avgTimeTaken();
            cell = headerRow.createCell(1);
            cell.setCellValue(val);

            cell = headerRow.createCell(2);
            cell.setCellValue(val / 1000);

            cell = headerRow.createCell(3);
            cell.setCellValue(val / 60000);


            headerRow = sheet.createRow(22);
            cell = headerRow.createCell(0);
            cell.setCellValue("Maximum time taken compared to all files");
            headerCellStyle.setFont(subSectionFont);
            cell.setCellStyle(headerCellStyle);

            val = curData.maxTimeTaken();
            cell = headerRow.createCell(1);
            cell.setCellValue(val);

            cell = headerRow.createCell(2);
            cell.setCellValue(val / 1000);

            cell = headerRow.createCell(3);
            cell.setCellValue(val / 60000);


            headerRow = sheet.createRow(23);
            cell = headerRow.createCell(0);
            cell.setCellValue("99th percentile latency");
            headerCellStyle.setFont(subSectionFont);
            cell.setCellStyle(headerCellStyle);

            val = curData.get99Percentile();
            cell = headerRow.createCell(1);
            cell.setCellValue(val);

            cell = headerRow.createCell(2);
            cell.setCellValue(val / 1000);

            cell = headerRow.createCell(3);
            cell.setCellValue(val / 60000);


            headerRow = sheet.createRow(24);
            cell = headerRow.createCell(0);
            cell.setCellValue("50th percentile latency");
            headerCellStyle.setFont(subSectionFont);
            cell.setCellStyle(headerCellStyle);

            val = curData.get50Percentile();
            cell = headerRow.createCell(1);
            cell.setCellValue(val);

            cell = headerRow.createCell(2);
            cell.setCellValue(val / 1000);

            cell = headerRow.createCell(3);
            cell.setCellValue(val / 60000);

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            if (curData.fileType.equals("Global")) {
                headerRow = sheet.createRow(26);
                cell = headerRow.createCell(0);
                cell.setCellValue("Summary/AllDiff file Path : ");
                headerCellStyle.setFont(subSectionFont);
                cell.setCellStyle(headerCellStyle);

                cell = headerRow.createCell(1);
                cell.setCellValue(finalOutputPath + "/summary.xlsx");


                headerRow = sheet.createRow(27);
                cell = headerRow.createCell(0);
                cell.setCellValue("Individual Diff file path : ");
                headerCellStyle.setFont(subSectionFont);
                cell.setCellStyle(headerCellStyle);

                cell = headerRow.createCell(1);
                cell.setCellValue(finalOutputPath + "/IndividualDiff/");
            }
        }
        // Write the output to a file
        try {
            FileOutputStream fileOut = new FileOutputStream(finalOutputPath + "/RunReport.xlsx");
            workbook.write(fileOut);
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateComparisionSummary(ArrayList<String[]> summaryData) {
        StatusLogger.AddRecordInfoExec("Creating Summary CSV Data");
        writeCSVFile(summaryData, new String[]{"File Name", "Exactly Same", "Number of Differences", "timeTaken(in ms)"}, finalOutputPath + "/summary.xlsx");
    }

    private void generateGlobalDiffReport(ArrayList<String[]> allDiffs) {
        StatusLogger.AddRecordInfoExec("Creating All Diff CSV Data");
        StatusLogger.AddRecordInfoExec("Total number of Diffs found : " + allDiffs.size());
        writeCSVFile(allDiffs, new String[]{"File Name", "tag_name", "content1", "content2", "details"}, finalOutputPath + "/allDiff.xlsx");
    }

    private void generateIndividualDiffReport(String s, ArrayList<DiffObject> temp) {
        StatusLogger.AddRecordInfoExec("Creating Individual CSV Data in " + s);
        ArrayList<String[]> entry = new ArrayList<>();
        for (DiffObject diffObject : temp) {
            entry.add(diffObject.getCsvEntry());
        }
        writeCSVFile(entry, new String[]{"tag_name", "diff_type (0-Text,1-Comment)", "content1", "content2", "details"}, finalOutputPath + "/IndividualDiff/" + s + ".xlsx");
    }

}
