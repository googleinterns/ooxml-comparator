import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class FileComparator {
    String fileOriginalPath, fileRoundtrippedPath;
    String fileExtension;

    FileComparator(String Path1, String Path2) {
        fileOriginalPath = Path1;
        fileRoundtrippedPath = Path2;

        if (Path1.endsWith("docx") && Path2.endsWith("docx")) {
            fileExtension = "docx";
        } else if (Path1.endsWith("pptx") && Path2.endsWith("pptx")) {
            fileExtension = "pptx";
        } else if (Path1.endsWith("xlsx") && Path2.endsWith("xlsx")) {
            fileExtension = "xlsx";
        } else {
            fileExtension = "invalid";
            System.out.println("Either Path is invalid / File types do not match");
        }
    }

    /**
     * The main comparision logic for comparision of two strings
     *
     * @param content1 List of string of content for tag1
     * @param content2 List of string of content for tag2
     * @return whether the content of the two tags are same or not
     */
    private boolean comparisionLogic(ArrayList<String> content1, ArrayList<String> content2) {
        ArrayList<Character> delim = new ArrayList<>(Arrays.asList(' ', '\t', '\n'));
        StringBuilder stringBuilder = new StringBuilder();
        for (String contentVal : content1) {
            try {
                float val = Float.parseFloat(contentVal);
                contentVal = String.valueOf(Math.round(val));
            } catch (NumberFormatException e) {
                // not a float!!
            }

            for (int i = 0; i < contentVal.length(); i++) {
                if (!delim.contains(contentVal.charAt(i))) {
                    stringBuilder.append(Character.toLowerCase(contentVal.charAt(i)));
                }
            }
        }
        String stringVal1 = (stringBuilder.toString());
        stringBuilder = new StringBuilder();
        for (String contentVal : content2) {
            try {
                float val = Float.parseFloat(contentVal);
                contentVal = String.valueOf(Math.round(val));
            } catch (NumberFormatException e) {
                // not a float!!
            }
            for (int i = 0; i < contentVal.length(); i++) {
                if (!delim.contains(contentVal.charAt(i))) {
                    stringBuilder.append(Character.toLowerCase(contentVal.charAt(i)));
                }
            }
        }
        String stringVal2 = stringBuilder.toString();
        return stringVal1.equals(stringVal2);
    }

    public ArrayList<DiffObject> diffReport;

    private void compareContentByOrder(String tagComp, String type, String FileType, ArrayList<ArrayList<String>> TagContents1, ArrayList<ArrayList<String>> TagContents2) {
        if (TagContents2.size() != TagContents1.size()) {
            StatusLogger.AddRecordInfoExec("NUMBER OF " + tagComp + " in " + FileType + " are not same!!");
            ArrayList<String> fillerData = new ArrayList<>();
            while (TagContents1.size() < TagContents2.size()) {
                TagContents1.add(fillerData);
            }
            while (TagContents1.size() > TagContents2.size()) {
                TagContents2.add(fillerData);
            }
        }

        int runTagCount = Math.min(TagContents1.size(), TagContents2.size());

        int matched = 0;
        for (int i = 0; i < runTagCount; i++) {
            boolean isSame = comparisionLogic(TagContents1.get(i), TagContents2.get(i));
            matched += 1;
            if (!isSame) {
                diffReport.add(new DiffObject(tagComp, type, TagContents1.get(i), TagContents2.get(i), "CONTENT DIFFERENT"));
            }
        }
        StatusLogger.AddRecordInfoExec("MATCHED : " + matched);
    }

    private void compareContentByID(String type, ArrayList<ArrayList<String>> TagContents1, ArrayList<ArrayList<String>> TagContents2, String diffMsg) {
        if (TagContents2.size() != TagContents1.size()) {
            StatusLogger.AddRecordInfoExec("NUMBER OF c in " + fileExtension + " are not same!!");
        }

        HashMap<String, ArrayList<String>> cellValues = new HashMap<>();
        for (ArrayList<String> it : TagContents1) {
            cellValues.put(it.get(0), it);
        }

        int matched = 0;
        for (ArrayList<String> it : TagContents2) {
            if (cellValues.containsKey(it.get(0))) {
                matched += 1;
                ArrayList<String> actualContent = cellValues.get(it.get(0));
                boolean isSame = comparisionLogic(actualContent, it);
                if (!isSame) {
                    diffReport.add(new DiffObject("c", type, actualContent, it, diffMsg));
                }
            }
        }
        StatusLogger.AddRecordInfoExec("MATCHED : " + matched);
    }

    /**
     * Fucntion that runs the main comparision for Text in the two files
     *
     * @return Diff objects for each differences found
     */
    public ArrayList<DiffObject> CompareText() {
        diffReport = new ArrayList<>();

        switch (fileExtension) {
            case "docx": {
                DocxFile file1 = new DocxFile(fileOriginalPath, false);
                DocxFile file2 = new DocxFile(fileRoundtrippedPath, true);

                ArrayList<ArrayList<String>> runTagContents1 = file1.getTextContent();
                ArrayList<ArrayList<String>> runTagContents2 = file2.getTextContent();
                compareContentByOrder("w:r", "0", "docx", runTagContents1, runTagContents2);

                ArrayList<ArrayList<String>> commentContents1 = file1.getCommentContent();
                ArrayList<ArrayList<String>> commentContents2 = file2.getCommentContent();

                compareContentByOrder("w:comment", "1", "docx", commentContents1, commentContents2);
                break;
            }
            case "pptx": {

                PptxFile file1 = new PptxFile(fileOriginalPath, false);
                PptxFile file2 = new PptxFile(fileRoundtrippedPath, true);

                ArrayList<ArrayList<String>> runTagContents1 = file1.getTextContent();
                ArrayList<ArrayList<String>> runTagContents2 = file2.getTextContent();

                StatusLogger.AddRecordInfoDebug("BEFORE Content Comparator");
                compareContentByOrder("a:r", "0", "pptx", runTagContents1, runTagContents2);
                StatusLogger.AddRecordInfoDebug("AFTER Content Comparator");

                ArrayList<ArrayList<String>> commentContents1 = file1.getCommentContent();
                ArrayList<ArrayList<String>> commentContents2 = file2.getCommentContent();

                compareContentByOrder("p:cm", "1", "pptx", commentContents1, commentContents2);
                break;
            }
            case "xlsx": {

                XlsxFile file1 = new XlsxFile(fileOriginalPath, false);
                XlsxFile file2 = new XlsxFile(fileRoundtrippedPath, true);

                file1.loadSharedStrings();
                file2.loadSharedStrings();
                StatusLogger.AddRecordInfoDebug("After Shared String Loading");

                ArrayList<ArrayList<String>> runTagContents1 = file1.GetTextContent();
                ArrayList<ArrayList<String>> runTagContents2 = file2.GetTextContent();

                StatusLogger.AddRecordInfoDebug("Before content comparator");
                compareContentByID("0", runTagContents1, runTagContents2, "Cell value different");
                StatusLogger.AddRecordInfoDebug("AFTER Content Comparator");

                ArrayList<ArrayList<String>> CommentTagContents1 = file1.getCommentContent();
                ArrayList<ArrayList<String>> CommentTagContents2 = file2.getCommentContent();
                StatusLogger.AddRecordInfoDebug("After GetCommentContent");

                compareContentByID("1", CommentTagContents1, CommentTagContents2, "Cell Comment Different");
                break;
            }
        }
        return diffReport;
    }
}
