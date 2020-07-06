import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

/**
 * JSONUtility implements fucntions to help Recursing into JSON Tree and extract relevent tags and information.
 */
public class JsonUtility {

    public static ArrayList<JSONObject> buffer;
    public static ArrayList<String> stringOrder;

    /**
     * Recusion to extract the Tags that needs to be processed.
     *
     * @param jsonDataSubtree     JSONData of the whole Tree
     * @param tagsToSearch         Tags that need to be searched for in the Tree
     * @param ignoreAttrib Whether we want to visit the Attributes too or not.
     */
    private static void recurseVisit(JSONObject jsonDataSubtree, ArrayList<String> tagsToSearch, Boolean ignoreAttrib) {
        if (jsonDataSubtree == null) {
            return;
        }
        //StatusLogger.AddRecordInfoDebug(jsonData.toString());
        for (Object key : jsonDataSubtree.keySet()) {

            Object value = jsonDataSubtree.get(key); // do something with jsonObject here
            String keyVal = (String) key;

            if (ignoreAttrib && (keyVal.charAt(0) == '@')) {
                // if its a attibute
                continue;
            }

            if (tagsToSearch.contains(keyVal)) {
                JSONObject json = new JSONObject();
                json.put(keyVal, value);
                buffer.add(json);
            }

            if (value instanceof JSONObject) {
                recurseVisit((JSONObject) value, tagsToSearch, ignoreAttrib);
            } else if (value instanceof JSONArray) {
                JSONArray subtags = (JSONArray) value;
                for (Object subtag : subtags) {
                    if (subtag instanceof JSONObject)
                        recurseVisit((JSONObject) subtag, tagsToSearch, ignoreAttrib);
                }
            }
        }
    }

    /**
     * To search for tags in the JSON Tree
     *
     * @param jsonData The Tree JSON data
     * @param tags     List of tags to be searched for
     * @return First level occurence of each of the tags
     */
    public static ArrayList<JSONObject> extractTag(JSONObject jsonData, ArrayList<String> tags) {
        buffer = new ArrayList<>();
        recurseVisit(jsonData, tags, true);
        return buffer;
    }

    private static void recurseText(JSONObject jsonData, ArrayList<String> textTags, boolean seenTag) {
        for (Object key : jsonData.keySet()) {
            Object value = jsonData.get(key);// do something with jsonObject here
            String keyVal = (String) key;
            if (keyVal.charAt(0) == '@') {
                continue;
            }

            boolean seenTagInParent = seenTag;
            if (textTags != null && textTags.contains(keyVal)) {
                seenTagInParent = true;
            }

            if (value instanceof JSONObject) {
                recurseText((JSONObject) value, textTags, seenTagInParent);
            } else if (value instanceof JSONArray) {
                JSONArray subtags = (JSONArray) value;
                for (Object subtag : subtags) {
                    if (subtag != null) {
                        recurseText((JSONObject) subtag, textTags, seenTagInParent);
                    }
                }
            } else {
                if (value != null && seenTagInParent) {
                    stringOrder.add((String) value);
                }
            }
        }
    }

    /**
     * Functon to get the Text or String Literals out of the JSON tree
     *
     * @param jsonObject       Json object for the Tree to be searched in
     * @param textTags         Tags that contains that
     * @param noTagMatchNeeded Whether we need to match a tag before taking that particular string
     * @return List of string literals in the subtree
     */
    public static ArrayList<String> getTextContent(JSONObject jsonObject, ArrayList<String> textTags, boolean noTagMatchNeeded) {
        stringOrder = new ArrayList<>();
        recurseText(jsonObject, textTags, noTagMatchNeeded);
        return stringOrder;
    }

    /**
     * Function to find the Comment Strings in Docx
     *
     * @param wCommentTagObj Comment JSON object
     * @return List of Strings of comment in the JSON tree
     */
    public static ArrayList<String> getCommentContentDocx(JSONObject wCommentTagObj) {
        stringOrder = new ArrayList<>();
        recurseText(wCommentTagObj, null, true);
        stringOrder.add(0, (String) wCommentTagObj.get("@w:id"));
        stringOrder.add(1, (String) wCommentTagObj.get("@w:author"));
        stringOrder.add(2, (String) wCommentTagObj.get("@w:date"));
        return stringOrder;
    }

    /**
     * Function to find the Comment Strings in Pptx
     *
     * @param wCommentTagObj Comment JSON object
     * @return List of Strings of comment in the JSON tree
     */
    public static ArrayList<String> getCommentContentPptx(JSONObject wCommentTagObj) {
        stringOrder = new ArrayList<>();
        recurseText(wCommentTagObj, null, true);
        stringOrder.add(0, (String) wCommentTagObj.get("@idx"));
        stringOrder.add(1, (String) wCommentTagObj.get("@authorId"));
        stringOrder.add(2, (String) wCommentTagObj.get("@dt"));
        return stringOrder;
    }

    /**
     * Function to find the Comment Strings in Xlsx
     *
     * @param wCommentTagObj Comment JSON object
     * @return List of Strings of comment in the JSON tree
     */
    public static ArrayList<String> getCommentContentXlsx(JSONObject wCommentTagObj, String file) {
        stringOrder = new ArrayList<>();
        recurseText(wCommentTagObj, null, true);
        stringOrder.add(0, file + wCommentTagObj.get("@ref"));
        return stringOrder;
    }
}
