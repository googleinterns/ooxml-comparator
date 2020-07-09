import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;


/**
 * XlsxFile Class implments the methods required for comparasions of XLSX files
 */
public class XlsxFile extends OoxmlFile {

    public static final String XLSX_SHEET_TEXT_FILE_NAME_PREFIX = "xl_worksheets_sheet";
    public static final String XLSX_SHEET_COMMENT_FILE_NAME_PREFIX = "xl_comments";
    public static final String XLSX_SHEET_FILE_NAME_SUFFIX = ".xml.json";

    public static final String XLSX_SHARED_STRING_FILE_NAME = "xl_sharedStrings.xml.json";
    public static final String XLSX_SHARED_STRING_TABLE_ENTRY_TAG_NAME = "si";
    public static final String XLSX_CUSTOM_JSON_FILE_NAME_TAG = "file";
    public static final String XLSX_CUSTOM_JSON_FILE_CONTENT_TAG = "sheet_data";

    public static final String XLSX_SHEET_CELL_VALUE_TAG = "v";
    public static final String XLSX_SHEET_CELL_FORMULA_TAG = "f";
    public static final String XLSX_SHEET_CELL_ROW_COL_UNIQUE_ID_ATTRIBUTE = "@r";
    public static final String XLSX_SHEET_CELL_TYPE_IDENTIFIER_ATTRIBUTE = "@t";
    public static final String XLSX_SHEET_CELL_TYPE_IDENTIFIER_SHARED_STRING_TYPE_VALUE = "s";
    public static final String XLSX_SHEET_CELL_TYPE_IDENTIFIER_MERGED_TYPE_VALUE = "str";
    public static final String XLSX_SHEET_CELL_TEXT_VALUE = "#text";
    public static final String XLSX_SHEET_CELL_REFERENCE_ID = "@ref";

    public static final String XLSX_SHEET_DATA_TAG_NAME = "sheetData";
    public static final String XLSX_TAG_TO_COMPARE_TEXT = "c";
    public static final String XLSX_TAG_TO_COMPARE_COMMENT = "comment";


    public ArrayList<String> filesToCompare,commentToCompare;
    public ArrayList<ArrayList<String>> sharedString;
    public ArrayList<ArrayList<String>> allTextTag;
    ArrayList<ArrayList<String>> allCommentTag;

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
            String xmlFileName = XLSX_SHEET_TEXT_FILE_NAME_PREFIX + i + XLSX_SHEET_FILE_NAME_SUFFIX;
            if (jsonDataFiles.containsKey(xmlFileName)) {
                filesToCompare.add(xmlFileName);
            } else {
                break;
            }
        }

        commentToCompare = new ArrayList<>();
        for(int i = 1; ; i++){
            String xmlFileName = XLSX_SHEET_COMMENT_FILE_NAME_PREFIX + i + XLSX_SHEET_FILE_NAME_SUFFIX;
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
        if(jsonDataFiles.containsKey(XLSX_SHARED_STRING_FILE_NAME)) {
            ArrayList<String> tags = new ArrayList<>();
            tags.add(XLSX_SHARED_STRING_TABLE_ENTRY_TAG_NAME);
            ArrayList<JSONObject> siTagContent = JsonUtility.extractTag(getJson(XLSX_SHARED_STRING_FILE_NAME), tags);

            if(siTagContent.isEmpty()){
                return;
            }

            sharedString = new ArrayList<>();
            if(siTagContent.get(0).get(XLSX_SHARED_STRING_TABLE_ENTRY_TAG_NAME) instanceof JSONObject){
                sharedString.add(JsonUtility.getTextContent((JSONObject)siTagContent.get(0).get(XLSX_SHARED_STRING_TABLE_ENTRY_TAG_NAME),null,true));
            }else{
                JSONArray shared_elems = (JSONArray) siTagContent.get(0).get(XLSX_SHARED_STRING_TABLE_ENTRY_TAG_NAME);
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
            jsonObject.put(XLSX_CUSTOM_JSON_FILE_NAME_TAG,String.valueOf(counter));
            counter++;
            jsonObject.put(XLSX_CUSTOM_JSON_FILE_CONTENT_TAG,getJson(files));
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
            jsonObject.put(XLSX_CUSTOM_JSON_FILE_NAME_TAG,String.valueOf(counter));
            counter++;
            jsonObject.put(XLSX_CUSTOM_JSON_FILE_CONTENT_TAG,getJson(files));
            allJson.add((jsonObject));
        }
        return allJson;
    }


    /**
     * For each of the Cell content found that contains Shared String, the function finds the relevant contents to match them.
     * @param fileJsonData The Json Data of the File being compared.
     * @param valueJsonToExtractData The Subtree found as the Match for the particular tag.
     */
    private ArrayList<String> extractCellContentSharedString(JSONObject fileJsonData,JSONObject valueJsonToExtractData){
        ArrayList<String> cellValue = sharedString.get(Integer.parseInt((String) valueJsonToExtractData.get(XLSX_SHEET_CELL_VALUE_TAG)));
        if(valueJsonToExtractData.containsKey(XLSX_SHEET_CELL_ROW_COL_UNIQUE_ID_ATTRIBUTE)){
            cellValue.add(0, fileJsonData.get(XLSX_CUSTOM_JSON_FILE_NAME_TAG).toString() + valueJsonToExtractData.get(XLSX_SHEET_CELL_ROW_COL_UNIQUE_ID_ATTRIBUTE));
            allTextTag.add(cellValue);
        }else{
            StatusLogger.addRecordWarningExec("This Element contains @t=s and no @r, looking it!!");
            StatusLogger.addRecordWarningExec(cellValue.toString());
        }
        return cellValue;
    }

    /**
     * For each of the Cell content found that is a part of Merged Cell, the function finds the relevant contents to match them.
     * @param fileJsonData The Json Data of the File being compared.
     * @param valueJsonToExtractData The Subtree found as the Match for the particular tag.
     */
    private ArrayList<String> extractCellContentMergedCellContent(JSONObject fileJsonData,JSONObject valueJsonToExtractData){
        ArrayList<String> cellValue = new ArrayList<>();
        if(valueJsonToExtractData.containsKey(XLSX_SHEET_CELL_ROW_COL_UNIQUE_ID_ATTRIBUTE)){
            cellValue.add(fileJsonData.get(XLSX_CUSTOM_JSON_FILE_NAME_TAG).toString()+valueJsonToExtractData.get(XLSX_SHEET_CELL_ROW_COL_UNIQUE_ID_ATTRIBUTE));
        }else{
            StatusLogger.addRecordWarningExec("This Element contains @t=str and no @r, looking it!!");
            StatusLogger.addRecordWarningExec(cellValue.toString());
        }
        if (valueJsonToExtractData.containsKey(XLSX_SHEET_CELL_FORMULA_TAG)) {
            if(valueJsonToExtractData.get(XLSX_SHEET_CELL_FORMULA_TAG) instanceof JSONObject){
                JSONObject fval = (JSONObject) valueJsonToExtractData.get(XLSX_SHEET_CELL_FORMULA_TAG);
                if(fval.containsKey(XLSX_SHEET_CELL_TEXT_VALUE))cellValue.add((String)fval.get(XLSX_SHEET_CELL_TEXT_VALUE));
                if(fval.containsKey(XLSX_SHEET_CELL_TYPE_IDENTIFIER_ATTRIBUTE)){
                    cellValue.add((String)fval.get(XLSX_SHEET_CELL_TYPE_IDENTIFIER_ATTRIBUTE));
                }
                if(fval.containsKey(XLSX_SHEET_CELL_REFERENCE_ID))cellValue.add((String)fval.get(XLSX_SHEET_CELL_REFERENCE_ID));
            }else {
                cellValue.add((String) valueJsonToExtractData.get(XLSX_SHEET_CELL_FORMULA_TAG));
            }
        }
        return cellValue;
    }

    /**
     * For each of the Cell content found that is Default type(not the above 2 case), the function finds the relevant contents to match them.
     * @param fileJsonData The Json Data of the File being compared.
     * @param valueJsonToExtractData The Subtree found as the Match for the particular tag.
     */
    private ArrayList<String> extractCellContentDefaultCell(JSONObject fileJsonData,JSONObject valueJsonToExtractData){
        ArrayList<String> cellValue = new ArrayList<>();
        if (valueJsonToExtractData.containsKey(XLSX_SHEET_CELL_ROW_COL_UNIQUE_ID_ATTRIBUTE)) {
            cellValue.add(fileJsonData.get(XLSX_CUSTOM_JSON_FILE_NAME_TAG).toString() +valueJsonToExtractData.get(XLSX_SHEET_CELL_ROW_COL_UNIQUE_ID_ATTRIBUTE));
        }else{
            StatusLogger.addRecordWarningExec("This Element no @r, looking it!!");
            StatusLogger.addRecordWarningExec(cellValue.toString());
        }
        if (valueJsonToExtractData.containsKey(XLSX_SHEET_CELL_VALUE_TAG)) {
            cellValue.add((String) valueJsonToExtractData.get(XLSX_SHEET_CELL_VALUE_TAG));
            //NOTE: if we want to prune out empty cells, we can do it here!!
        }
        if (valueJsonToExtractData.containsKey(XLSX_SHEET_CELL_FORMULA_TAG)) {
            if(valueJsonToExtractData.get(XLSX_SHEET_CELL_FORMULA_TAG) instanceof JSONObject){
                JSONObject fval = (JSONObject) valueJsonToExtractData.get(XLSX_SHEET_CELL_FORMULA_TAG);
                if(fval.containsKey(XLSX_SHEET_CELL_TEXT_VALUE))cellValue.add((String)fval.get(XLSX_SHEET_CELL_TEXT_VALUE));
                if(fval.containsKey(XLSX_SHEET_CELL_TYPE_IDENTIFIER_ATTRIBUTE)){
                    cellValue.add((String)fval.get(XLSX_SHEET_CELL_TYPE_IDENTIFIER_ATTRIBUTE));
                }
                if(fval.containsKey(XLSX_SHEET_CELL_REFERENCE_ID))cellValue.add((String)fval.get(XLSX_SHEET_CELL_REFERENCE_ID));
            }else {
                cellValue.add((String) valueJsonToExtractData.get(XLSX_SHEET_CELL_FORMULA_TAG));
            }
        }
        return cellValue;
    }

    /**
     * For each of the Cell content found, the function finds the relevant contents to match them.
     * @param fileJsonData The Json Data of the File being compared.
     * @param valueJsonToExtractData The Subtree found as the Match for the particular tag.
     */
    private void extractCellContent(JSONObject fileJsonData, JSONObject valueJsonToExtractData){
        if (valueJsonToExtractData.containsKey(XLSX_SHEET_CELL_TYPE_IDENTIFIER_ATTRIBUTE) && valueJsonToExtractData.get(XLSX_SHEET_CELL_TYPE_IDENTIFIER_ATTRIBUTE).equals(XLSX_SHEET_CELL_TYPE_IDENTIFIER_SHARED_STRING_TYPE_VALUE)) {
            if(!valueJsonToExtractData.containsKey(XLSX_SHEET_CELL_VALUE_TAG)) {
                return;
            }
            ArrayList<String> cellValue = extractCellContentSharedString(fileJsonData,valueJsonToExtractData);
            allTextTag.add(cellValue);
        }
        else if(valueJsonToExtractData.containsKey(XLSX_SHEET_CELL_TYPE_IDENTIFIER_ATTRIBUTE) && valueJsonToExtractData.get(XLSX_SHEET_CELL_TYPE_IDENTIFIER_ATTRIBUTE).equals(XLSX_SHEET_CELL_TYPE_IDENTIFIER_MERGED_TYPE_VALUE)){
            ArrayList<String> cellValue = extractCellContentMergedCellContent(fileJsonData,valueJsonToExtractData);
            allTextTag.add(cellValue);
        }
        else {
            ArrayList<String> cellValue = extractCellContentDefaultCell(fileJsonData,valueJsonToExtractData);
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
            tags.add(XLSX_SHEET_DATA_TAG_NAME);
            ArrayList<JSONObject> sheetData = JsonUtility.extractTag(file,tags);
            tags = new ArrayList<>();
            tags.add(XLSX_TAG_TO_COMPARE_TEXT);

            // if there is no sheet data then continue with next sheet
            if(sheetData.isEmpty())continue;
            ArrayList<JSONObject> cTagOccurence = JsonUtility.extractTag(sheetData.get(0), tags);

            for (JSONObject cTagObj : cTagOccurence) {
                if(cTagObj.get(XLSX_TAG_TO_COMPARE_TEXT) instanceof JSONObject){
                    JSONObject cTagObject = (JSONObject) cTagObj.get(XLSX_TAG_TO_COMPARE_TEXT);
                    extractCellContent(file,cTagObject);
                }
                else {
                    JSONArray cTagsArray = (JSONArray) cTagObj.get(XLSX_TAG_TO_COMPARE_TEXT);
                    for (Object object : cTagsArray) {
                        JSONObject val = (JSONObject) object;
                        extractCellContent(file, val);
                    }
                }
            }
        }
        return allTextTag;
    }

    private void extractCommentTag(JSONObject wCommentTagObj, JSONObject file){
        StatusLogger.addRecordInfoDebug(wCommentTagObj.toString());
        ArrayList<String> allStringCommentContent = JsonUtility.getCommentContentXlsx(wCommentTagObj, (String) file.get(XLSX_CUSTOM_JSON_FILE_NAME_TAG));
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
            tags.add(XLSX_TAG_TO_COMPARE_COMMENT);

            StatusLogger.addRecordInfoDebug("Before wcommentExtract");
            StatusLogger.addRecordInfoDebug(JsonUtility.extractTag(file, tags).toString());

            JSONObject commentTag = JsonUtility.extractTag(file, tags).get(0);
            StatusLogger.addRecordInfoDebug("After wcommentExtract");

            if(commentTag.get(XLSX_TAG_TO_COMPARE_COMMENT) instanceof JSONObject){
                extractCommentTag((JSONObject) commentTag.get(XLSX_TAG_TO_COMPARE_COMMENT),file);
            }else{
                JSONArray allCommentTagJson = (JSONArray) commentTag.get(XLSX_TAG_TO_COMPARE_COMMENT);
                for (Object commentTagObj : allCommentTagJson){
                    extractCommentTag((JSONObject) commentTagObj,file);
                }
            }

        }
        return allCommentTag;
    }

}
