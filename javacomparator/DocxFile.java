import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * DocxFile Class implments the methods required for comparasions of DOCX files.
 */
public class DocxFile extends OoxmlFile {

    public static final String DOCX_WORD_DOCUMENT_FILE = "word_document.xml.json";
    public static final String DOCX_WORD_COMMENTS_FILE = "word_comments.xml.json";
    public static final String DOCX_TAG_TO_COMPARE_TEXT = "w:r";
    public static final String DOCX_TAG_TO_GET_TEXT_CONTENT = "w:t";
    public static final String DOCX_TAG_TO_COMPARE_COMMENT = "w:comment";

    public ArrayList<String> filesToCompare;
    public ArrayList<String> commentToCompare;
    ArrayList<ArrayList<String>> allCommentTag;

    /**
     * Constuctor takes in the FolderPath for the file and creates a list of files to be compared.
     * @param folderPath Folder path that has the OOXML content of the file in JSON format.
     * @param roundtripped Whether the file is roundTripped file or not.
     */
    public DocxFile(String folderPath, boolean roundtripped) {
        super(folderPath, roundtripped);
        loadFromPath();
        filesToCompare = new ArrayList<>();
        commentToCompare = new ArrayList<>();
        this.filesToCompare.add(DOCX_WORD_DOCUMENT_FILE);
        if(jsonDataFiles.containsKey(DOCX_WORD_COMMENTS_FILE)){
            commentToCompare.add(DOCX_WORD_COMMENTS_FILE);
        }
    }

    /**
     * @return the JSON for the files to be compared for text comparision.
     */
    public ArrayList<JSONObject> getTextJson() {
        ArrayList<JSONObject> allJson = new ArrayList<>();
        for (String files : filesToCompare) {
            allJson.add(getJson(files));
        }
        return allJson;
    }

    /**
     * @return the JSON for the files to be compared for comment comparision.
     */
    public ArrayList<JSONObject> getCommentJson(){
        ArrayList<JSONObject> allJson = new ArrayList<>();
        for (String files : commentToCompare) {
            allJson.add(getJson(files));
        }
        return allJson;
    }

    /**
     * The functions implement the total running of tag extraction for text (w:r) and then in the subtree, finding for strings to be matched.
     * @return List of List, where each of them contains the total data to be compared for each 'w:r' tag.
     */
    public ArrayList<ArrayList<String>> getTextContent() {
        ArrayList<ArrayList<String>> allTextTag = new ArrayList<>();

        for (JSONObject file : getTextJson()) {

            ArrayList<String> tags = new ArrayList<>();
            tags.add(DOCX_TAG_TO_COMPARE_TEXT);
            ArrayList<JSONObject> wrTag = JsonUtility.extractTag(file, tags);

            for (JSONObject wrTagObj : wrTag) {
                ArrayList<String> textContent = JsonUtility.getTextContent(wrTagObj, new ArrayList<>(Arrays.asList(DOCX_TAG_TO_GET_TEXT_CONTENT)),false);
                if(!textContent.isEmpty()){
                    allTextTag.add(textContent);
                }
            }
        }

        return allTextTag;
    }

    private void extractwcommentTag(JSONObject wCommentTagObj){
        ArrayList<String> commentContent = JsonUtility.getCommentContentDocx(wCommentTagObj);
        allCommentTag.add(commentContent);
    }

    /**
     * The functions implement the total running of tag extraction for text (w:comment tag) and then in the subtree, finding for strings to be matched.
     * @return List of List, where each of them contains the total data to be compared for each 'w:comment' tag.
     */
    public ArrayList<ArrayList<String>> getCommentContent() {
        allCommentTag = new ArrayList<>();
        for (JSONObject file : getCommentJson()) {
            ArrayList<String> tags = new ArrayList<>();
            tags.add(DOCX_TAG_TO_COMPARE_COMMENT);
            JSONObject wCommentTag = JsonUtility.extractTag(file, tags).get(0);
            if (wCommentTag.get(DOCX_TAG_TO_COMPARE_COMMENT) instanceof JSONObject) {
                JSONObject wCommentTagObj = (JSONObject) wCommentTag.get(DOCX_TAG_TO_COMPARE_COMMENT);
                extractwcommentTag(wCommentTagObj);
            } else {
                ArrayList<JSONObject> allCommentTagJson = (ArrayList<JSONObject>) wCommentTag.get(DOCX_TAG_TO_COMPARE_COMMENT);
                for (JSONObject wCommentTagObj : allCommentTagJson) {
                    extractwcommentTag(wCommentTagObj);
                }
            }
        }
        allCommentTag.sort(Comparator.comparing(a -> a.get(0)));
        return allCommentTag;
    }

}
