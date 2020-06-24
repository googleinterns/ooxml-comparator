import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class JsonUtility {

    public static ArrayList<JSONObject> buffer;
    public static ArrayList<String> stringOrder;

    JsonUtility(){

    }

    public static void recurse_visit(JSONObject jsonData, ArrayList<String> tags, Boolean ignoreAttrib, Boolean debug) {
        if(jsonData==null)
            return;
        for (Object key : jsonData.keySet()) {

            Object value = jsonData.get(key); // do something with jsonObject here

            String key_val = (String) key;

            if (ignoreAttrib && (key_val.charAt(0) == '@')) {
                continue;
            }

            if (tags.contains(key_val)) {
                JSONObject json = new JSONObject();
                json.put(key_val,value);
                buffer.add(json);
            }

            if (debug) {
                System.out.println("KEY : " + key + ", VALUE : " + value);
            }

            if (value instanceof JSONObject) {
                if(value!=null)
                    recurse_visit((JSONObject) value, tags, ignoreAttrib, debug);
            } else if (value instanceof JSONArray) {
                JSONArray subtags = (JSONArray) value;
                for (Object subtag : subtags) {
                    if(subtag!=null)
                    recurse_visit((JSONObject) subtag, tags, ignoreAttrib, debug);
                }
            }
        }
    }

    public static ArrayList<JSONObject> extract_tag(JSONObject jsonData, ArrayList<String> tags) {
        buffer = new ArrayList<JSONObject>();
        recurse_visit(jsonData, tags, true, false);
        return buffer;
    }

    public static void recurse_text(JSONObject jsonData) {

        for (Object key : jsonData.keySet()) {
            Object value = jsonData.get(key);// do something with jsonObject here
            String key_val = (String) key;
            if (key_val.charAt(0) == '@') {
                continue;
            }

           //System.out.println("KEY : " + key + ", VALUE : " + value);

            if (value instanceof JSONObject) {
                if(value!=null) {
                    recurse_text((JSONObject) value);
                }
            } else if (value instanceof JSONArray) {
                if(value!=null) {
                    JSONArray subtags = (JSONArray) value;
                    for (Object subtag : subtags) {
                        if (subtag != null) {
                            recurse_text((JSONObject) subtag);
                        }
                    }
                }
            } else {
                System.out.println(key_val);
                if (value!=null) {
                    stringOrder.add((String) value);
                }
            }
        }
    }

    public static ArrayList<String> getTextContent(JSONObject jsonObject) {
        stringOrder = new ArrayList<String>();
        recurse_text(jsonObject);
        return stringOrder;
    }

    public static ArrayList<String> getCommentContentDocx(JSONObject wCommentTagObj) {
        stringOrder = new ArrayList<String>();
        recurse_text(wCommentTagObj);
        stringOrder.add(0, (String) wCommentTagObj.get("@w:id"));
        stringOrder.add(1, (String) wCommentTagObj.get("@w:author"));
        stringOrder.add(2, (String) wCommentTagObj.get("@w:date"));
        return stringOrder;
    }

    public static ArrayList<String> getCommentContentPptx(JSONObject wCommentTagObj) {
        stringOrder = new ArrayList<String>();
        recurse_text(wCommentTagObj);
        stringOrder.add(0, (String) wCommentTagObj.get("@idx"));
        stringOrder.add(1, (String) wCommentTagObj.get("@authorId"));
        stringOrder.add(2, (String) wCommentTagObj.get("@dt"));
        //stringOrder.add(3, (String) wCommentTagObj.get("p:pos"));// TODO: Really optional
        return stringOrder;
    }

    public static ArrayList<String> getCommentContentXlsx(JSONObject wCommentTagObj,String file) {
        stringOrder = new ArrayList<String>();
        recurse_text(wCommentTagObj);
        stringOrder.add(0, file+(String) wCommentTagObj.get("@ref"));
        return stringOrder;
    }
}
