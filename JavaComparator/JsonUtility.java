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
                System.out.println();
                if(value!=null)
                    recurse_visit((JSONObject) value, tags, ignoreAttrib, debug);
            } else if (value instanceof JSONArray) {
                JSONArray subtags = (JSONArray) value;
                for (Object subtag : subtags) {
                    System.out.println();
                    if(subtag!=null)
                    recurse_visit((JSONObject) subtag, tags, ignoreAttrib, debug);
                }
            }
        }
    }

    public static ArrayList<JSONObject> extract_tag(JSONObject jsonData, ArrayList<String> tags) {
        buffer = new ArrayList<JSONObject>();
        recurse_visit(jsonData, tags, true, true);
        return buffer;
    }

    public static void recurse_text(JSONObject jsonData) {

        for (Object key : jsonData.keySet()) {

            Object value = jsonData.get(key);// do something with jsonObject here

            String key_val = (String) key;
            if (key_val.charAt(0) == '@') {
                continue;
            }

            System.out.println("KEY : " + key + ", VALUE : " + value);

            if (value instanceof JSONObject) {
                System.out.println();
                if(value!=null) {
                    recurse_text((JSONObject) value);
                }
            } else if (value instanceof JSONArray) {
                JSONArray subtags = (JSONArray) value;
                for (Object subtag : subtags) {
                    System.out.println();
                    if(subtag!=null) {
                        recurse_text((JSONObject) subtag);
                    }
                }
            } else {
                System.out.println(key_val);
                if (value!=null) {
                    System.out.println();
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
}
