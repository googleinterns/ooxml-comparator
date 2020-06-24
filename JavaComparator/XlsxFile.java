import org.apache.openjpa.persistence.jest.JSON;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class XlsxFile extends OoxmlFile {
    public final String type = "xlsx";
    public ArrayList<String> filesToCompare,commentToCompare;
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


        commentToCompare = new ArrayList<String>();
        for(int i = 1; ; i++){
            String xmlName = "xl_comments" + i + ".xml.json";
            if (jsonFiles.containsKey(xmlName)) {
                commentToCompare.add(xmlName);
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
        int counter = 0;
        for (String files : filesToCompare) {
            JSONObject temp = new JSONObject();
            temp.put("file",String.valueOf(counter));
            counter++;
            temp.put("sheet_data",getJson(files));
            allJson.add((temp));
        }
        return allJson;
    }

    public ArrayList<JSONObject> getCommentJson(){
        ArrayList<JSONObject> allJson = new ArrayList<JSONObject>();
        int counter = 0;
        for (String files : commentToCompare) {
            JSONObject temp = new JSONObject();
            temp.put("file",String.valueOf(counter));
            counter++;
            temp.put("sheet_data",getJson(files));
            allJson.add((temp));
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
                //System.out.println(cTagObj);
                if(cTagObj.get("c") instanceof JSONObject){
                    JSONObject val = (JSONObject) cTagObj.get("c");
                    //System.out.println(val.toString());
                    if (val.containsKey("@t") && val.get("@t").equals("s")) {
                        ArrayList<String> cellValue = sharedString.get(Integer.parseInt((String) val.get("v")));
                        if(val.containsKey("@r")){
                            cellValue.add(0,(String)  file.get("file")+val.get("@r"));
                            //cellValue.add("Shared String Content");
                            allTextTag.add(cellValue);
                        }else{
                            StatusLogger.AddRecordWARNING("This Element contains @t=s and no @r, looking it!!");
                            StatusLogger.AddRecordWARNING(cellValue.toString());
                        }
                    }
                    else if(val.containsKey("@t") && val.get("@t").equals("str")){
                        ArrayList<String> cellValue = new ArrayList<String>();
                        if(val.containsKey("@r")){
                            cellValue.add((String)  file.get("file")+val.get("@r"));
                            //cellValue.add("@t = str type string");
                        }else{
                            StatusLogger.AddRecordWARNING("This Element contains @t=str and no @r, looking it!!");
                            StatusLogger.AddRecordWARNING(cellValue.toString());
                        }
                        if (val.containsKey("f")) {
                            if(val.get("f") instanceof JSONObject){
                                JSONObject fval = (JSONObject) val.get("f");
                                if(fval.containsKey("#text"))cellValue.add((String)fval.get("#text"));
                                if(fval.containsKey("@t"))cellValue.add((String)fval.get("@t"));
                                if(fval.containsKey("@ref"))cellValue.add((String)fval.get("@ref"));
                            }else {
                                cellValue.add((String) val.get("f"));
                            }
                        }
                        allTextTag.add(cellValue);
                    }
                    else {
                        ArrayList<String> cellValue = new ArrayList<String>();
                        if (val.containsKey("@r")) {
                            cellValue.add((String)  file.get("file")+val.get("@r"));
                            //cellValue.add("Normal Cell content");
                        }else{
                            StatusLogger.AddRecordWARNING("This Element no @r, looking it!!");
                            StatusLogger.AddRecordWARNING(cellValue.toString());
                        }
                        if (val.containsKey("v")) {
                            cellValue.add((String) val.get("v"));
                            //NOTE: if we want to prune out empty cells, we can do it here!!
                        }
                        if (val.containsKey("f")) {
                            if(val.get("f") instanceof JSONObject){
                                JSONObject fval = (JSONObject) val.get("f");
                                if(fval.containsKey("#text"))cellValue.add((String)fval.get("#text"));
                                if(fval.containsKey("@t"))cellValue.add((String)fval.get("@t"));
                                if(fval.containsKey("@ref"))cellValue.add((String)fval.get("@ref"));
                            }else {
                                cellValue.add((String) val.get("f"));
                            }
                        }
                        allTextTag.add(cellValue);
                    }
                }
                else {
                    JSONArray tempVal = (JSONArray) cTagObj.get("c");
                    for (int i = 0; i < tempVal.size(); i++) {
                        JSONObject val = (JSONObject) tempVal.get(i);
                        //System.out.println(val.toString());
                        if (val.containsKey("@t") && val.get("@t").equals("s")) {
                            ArrayList<String> cellValue = sharedString.get(Integer.parseInt((String) val.get("v")));
                            if(val.containsKey("@r")){
                                cellValue.add(0, (String) file.get("file")+val.get("@r"));
                                //cellValue.add("Shared String Content");
                                allTextTag.add(cellValue);
                            }else{
                                StatusLogger.AddRecordWARNING("This Element contains @t=s and no @r, looking it!!");
                                StatusLogger.AddRecordWARNING(cellValue.toString());
                            }
                        }
                        else if(val.containsKey("@t") && val.get("@t").equals("str")){
                            ArrayList<String> cellValue = new ArrayList<String>();
                            if(val.containsKey("@r")){
                                cellValue.add((String)  file.get("file")+val.get("@r"));
                                //cellValue.add("@t = str type string");
                            }else{
                                StatusLogger.AddRecordWARNING("This Element contains @t=str and no @r, looking it!!");
                                StatusLogger.AddRecordWARNING(cellValue.toString());
                            }
                            if (val.containsKey("f")) {
                                if(val.get("f") instanceof JSONObject){
                                    JSONObject fval = (JSONObject) val.get("f");
                                    if(fval.containsKey("#text"))cellValue.add((String)fval.get("#text"));
                                    if(fval.containsKey("@t"))cellValue.add((String)fval.get("@t"));
                                    if(fval.containsKey("@ref"))cellValue.add((String)fval.get("@ref"));
                                }else {
                                    cellValue.add((String) val.get("f"));
                                }
                            }
                            allTextTag.add(cellValue);
                        }
                        else {
                            ArrayList<String> cellValue = new ArrayList<String>();
                            if (val.containsKey("@r")) {
                                cellValue.add((String)  file.get("file")+val.get("@r"));
                                //cellValue.add("Normal cell content");
                            }else{
                                StatusLogger.AddRecordWARNING("This Element no @r, looking it!!");
                                StatusLogger.AddRecordWARNING(cellValue.toString());
                            }
                            if (val.containsKey("v")) {
                                cellValue.add((String) val.get("v"));
                                //NOTE: if we want to prune out empty cells, we can do it here!!
                            }
                            if (val.containsKey("f")) {
                                if(val.get("f") instanceof JSONObject){
                                    JSONObject fval = (JSONObject) val.get("f");
                                    if(fval.containsKey("#text"))cellValue.add((String)fval.get("#text"));
                                    if(fval.containsKey("@t"))cellValue.add((String)fval.get("@t"));
                                    if(fval.containsKey("@ref"))cellValue.add((String)fval.get("@ref"));
                                }else {
                                    cellValue.add((String) val.get("f"));
                                }
                            }
                            allTextTag.add(cellValue);
                        }
                    }
                }
            }
        }
        return allTextTag;
    }

    public ArrayList<ArrayList<String>> GetCommentContent(){
        ArrayList<ArrayList<String>> allCommentTag = new ArrayList<ArrayList<String>>();

        for (JSONObject file : getCommentJson()) {
            ArrayList<String> tags = new ArrayList<String>();
            tags.add("comment");
            JSONObject wCommentTag = JsonUtility.extract_tag(file, tags).get(0);

            // TODO: Check in any case it may be single JSONObject
            ArrayList<JSONObject> allCommentTagJson = (ArrayList<JSONObject>) wCommentTag.get("comment");
            for (JSONObject wCommentTagObj : allCommentTagJson){
                //System.out.println(wCommentTagObj);
                ArrayList<String> temp = JsonUtility.getCommentContentXlsx(wCommentTagObj, (String) file.get("file"));
                allCommentTag.add(temp);
                //System.out.println(temp.toString());
            }
        }
        return allCommentTag;
    }

    public void debug() {
        System.out.println(filesToCompare.toString());
    }
}
