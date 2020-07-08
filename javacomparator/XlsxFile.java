import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;


/**
 * XlsxFile Class implments the methods required for comparasions of XLSX files
 */
public class XlsxFile extends OoxmlFile {
    public ArrayList<String> filesToCompare,commentToCompare;
    public ArrayList<ArrayList<String>> sharedString;

    /**
     * Constuctor takes in the FolderPath for the file and creates a list of files to be compared.
     * @param folderPath Folder path that has the OOXML content of the file in JSON format
     * @param roundtripped Whether the file is roundTripped file or not.
     */
    public XlsxFile(String folderPath, boolean roundtripped) {
        super(folderPath, roundtripped);
        loadFromPath();
        filesToCompare = new ArrayList<>();
        for (int i = 1; ; i++) {
            String xmlFileName = "xl_worksheets_sheet" + i + ".xml.json";
            if (jsonDataFiles.containsKey(xmlFileName)) {
                filesToCompare.add(xmlFileName);
            } else {
                break;
            }
        }

        commentToCompare = new ArrayList<>();
        for(int i = 1; ; i++){
            String xmlFileName = "xl_comments" + i + ".xml.json";
            if (jsonDataFiles.containsKey(xmlFileName)) {
                commentToCompare.add(xmlFileName);
            } else {
                break;
            }
        }
    }

    /**
     * This load the contents of the shared strings from the xl_sharedStrings.xml in OOXML.
     */
    public void loadSharedStrings(){
        if(jsonDataFiles.containsKey("xl_sharedStrings.xml.json")) {
            ArrayList<String> tags = new ArrayList<>();
            tags.add("si");
            ArrayList<JSONObject> siTagContent = JsonUtility.extractTag(getJson("xl_sharedStrings.xml.json"), tags);

            if(siTagContent.isEmpty()){
                return;
            }

            sharedString = new ArrayList<>();
            if(siTagContent.get(0).get("si") instanceof JSONObject){
                sharedString.add(JsonUtility.getTextContent((JSONObject)siTagContent.get(0).get("si"),null,true));
            }else{
                JSONArray shared_elems = (JSONArray) siTagContent.get(0).get("si");
                for (Object data : shared_elems) {
                    sharedString.add(JsonUtility.getTextContent((JSONObject) data,null,true));
                }
            }
        }
    }

    /**
     * @return the JSON for the files to be compared by adding the Sheet number in the context
     */
    public ArrayList<JSONObject> getTextJson() {
        ArrayList<JSONObject> allJson = new ArrayList<>();
        int counter = 0;
        for (String files : filesToCompare) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("file",String.valueOf(counter));
            counter++;
            jsonObject.put("sheet_data",getJson(files));
            allJson.add((jsonObject));
        }
        return allJson;
    }

    /**
     * @return the JSON for the files to be compared for Comments Context with Sheet number added
     */
    public ArrayList<JSONObject> getCommentJson(){
        ArrayList<JSONObject> allJson = new ArrayList<>();
        int counter = 0;
        for (String files : commentToCompare) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("file",String.valueOf(counter));
            counter++;
            jsonObject.put("sheet_data",getJson(files));
            allJson.add((jsonObject));
        }
        return allJson;
    }
    ArrayList<ArrayList<String>> allTextTag;

    private void ExtractCellContent(JSONObject file, JSONObject val){
        if (val.containsKey("@t") && val.get("@t").equals("s")) {
            if(!val.containsKey("v")) {
                return;
            }
            ArrayList<String> cellValue = sharedString.get(Integer.parseInt((String) val.get("v")));
            if(val.containsKey("@r")){
                cellValue.add(0, file.get("file").toString() + val.get("@r"));
                allTextTag.add(cellValue);
            }else{
                StatusLogger.addRecordWarningExec("This Element contains @t=s and no @r, looking it!!");
                StatusLogger.addRecordWarningExec(cellValue.toString());
            }
        }
        else if(val.containsKey("@t") && val.get("@t").equals("str")){
            ArrayList<String> cellValue = new ArrayList<>();
            if(val.containsKey("@r")){
                cellValue.add(file.get("file").toString() +val.get("@r"));
            }else{
                StatusLogger.addRecordWarningExec("This Element contains @t=str and no @r, looking it!!");
                StatusLogger.addRecordWarningExec(cellValue.toString());
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
            ArrayList<String> cellValue = new ArrayList<>();
            if (val.containsKey("@r")) {
                cellValue.add(file.get("file").toString() +val.get("@r"));
            }else{
                StatusLogger.addRecordWarningExec("This Element no @r, looking it!!");
                StatusLogger.addRecordWarningExec(cellValue.toString());
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

    /**
     * The functions implement the total running of tag extraction for text (c tag) and then in the subtree, finding for strings to be matched
     * @return List of List, where each of them contains the total data to be compared for each 'c' tag
     */
    public ArrayList<ArrayList<String>> GetTextContent() {
        allTextTag = new ArrayList<>();

        for (JSONObject file : getTextJson()) {
            ArrayList<String> tags = new ArrayList<>();
            tags.add("sheetData");
            ArrayList<JSONObject> sheetData = JsonUtility.extractTag(file,tags);
            tags = new ArrayList<>();
            tags.add("c");

            // if there is no sheet data then continue with next sheet
            if(sheetData.isEmpty())continue;
            ArrayList<JSONObject> cTagOccurence = JsonUtility.extractTag(sheetData.get(0), tags);

            for (JSONObject cTagObj : cTagOccurence) {
                if(cTagObj.get("c") instanceof JSONObject){
                    JSONObject cTagObject = (JSONObject) cTagObj.get("c");
                    ExtractCellContent(file,cTagObject);
                }
                else {
                    JSONArray cTagsArray = (JSONArray) cTagObj.get("c");
                    for (Object object : cTagsArray) {
                        JSONObject val = (JSONObject) object;
                        ExtractCellContent(file, val);
                    }
                }
            }
        }
        return allTextTag;
    }
    ArrayList<ArrayList<String>> allCommentTag;

    private void extractCommentTag(JSONObject wCommentTagObj, JSONObject file){
        StatusLogger.addRecordInfoDebug(wCommentTagObj.toString());
        ArrayList<String> allStringCommentContent = JsonUtility.getCommentContentXlsx(wCommentTagObj, (String) file.get("file"));
        allCommentTag.add(allStringCommentContent);
    }

    /**
     * The functions implement the total running of tag extraction for text (comment tag) and then in the subtree, finding for strings to be matched
     * @return List of List, where each of them contains the total data to be compared for each 'comment' tag
     */
    public ArrayList<ArrayList<String>> getCommentContent(){
        allCommentTag = new ArrayList<>();

        for (JSONObject file : getCommentJson()) {
            ArrayList<String> tags = new ArrayList<>();
            tags.add("comment");

            StatusLogger.addRecordInfoDebug("Before wcommentExtract");
            StatusLogger.addRecordInfoDebug(JsonUtility.extractTag(file, tags).toString());

            JSONObject commentTag = JsonUtility.extractTag(file, tags).get(0);
            StatusLogger.addRecordInfoDebug("After wcommentExtract");

            if(commentTag.get("comment") instanceof JSONObject){
                extractCommentTag((JSONObject) commentTag.get("comment"),file);
            }else{
                JSONArray allCommentTagJson = (JSONArray) commentTag.get("comment");
                for (Object commentTagObj : allCommentTagJson){
                    extractCommentTag((JSONObject) commentTagObj,file);
                }
            }

        }
        return allCommentTag;
    }

}
