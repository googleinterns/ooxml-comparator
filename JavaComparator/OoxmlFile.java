import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;


/**
 * OoxmlFile Class implement various file loading methods to load JSON files into the Objects
 */
public class OoxmlFile {
    String data_path;
    boolean roundtripped;

    Map<String, JSONObject> jsonFiles;
    /**
     * Constuctor takes in the FolderPath for the file and creates a list of files to be compared.
     * @param folderPath Folder path that has the OOXML content of the file in JSON format
     * @param roundtripped Whether the file is roundTripped file or not.
     */
    public OoxmlFile(String folderPath, boolean roundtripped) {
        this.data_path = folderPath;
        this.roundtripped = roundtripped;
        jsonFiles = new TreeMap<>();
    }

    /**
     * Load all the subfiles in the Folder for OOXML file. Each of the XML's JSON file is loaded and saved by indexing on name.
     */
    public void loadFromPath() {
        File folder = new File(data_path);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < Objects.requireNonNull(listOfFiles).length; i++) {
            if (listOfFiles[i].isFile()) {
                String fileName = listOfFiles[i].getName();

                JSONParser parser = new JSONParser();
                try {
                    Object obj = parser.parse(new FileReader(listOfFiles[i]));
                    JSONObject jsonObject = (JSONObject) obj;
                    this.jsonFiles.put(fileName, jsonObject);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Getter method for any xml file
     * @param fileName filename whose JSON data is to be retrieved
     * @return JSONObject of the file
     */
    public JSONObject getJson(String fileName) {
        return jsonFiles.get(fileName);
    }

    private void printAllJsons() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        for (Map.Entry<String, JSONObject> entry : this.jsonFiles.entrySet()) {
            System.out.println("Key = " + entry.getKey());
            String prettyJsonString = gson.toJson(entry.getValue());
            System.out.println(prettyJsonString);
        }
    }

    private void printJsons(String fileName) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJsonString = gson.toJson(jsonFiles.get(fileName));
        System.out.println(fileName);
        System.out.println(prettyJsonString);
    }

}
