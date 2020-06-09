import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.TreeMap;


public class OoxmlFile {

    String data_path;
    boolean roundtripped = false;

    Map<String, JSONObject> jsonFiles;

    public OoxmlFile(String folderPath, boolean roundtripped) {
        this.data_path = folderPath;
        this.roundtripped = roundtripped;
        jsonFiles = new TreeMap<String, JSONObject>();
    }

    public void loadFromPath() {
        File folder = new File(data_path);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String fileName = listOfFiles[i].getName();
                String basename = FilenameUtils.getBaseName(fileName);
                System.out.println(fileName);
                System.out.println(basename);

                JSONParser parser = new JSONParser();
                try {
                    Object obj = parser.parse(new FileReader(listOfFiles[i]));
                    // A JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.
                    JSONObject jsonObject = (JSONObject) obj;
                    // System.out.println(jsonObject);

                    this.jsonFiles.put(fileName, jsonObject);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public JSONObject getJson(String fileName) {
        return jsonFiles.get(fileName);
    }

    public void printAllJsons() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        for (Map.Entry<String, JSONObject> entry : this.jsonFiles.entrySet()) {
            System.out.println("Key = " + entry.getKey());
            String prettyJsonString = gson.toJson(entry.getValue());
            System.out.println(prettyJsonString);
        }
    }

    public void printJsons(String fileName) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJsonString = gson.toJson(jsonFiles.get(fileName));
        System.out.println(fileName);
        System.out.println(prettyJsonString);
    }

}
