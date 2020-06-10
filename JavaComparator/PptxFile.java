import org.json.simple.JSONObject;

import java.util.ArrayList;

public class PptxFile extends OoxmlFile {
    public final String type = "pptx";
    public ArrayList<String> filesToCompare;

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

    public void debug() {
        System.out.println(filesToCompare.toString());
    }
}
