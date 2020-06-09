import org.json.simple.JSONObject;

import java.util.ArrayList;

public class DocxFile extends OoxmlFile {

    public final String type = "docx";
    public ArrayList<String> filesToCompare;

    public DocxFile(String folderPath, boolean roundtripped) {
        super(folderPath, roundtripped);
        loadFromPath();
        filesToCompare = new ArrayList<String>();
        this.filesToCompare.add("word_document.xml.json");
    }

    public ArrayList<JSONObject> getJson() {
        ArrayList<JSONObject> allJson = new ArrayList<JSONObject>();
        for (String files : filesToCompare) {
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
                allTextTag.add(JsonUtility.getTextContent(wrTagObj));
            }
        }

        return allTextTag;
    }

    public void debug() {
        System.out.println(filesToCompare.toString());
    }
}
