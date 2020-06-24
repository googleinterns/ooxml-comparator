import org.json.simple.JSONObject;

import java.util.ArrayList;

public class DocxFile extends OoxmlFile {

    public final String type = "docx";
    public ArrayList<String> filesToCompare;
    public ArrayList<String> commentToCompare;

    public DocxFile(String folderPath, boolean roundtripped) {
        super(folderPath, roundtripped);
        loadFromPath();
        filesToCompare = new ArrayList<String>();
        commentToCompare = new ArrayList<String>();
        this.filesToCompare.add("word_document.xml.json");
        if(jsonFiles.containsKey("word_comments.xml.json")){
            commentToCompare.add("word_comments.xml.json");
        }
    }

    public ArrayList<JSONObject> getJson() {
        ArrayList<JSONObject> allJson = new ArrayList<JSONObject>();
        for (String files : filesToCompare) {
            allJson.add(getJson(files));
        }
        return allJson;
    }

    public ArrayList<JSONObject> getCommentJson(){
        ArrayList<JSONObject> allJson = new ArrayList<JSONObject>();
        for (String files : commentToCompare) {
            allJson.add(getJson(files));
        }
        return allJson;
    }

    public ArrayList<ArrayList<String>> GetTextContent() {
        ArrayList<ArrayList<String>> allTextTag = new ArrayList<ArrayList<String>>();

        for (JSONObject file : getJson()) {

            ArrayList<String> tags = new ArrayList<String>();
            tags.add("w:r");
            ArrayList<JSONObject> wrTag = JsonUtility.extract_tag(file, tags);

            for (JSONObject wrTagObj : wrTag) {
                System.out.println(wrTagObj);
                ArrayList<String> temp = JsonUtility.getTextContent(wrTagObj);
                if(!temp.isEmpty()){
                    allTextTag.add(temp);
                }
            }
        }

        return allTextTag;
    }

    public ArrayList<ArrayList<String>> GetCommentContent() {
        ArrayList<ArrayList<String>> allCommentTag = new ArrayList<ArrayList<String>>();
        for (JSONObject file : getCommentJson()) {
            ArrayList<String> tags = new ArrayList<String>();
            tags.add("w:comment");
            JSONObject wCommentTag = JsonUtility.extract_tag(file, tags).get(0);

            // TODO: Check in any case it may be single JSONObject

            ArrayList<JSONObject> allCommentTagJson = (ArrayList<JSONObject>) wCommentTag.get("w:comment");
            for (JSONObject wCommentTagObj : allCommentTagJson){
                System.out.println(wCommentTagObj);
                ArrayList<String> temp = JsonUtility.getCommentContentDocx(wCommentTagObj);
                allCommentTag.add(temp);
                System.out.println(temp.toString());
            }
        }
        return allCommentTag;
    }

    public void debug() {
        System.out.println(filesToCompare.toString());
    }
}
