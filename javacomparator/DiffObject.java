import java.util.ArrayList;

/**
 * Each of the Diff between some tag is stored as the object of this class.
 */
public class DiffObject {
    String tagCausingDiff;
    String typeOfTag;
    ArrayList<String> contentOfTagInOriginalFile, contentOfTagInRoundtrippedFile;
    String detailsOfDiffCause;

    /**
     * @param tagCausingDiff    Tag in which this diff was found
     * @param contentOfTagInOriginalFile  The content of the tag in the first file
     * @param contentOfTagInRoundtrippedFile   The content of the tag in the second file
     * @param detailsOfDiffCause Some text details for the reason of Diff
     */
    public DiffObject(String tagCausingDiff, String typeOfTag, ArrayList<String> contentOfTagInOriginalFile, ArrayList<String> contentOfTagInRoundtrippedFile, String detailsOfDiffCause) {
        this.tagCausingDiff = tagCausingDiff;
        this.typeOfTag = typeOfTag;
        this.contentOfTagInOriginalFile = contentOfTagInOriginalFile;
        this.contentOfTagInRoundtrippedFile = contentOfTagInRoundtrippedFile;
        this.detailsOfDiffCause = detailsOfDiffCause;
    }

    /**
     * @return String[] object of the stored info to be written by XLSXWriter
     */
    public String[] getCsvEntry() {
        return new String[]{tagCausingDiff, typeOfTag, contentOfTagInOriginalFile.toString(), contentOfTagInRoundtrippedFile.toString(), detailsOfDiffCause};
    }
}
