import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

/**
 * JSONUtility implements fucntions to help Recursing into JSON Tree and extract relevent tags and information.
 */
public class JsonUtility {

    public static final char ATTRIBUTE_TAG_START_CHARACTER = '@';
    public static final boolean DEFAULT_STATE_IGNORE_ATTRIBUTE = true;
    public static final boolean DEFAULT_STATE_SEEN_ANCESTOR_ATTRIBUTE = true;

    public static final String DOCX_COMMENT_ATTRIBUTE_ID = "@w:id";
    public static final String DOCX_COMMENT_ATTRIBUTE_AUTHOR = "@w:author";
    public static final String DOCX_COMMENT_ATTRIBUTE_DATE = "@w:date";

    public static final String PPTX_COMMENT_ATTRIBUTE_ID = "@idx";
    public static final String PPTX_COMMENT_ATTRIBUTE_AUTHOR = "@authorId";
    public static final String PPTX_COMMENT_ATTRIBUTE_DATE = "@dt";

    public static final String XLSX_COMMENT_ATTRIBUTE_REF_ID = "@ref";

    public static ArrayList<JSONObject> buffer;
    public static ArrayList<String> stringOrder;

    /**
     * Recusion to extract the Tags that needs to be processed.
     *
     * @param jsonDataSubtree     JSONData of the whole Tree
     * @param tagsToSearch         Tags that need to be searched for in the Tree
     * @param ignoreAttribute Whether we want to visit the Attributes too or not.
     */
    private static void recurseVisit(JSONObject jsonDataSubtree, ArrayList<String> tagsToSearch, Boolean ignoreAttribute) {
        if (jsonDataSubtree == null) {
            return;
        }

        for (Object tagName : jsonDataSubtree.keySet()) {
            Object jsonValue = jsonDataSubtree.get(tagName);
            String tagNameValue = (String) tagName;

            if (ignoreAttribute && (tagNameValue.charAt(0) == ATTRIBUTE_TAG_START_CHARACTER)) {
                // If the node in the recursion is a Attribute.
                continue;
            }

            if (tagsToSearch.contains(tagNameValue)) {
                JSONObject matchedJsonTree = new JSONObject();
                matchedJsonTree.put(tagNameValue, jsonValue);
                buffer.add(matchedJsonTree);
            }

            if (jsonValue instanceof JSONObject) {
                recurseVisit((JSONObject) jsonValue, tagsToSearch, ignoreAttribute);
            } else if (jsonValue instanceof JSONArray) {
                JSONArray childSubtreeTagName = (JSONArray) jsonValue;
                for (Object subtreeTag : childSubtreeTagName) {
                    if (subtreeTag instanceof JSONObject)
                        recurseVisit((JSONObject) subtreeTag, tagsToSearch, ignoreAttribute);
                }
            }
        }
    }

    /**
     * To search for tags in the JSON Tree
     *
     * @param jsonData The Tree JSON data.
     * @param tags     List of tags to be searched for.
     * @return First level occurence of each of the tags.
     */
    public static ArrayList<JSONObject> extractTag(JSONObject jsonData, ArrayList<String> tags) {
        buffer = new ArrayList<>();
        recurseVisit(jsonData, tags, DEFAULT_STATE_IGNORE_ATTRIBUTE);
        return buffer;
    }

    /**
     * Recurse on subtree to find the lower level Content text to be used for comparing the tags.
     * @param jsonData Subtree Json to be searched in.
     * @param textTags tags that marks the start of some subtree that should be compared.
     * @param seenTag whether this subree is Already inside a textTag marked node.
     */
    private static void recurseText(JSONObject jsonData, ArrayList<String> textTags, boolean seenTag) {
        for (Object tagName : jsonData.keySet()) {
            Object jsonValue = jsonData.get(tagName); // do something with jsonObject here
            String tagNameValue = (String) tagName;

            if (tagNameValue.charAt(0) == ATTRIBUTE_TAG_START_CHARACTER) {
                continue;
            }

            boolean seenTagInParent = seenTag;
            if (textTags != null && textTags.contains(tagNameValue)) {
                seenTagInParent = DEFAULT_STATE_SEEN_ANCESTOR_ATTRIBUTE;
            }

            if (jsonValue instanceof JSONObject) {
                recurseText((JSONObject) jsonValue, textTags, seenTagInParent);
            } else if (jsonValue instanceof JSONArray) {
                JSONArray childSubtreeTagName = (JSONArray) jsonValue;
                for (Object subtreeTag : childSubtreeTagName) {
                    if (subtreeTag != null) {
                        recurseText((JSONObject) subtreeTag, textTags, seenTagInParent);
                    }
                }
            } else {
                if (jsonValue != null && seenTagInParent) {
                    stringOrder.add((String) jsonValue);
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
        recurseText(wCommentTagObj, null, DEFAULT_STATE_SEEN_ANCESTOR_ATTRIBUTE);
        stringOrder.add(0, (String) wCommentTagObj.get(DOCX_COMMENT_ATTRIBUTE_ID));
        stringOrder.add(1, (String) wCommentTagObj.get(DOCX_COMMENT_ATTRIBUTE_AUTHOR));
        stringOrder.add(2, (String) wCommentTagObj.get(DOCX_COMMENT_ATTRIBUTE_DATE));
        return stringOrder;
    }

    /**
     * Function to find the Comment Strings in Pptx
     *
     * @param commentTagObj Comment JSON object
     * @return List of Strings of comment in the JSON tree
     */
    public static ArrayList<String> getCommentContentPptx(JSONObject commentTagObj) {
        stringOrder = new ArrayList<>();
        recurseText(commentTagObj, null, DEFAULT_STATE_SEEN_ANCESTOR_ATTRIBUTE);
        stringOrder.add(0, (String) commentTagObj.get(PPTX_COMMENT_ATTRIBUTE_ID));
        stringOrder.add(1, (String) commentTagObj.get(PPTX_COMMENT_ATTRIBUTE_AUTHOR));
        stringOrder.add(2, (String) commentTagObj.get(PPTX_COMMENT_ATTRIBUTE_DATE));
        return stringOrder;
    }

    /**
     * Function to find the Comment Strings in Xlsx
     * @param commentTagObject Comment JSON object
     * @return List of Strings of comment in the JSON tree
     */
    public static ArrayList<String> getCommentContentXlsx(JSONObject commentTagObject, String fileUniqueId) {
        stringOrder = new ArrayList<>();
        recurseText(commentTagObject, null, DEFAULT_STATE_SEEN_ANCESTOR_ATTRIBUTE);
        stringOrder.add(0, fileUniqueId + commentTagObject.get(XLSX_COMMENT_ATTRIBUTE_REF_ID));
        return stringOrder;
    }
}
