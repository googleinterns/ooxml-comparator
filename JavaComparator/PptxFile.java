import com.google.gson.JsonObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class PptxFile extends OoxmlFile {
    public final String type = "pptx";
    public ArrayList<String> filesToCompare;
    public ArrayList<String> commentToCompare;

    public PptxFile(String folderPath, boolean roundtripped) {
        super(folderPath, roundtripped);
        loadFromPath();
        filesToCompare = new ArrayList<String>();
        for (int i = 1; ; i++) {
            String xmlName = "ppt_slides_slide" + i + ".xml.json";
            if (jsonFiles.containsKey(xmlName)) {
                filesToCompare.add(xmlName);
            } else {
                break;
            }
        }

        commentToCompare = new ArrayList<String>();
        for(int i = 1; ; i++){
            String xmlName = "ppt_comments_comment" + i + ".xml.json";
            if (jsonFiles.containsKey(xmlName)) {
                commentToCompare.add(xmlName);
            } else {
                break;
            }
        }
    }

    public ArrayList<JSONObject> getJson() {
        //TODO: Add slide number attribute for more details later.
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
            tags.add("a:r");
            ArrayList<JSONObject> arTag = JsonUtility.extract_tag(file, tags);

            for (JSONObject arTagObj : arTag) {
                System.out.println(arTagObj);
                ArrayList<String> temp = JsonUtility.getTextContent(arTagObj);
                if(!temp.isEmpty()){
                    allTextTag.add(temp);
                }
            }
        }

        return allTextTag;
    }

    public HashMap<String,ArrayList<String>> GetAuthorTable(){
        HashMap<String,ArrayList<String>> authorTable = new HashMap<String, ArrayList<String>>();
        if(jsonFiles.containsKey("ppt_commentAuthors.xml.json")){
            JSONObject authorFile = getJson("ppt_commentAuthors.xml.json");
            ArrayList<String> tags = new ArrayList<String>();
            tags.add("p:cmAuthor");
            JSONObject wCommentAuth = JsonUtility.extract_tag(authorFile, tags).get(0);
            System.out.println(wCommentAuth);
            if(wCommentAuth.get("p:cmAuthor") instanceof JSONObject){
                JSONObject obj = (JSONObject) wCommentAuth.get("p:cmAuthor");
                ArrayList<String> data = new ArrayList<String>();
                //data.add((String) obj.get("@initials"));
                data.add((String) obj.get("@name"));
                authorTable.put((String) obj.get("@id"),data);
            }else if(wCommentAuth.get("p:cmAuthor") instanceof JSONArray){
                JSONArray allObj = (JSONArray) wCommentAuth.get("p:cmAuthor");
                for (Object o : allObj) {
                    JSONObject obj = (JSONObject) o;
                    ArrayList<String> data = new ArrayList<String>();
                    //data.add((String) obj.get("@initials"));
                    data.add((String) obj.get("@name"));
                    authorTable.put((String) obj.get("@id"), data);
                }
            }else{
                //TODO: add to logger maybe
            }
        }
        return authorTable;
    }

    public ArrayList<ArrayList<String>> GetCommentContent() {
        ArrayList<ArrayList<String>> allCommentTag = new ArrayList<ArrayList<String>>();
        HashMap<String,ArrayList<String>> authorTable = GetAuthorTable();
        System.out.println(authorTable);

        for (JSONObject file : getCommentJson()) {
            ArrayList<String> tags = new ArrayList<String>();
            tags.add("p:cm");
            JSONObject wCommentTag = JsonUtility.extract_tag(file, tags).get(0);
            System.out.println(wCommentTag);

            if(wCommentTag.get("p:cm") instanceof JSONObject){
                JSONObject wCommentTagObj = (JSONObject) wCommentTag.get("p:cm");
                System.out.println(wCommentTagObj);
                ArrayList<String> temp = JsonUtility.getCommentContentPptx(wCommentTagObj);
                String authorId = temp.get(1);
                temp.remove(1);
                for(String data:authorTable.get(authorId)){
                    temp.add(1,data);
                }
                allCommentTag.add(temp);
                System.out.println(temp.toString());

            }else if(wCommentTag.get("p:cm") instanceof JSONArray){
                JSONArray allCommentTagJson = (JSONArray) wCommentTag.get("p:cm");
                for (Object o : allCommentTagJson){
                    JSONObject wCommentTagObj = (JSONObject) o;
                    System.out.println("HERE");
                    System.out.println(wCommentTagObj);
                    ArrayList<String> temp = JsonUtility.getCommentContentPptx(wCommentTagObj);
                    System.out.println(temp);
                    String authorId = temp.get(1);
                    temp.remove(1);
                    for(String data:authorTable.get(authorId)){
                        temp.add(1,data);
                    }
                    allCommentTag.add(temp);
                    System.out.println(temp.toString());
                }
            }
        }

        return allCommentTag;
    }


    public void debug() {
        System.out.println(filesToCompare.toString());
    }
}
