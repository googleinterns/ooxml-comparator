import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * PptxFile Class implments the methods required for comparasions of PPTX files
 */
public class PptxFile extends OoxmlFile {
    public final String TYPEFILE = "pptx";
    public ArrayList<String> filesToCompare;
    public ArrayList<String> commentToCompare;

    /**
     * Constuctor takes in the FolderPath for the file and creates a list of files to be compared.
     * @param folderPath Folder path that has the OOXML content of the file in JSON format
     * @param roundtripped Whether the file is roundTripped file or not.
     */
    public PptxFile(String folderPath, boolean roundtripped) {
        super(folderPath, roundtripped);
        loadFromPath();
        filesToCompare = new ArrayList<>();
        for (int i = 1; ; i++) {
            String xmlFileName = "ppt_slides_slide" + i + ".xml.json";
            if (jsonDataFiles.containsKey(xmlFileName)) {
                filesToCompare.add(xmlFileName);
            } else {
                break;
            }
        }

        commentToCompare = new ArrayList<>();
        for(int i = 1; ; i++){
            String xmlFileName = "ppt_comments_comment" + i + ".xml.json";
            if (jsonDataFiles.containsKey(xmlFileName)) {
                commentToCompare.add(xmlFileName);
            } else {
                break;
            }
        }
    }

    /**
     * @return the JSON for the files to be compared for text comparision
     */
    private ArrayList<JSONObject> getTextJson() {
        ArrayList<JSONObject> allJson = new ArrayList<>();
        for (String files : filesToCompare) {
            allJson.add(getJson(files));
        }
        return allJson;
    }

    /**
     * @return the JSON for the files to be compared for comments comparision
     */
    private ArrayList<JSONObject> getCommentJson(){
        ArrayList<JSONObject> allJson = new ArrayList<>();
        for (String files : commentToCompare) {
            allJson.add(getJson(files));
        }
        return allJson;
    }

    /**
     * The functions implement the total running of tag extraction for text (a:r tag) and then in the subtree, finding for strings to be matched
     * @return List of List, where each of them contains the total data to be compared for each 'a:r' tag
     */
    public ArrayList<ArrayList<String>> getTextContent() {
        ArrayList<ArrayList<String>> allTextTag = new ArrayList<>();

        for (JSONObject file : getTextJson()) {

            ArrayList<String> tags = new ArrayList<>();
            tags.add("a:r");
            ArrayList<JSONObject> arTag = JsonUtility.extractTag(file, tags);

            for (JSONObject arTagObj : arTag) {
                ArrayList<String> temp = JsonUtility.getTextContent(arTagObj, new ArrayList<>(Arrays.asList("a:t")),false);
                if(!temp.isEmpty()){
                    allTextTag.add(temp);
                }
            }
        }

        return allTextTag;
    }

    /**
     * @return Hashmap of author data of the File hashed by the ID.
     */
    private HashMap<String,ArrayList<String>> getAuthorTable(){
        HashMap<String,ArrayList<String>> authorTable = new HashMap<>();
        if(jsonDataFiles.containsKey("ppt_commentAuthors.xml.json")){
            JSONObject authorFile = getJson("ppt_commentAuthors.xml.json");
            ArrayList<String> tags = new ArrayList<>();
            tags.add("p:cmAuthor");
            JSONObject wCommentAuth = JsonUtility.extractTag(authorFile, tags).get(0);

            if(wCommentAuth.get("p:cmAuthor") instanceof JSONObject){
                JSONObject obj = (JSONObject) wCommentAuth.get("p:cmAuthor");
                ArrayList<String> data = new ArrayList<>();
                data.add((String) obj.get("@name"));
                authorTable.put((String) obj.get("@id"),data);

            }else if(wCommentAuth.get("p:cmAuthor") instanceof JSONArray){
                JSONArray allObj = (JSONArray) wCommentAuth.get("p:cmAuthor");
                for (Object object : allObj) {
                    JSONObject obj = (JSONObject) object;
                    ArrayList<String> data = new ArrayList<>();
                    data.add((String) obj.get("@name"));
                    authorTable.put((String) obj.get("@id"), data);
                }
            }else{
                StatusLogger.addRecordInfoDebug("Author Table not Handled");
                StatusLogger.addRecordInfoDebug(wCommentAuth.toString());
            }
        }
        return authorTable;
    }

    ArrayList<ArrayList<String>> allCommentTag;
    HashMap<String,ArrayList<String>> authorTable;

    private void extractpcmTag(JSONObject wCommentTagObj){
        ArrayList<String> temp = JsonUtility.getCommentContentPptx(wCommentTagObj);
        String authorId = temp.get(1);
        temp.remove(1);
        for(String data:authorTable.get(authorId)){
            temp.add(1,data);
        }
        allCommentTag.add(temp);
    }

    /**
     * The functions implement the total running of tag extraction for text (comment tag) and then in the subtree, finding for strings to be matched
     * @return List of List, where each of them contains the total data to be compared for each 'p:cm' tag
     */
    public ArrayList<ArrayList<String>> getCommentContent() {

        allCommentTag = new ArrayList<>();
        authorTable = getAuthorTable();

        for (JSONObject file : getCommentJson()) {
            ArrayList<String> tags = new ArrayList<>();
            tags.add("p:cm");
            JSONObject wCommentTag = JsonUtility.extractTag(file, tags).get(0);

            if(wCommentTag.get("p:cm") instanceof JSONObject){
                JSONObject wCommentTagObj = (JSONObject) wCommentTag.get("p:cm");
                extractpcmTag(wCommentTagObj);
            }else if(wCommentTag.get("p:cm") instanceof JSONArray){
                JSONArray allCommentTagJson = (JSONArray) wCommentTag.get("p:cm");
                for (Object o : allCommentTagJson){
                    JSONObject wCommentTagObj = (JSONObject) o;
                    extractpcmTag(wCommentTagObj);
                }
            }
        }

        return allCommentTag;
    }
}
