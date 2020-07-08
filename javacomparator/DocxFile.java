import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * DocxFile Class implments the methods required for comparasions of DOCX files.
 */
public class DocxFile extends OoxmlFile {

    public final String TYPEFILE = "docx";
    public ArrayList<String> filesToCompare;
    public ArrayList<String> commentToCompare;

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
        this.filesToCompare.add("word_document.xml.json");
        if(jsonDataFiles.containsKey("word_comments.xml.json")){
            commentToCompare.add("word_comments.xml.json");
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
            tags.add("w:r");
            ArrayList<JSONObject> wrTag = JsonUtility.extractTag(file, tags);

            for (JSONObject wrTagObj : wrTag) {
                ArrayList<String> textContent = JsonUtility.getTextContent(wrTagObj, new ArrayList<>(Arrays.asList("w:t")),false);
                if(!textContent.isEmpty()){
                    allTextTag.add(textContent);
                }
            }
        }

        return allTextTag;
    }

    ArrayList<ArrayList<String>> allCommentTag;

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
            tags.add("w:comment");
            JSONObject wCommentTag = JsonUtility.extractTag(file, tags).get(0);
            if (wCommentTag.get("w:comment") instanceof JSONObject) {
                JSONObject wCommentTagObj = (JSONObject) wCommentTag.get("w:comment");
                extractwcommentTag(wCommentTagObj);
            } else {
                ArrayList<JSONObject> allCommentTagJson = (ArrayList<JSONObject>) wCommentTag.get("w:comment");
                for (JSONObject wCommentTagObj : allCommentTagJson) {
                    extractwcommentTag(wCommentTagObj);
                }
            }
        }
        allCommentTag.sort(Comparator.comparing(a -> a.get(0)));
        return allCommentTag;
    }

}
