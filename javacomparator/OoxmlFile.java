import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;


/**
 * OoxmlFile Class implement various file loading methods to load JSON files into the Objects.
 */
public class OoxmlFile {
    String fileDataPath;
    boolean roundTripped;

    Map<String, JSONObject> jsonDataFiles;

    /**
     * Constuctor takes in the FolderPath for the file and creates a list of files to be compared.
     *
     * @param fileDataPath Folder path that has the OOXML content of the file in JSON format.
     * @param roundTripped Whether the file is roundTripped file or not.
     */
    public OoxmlFile(String fileDataPath, boolean roundTripped) {
        this.fileDataPath = fileDataPath;
        this.roundTripped = roundTripped;
        jsonDataFiles = new TreeMap<>();
    }

    /**
     * Load all the subfiles in the Folder for OOXML file. Each of the XML's JSON file is loaded and saved by indexing on name.
     */
    public void loadFromPath() {
        File folder = new File(fileDataPath);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < Objects.requireNonNull(listOfFiles).length; i++) {
            if (listOfFiles[i].isFile()) {
                String fileName = listOfFiles[i].getName();
                JSONParser parser = new JSONParser();

                try {
                    Object object = parser.parse(new FileReader(listOfFiles[i]));
                    JSONObject jsonObject = (JSONObject) object;
                    this.jsonDataFiles.put(fileName, jsonObject);

                } catch (Exception e) {
                    StatusLogger.addRecordWarningExec(e.getMessage());
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
        return jsonDataFiles.get(fileName);
    }
}
