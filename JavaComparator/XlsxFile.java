import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class XlsxFile extends OoxmlFile {
    public final String type = "xlsx";
    public ArrayList<String> filesToCompare;
    public ArrayList<ArrayList<String>> sharedString;

    public XlsxFile(String folderPath, boolean roundtripped) {
        super(folderPath, roundtripped);
        loadFromPath();
        filesToCompare = new ArrayList<String>();
        for (int i = 1; ; i++) {
            String xmlName = "xl_worksheets_sheet" + i + ".xml.json";
            if (jsonFiles.containsKey(xmlName)) {
                filesToCompare.add(xmlName);
            } else {
                break;
            }
        }
    }

    public void loadSharedStrings(){
        printJsons("xl_sharedStrings.xml.json");
        ArrayList<String> tags = new ArrayList<String>();
        tags.add("si");
        ArrayList<JSONObject> temp = JsonUtility.extract_tag(getJson("xl_sharedStrings.xml.json"), tags);
        assert(temp.size()==1);

        sharedString = new ArrayList<ArrayList<String>>();

        System.out.println(temp);
        JSONArray shared_elems = (JSONArray) temp.get(0).get("si");
        for(Object data:shared_elems){
            sharedString.add(JsonUtility.getTextContent((JSONObject) data));
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
            tags.add("sheetData");

            ArrayList<JSONObject> sheetData = JsonUtility.extract_tag(file,tags);

            tags = new ArrayList<String>();
            tags.add("c");

            assert(sheetData.size()==1);
            ArrayList<JSONObject> cTag = JsonUtility.extract_tag(sheetData.get(0), tags);

            for (JSONObject cTagObj : cTag) {
                System.out.println(cTagObj);
                if(cTagObj.get("c") instanceof JSONObject){
                    JSONObject val = (JSONObject) cTagObj.get("c");
                    if (val.containsKey("@t") && val.get("@t") == "s") {
                        allTextTag.add(sharedString.get((Integer) val.get("v")));
                    } else {
                        ArrayList<String> cellValue = new ArrayList<String>();
                        if (val.containsKey("@r")) {
                            cellValue.add((String) val.get("@r"));
                        }
                        if (val.containsKey("v")) {
                            cellValue.add((String) val.get("v"));
                        }
                        if (val.containsKey("f")) {
                            cellValue.add((String) val.get("f"));
                        }
                        allTextTag.add(cellValue);
                    }
                }
                else {
                    JSONArray tempVal = (JSONArray) cTagObj.get("c");
                    for (int i = 0; i < tempVal.size(); i++) {
                        JSONObject val = (JSONObject) tempVal.get(i);
                        if (val.containsKey("@t") && val.get("@t") == "s") {
                            allTextTag.add(sharedString.get((Integer) val.get("v")));
                        } else {
                            ArrayList<String> cellValue = new ArrayList<String>();
                            if (val.containsKey("@r")) {
                                cellValue.add((String) val.get("@r"));
                            }
                            if (val.containsKey("v")) {
                                cellValue.add((String) val.get("v"));
                            }
                            if (val.containsKey("f")) {
                                cellValue.add((String) val.get("f"));
                            }
                            allTextTag.add(cellValue);
                        }
                    }
                }
            }
        }
        return allTextTag;
    }

    public void debug() {
        System.out.println(filesToCompare.toString());
    }
}
