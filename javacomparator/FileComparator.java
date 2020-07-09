import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Class to compare 2 files to find out the Diffs.
 */
public class FileComparator {

    public static final String DOCX_FILE_TYPE = "docx";
    public static final String PPTX_FILE_TYPE = "pptx";
    public static final String XLSX_FILE_TYPE = "xlsx";
    public static final String INVALID_FILE_TYPE = "invalid";
    public static final List<Character> CHARACTER_IGNORE_LIST =  Arrays.asList(' ', '\t', '\n');
    public static final String CONTENT_DIFFERENT_MESSAGE_FOR_REPORT = "TEXT CONTENT DIFFERENT";
    String fileOriginalPath, fileRoundtrippedPath;
    String fileExtension;
    public ArrayList<DiffObject> diffReport;

    /**
     * Constructor to load the File Paths of files to be compared and decide on the file type.
     * @param fileOriginalPath
     * @param fileRoundtrippedPath
     */
    FileComparator(String fileOriginalPath, String fileRoundtrippedPath) {
        this.fileOriginalPath = fileOriginalPath;
        this.fileRoundtrippedPath = fileRoundtrippedPath;

        if (fileOriginalPath.endsWith(DOCX_FILE_TYPE) && fileRoundtrippedPath.endsWith(DOCX_FILE_TYPE)) {
            fileExtension = DOCX_FILE_TYPE;
        } else if (fileOriginalPath.endsWith(PPTX_FILE_TYPE) && fileRoundtrippedPath.endsWith(PPTX_FILE_TYPE)) {
            fileExtension = PPTX_FILE_TYPE;
        } else if (fileOriginalPath.endsWith(XLSX_FILE_TYPE) && fileRoundtrippedPath.endsWith(XLSX_FILE_TYPE)) {
            fileExtension = XLSX_FILE_TYPE;
        } else {
            fileExtension = INVALID_FILE_TYPE;
            StatusLogger.addRecordWarningExec("Either Path is invalid / File types do not match");
        }
    }

    /**
     * The main comparision logic for comparision of two strings
     *
     * @param content1 List of string of content for tag1
     * @param content2 List of string of content for tag2
     * @return whether the content of the two tags are same or not
     */
    public boolean comparisionLogic(ArrayList<String> content1, ArrayList<String> content2) {

        StringBuilder stringBuilder = new StringBuilder();
        for (String contentVal : content1) {
            try {
                float val = Float.parseFloat(contentVal);
                contentVal = String.valueOf(Math.round(val));
                stringBuilder.append(contentVal);
            } catch (NumberFormatException e) {
                for (int i = 0; i < contentVal.length(); i++) {
                    if (!CHARACTER_IGNORE_LIST.contains(contentVal.charAt(i))) {
                        stringBuilder.append(Character.toLowerCase(contentVal.charAt(i)));
                    }
                }
            }
        }
        String stringVal1 = (stringBuilder.toString());
        stringBuilder = new StringBuilder();
        for (String contentVal : content2) {
            try {
                float val = Float.parseFloat(contentVal);
                contentVal = String.valueOf(Math.round(val));
                stringBuilder.append(contentVal);
            } catch (NumberFormatException e) {
                for (int i = 0; i < contentVal.length(); i++) {
                    if (!CHARACTER_IGNORE_LIST.contains(contentVal.charAt(i))) {
                        stringBuilder.append(Character.toLowerCase(contentVal.charAt(i)));
                    }
                }
            }
        }
        String stringVal2 = stringBuilder.toString();
        return stringVal1.equals(stringVal2);
    }
    
    /**
     * Compares the Tags extracted from the two subtree by the order in which they appear.
     * @param tagComp Tag the JSONSubtree is extracted for
     * @param type "0" for text, "1" for comment tag
     * @param FileType Type of file the matching is done for.
     * @param TagContents1 Content of the First Subtree, from the Original OOXML file.
     * @param TagContents2 Content of the Second Subtree, from the Roundtripped OOXML file.
     */
    private void compareContentByOrder(String tagComp, String type, String FileType, ArrayList<ArrayList<String>> TagContents1, ArrayList<ArrayList<String>> TagContents2) {
        if (TagContents2.size() != TagContents1.size()) {
            StatusLogger.addRecordInfoExec("NUMBER OF " + tagComp + " in " + FileType + " are not same!!");
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
                diffReport.add(new DiffObject(tagComp, type, TagContents1.get(i), TagContents2.get(i),CONTENT_DIFFERENT_MESSAGE_FOR_REPORT));
            }
        }
        StatusLogger.addRecordInfoExec("MATCHED : " + matched);
    }

    /**
     * Compare the contents for the two subtree ordered by the ID.
     * @param type "0" for text, "1" for comment tag
     * @param TagContents1 Content of the First Subtree, from the Original OOXML file.
     * @param TagContents2 Content of the Second Subtree, from the Roundtripped OOXML file.
     */
    private void compareContentByID(String type, ArrayList<ArrayList<String>> TagContents1, ArrayList<ArrayList<String>> TagContents2) {
        if (TagContents2.size() != TagContents1.size()) {
            StatusLogger.addRecordInfoExec("NUMBER OF c in " + fileExtension + " are not same!!");
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
                    diffReport.add(new DiffObject("c", type, actualContent, it,CONTENT_DIFFERENT_MESSAGE_FOR_REPORT));
                }
            }
        }
        StatusLogger.addRecordInfoExec("MATCHED : " + matched);
    }

    /**
     * Call the relevent function for Docx to get the text, compare them, get the comments and compare them.
     */
    private void compareTextAndCommentDocx(){
        DocxFile file1 = new DocxFile(fileOriginalPath, false);
        DocxFile file2 = new DocxFile(fileRoundtrippedPath, true);

        ArrayList<ArrayList<String>> runTagContents1 = file1.getTextContent();
        ArrayList<ArrayList<String>> runTagContents2 = file2.getTextContent();
        compareContentByOrder(DocxFile.DOCX_TAG_TO_COMPARE_TEXT, DiffGenerator.DIFF_FOUND_TAG_TYPE_TEXT, DOCX_FILE_TYPE, runTagContents1, runTagContents2);

        ArrayList<ArrayList<String>> commentContents1 = file1.getCommentContent();
        ArrayList<ArrayList<String>> commentContents2 = file2.getCommentContent();

        compareContentByOrder(DocxFile.DOCX_TAG_TO_COMPARE_COMMENT, DiffGenerator.DIFF_FOUND_TAG_TYPE_COMMENT, DOCX_FILE_TYPE, commentContents1, commentContents2);
    }

    /**
     * Call the relevent function for Pptx to get the text, compare them, get the comments and compare them.
     */
    private void compareTextAndCommentPptx(){
        PptxFile file1 = new PptxFile(fileOriginalPath, false);
        PptxFile file2 = new PptxFile(fileRoundtrippedPath, true);

        ArrayList<ArrayList<String>> runTagContents1 = file1.getTextContent();
        ArrayList<ArrayList<String>> runTagContents2 = file2.getTextContent();

        compareContentByOrder(PptxFile.PPTX_TAG_TO_COMPARE_TEXT, DiffGenerator.DIFF_FOUND_TAG_TYPE_TEXT, PPTX_FILE_TYPE, runTagContents1, runTagContents2);

        ArrayList<ArrayList<String>> commentContents1 = file1.getCommentContent();
        ArrayList<ArrayList<String>> commentContents2 = file2.getCommentContent();

        compareContentByOrder(PptxFile.PPTX_TAG_TO_COMPARE_COMMENT, DiffGenerator.DIFF_FOUND_TAG_TYPE_COMMENT, PPTX_FILE_TYPE, commentContents1, commentContents2);
    }

    /**
     * Call the relevent function for Xlsx to get the text, compare them, get the comments and compare them.
     */
    private void compareTextAndCommentXlsx(){
        XlsxFile file1 = new XlsxFile(fileOriginalPath, false);
        XlsxFile file2 = new XlsxFile(fileRoundtrippedPath, true);

        file1.loadSharedStrings();
        file2.loadSharedStrings();

        ArrayList<ArrayList<String>> runTagContents1 = file1.GetTextContent();
        ArrayList<ArrayList<String>> runTagContents2 = file2.GetTextContent();

        compareContentByID(DiffGenerator.DIFF_FOUND_TAG_TYPE_TEXT, runTagContents1, runTagContents2);

        ArrayList<ArrayList<String>> CommentTagContents1 = file1.getCommentContent();
        ArrayList<ArrayList<String>> CommentTagContents2 = file2.getCommentContent();

        compareContentByID(DiffGenerator.DIFF_FOUND_TAG_TYPE_COMMENT, CommentTagContents1, CommentTagContents2);
    }

    /**
     * Fucntion that runs the main comparision for Text in the two files
     * @return Diff objects for each differences found
     */
    public ArrayList<DiffObject> compareText() {
        diffReport = new ArrayList<>();

        switch (fileExtension) {
            case DOCX_FILE_TYPE: {
                compareTextAndCommentDocx();
                break;
            }
            case PPTX_FILE_TYPE: {
                compareTextAndCommentPptx();
                break;
            }
            case XLSX_FILE_TYPE: {
                compareTextAndCommentXlsx();
                break;
            }
        }
        return diffReport;
    }
}
