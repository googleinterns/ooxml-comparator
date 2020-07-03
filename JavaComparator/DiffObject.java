import java.util.ArrayList;

/**
 * Each of the Diff between some tag is stored as the object of this class.
 */
public class DiffObject {
    String tag;
    ArrayList<String> content1,content2;
    String details;

    /**
     * @param tag Tag in which this diff was found
     * @param obj1 The content of the tag in the first file
     * @param obj2 The content of the tag in the second file
     * @param detail Some text details for the reason of Diff
     */
    public DiffObject(String tag, ArrayList<String> obj1, ArrayList<String> obj2, String detail){
        this.tag = tag;
        content1 = obj1;
        content2 = obj2;
        details = detail;
    }

    /**
     * @return String[] object of the stored info to be written by XLSXWriter
     */
    public String[] getCsvEntry(){
        return new String[]{ tag, content1.toString(), content2.toString(), details};
    }
}
