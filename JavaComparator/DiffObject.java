import java.util.ArrayList;

public class DiffObject {
    String tag;
    ArrayList<String> content1,content2;
    String details;

    public DiffObject(String tag, ArrayList<String> obj1, ArrayList<String> obj2, String detail){
        this.tag = tag;
        content1 = obj1;
        content2 = obj2;
        details = detail;
    }

    public String[] getCsvEntry(){
        return new String[]{ tag, content1.toString(), content2.toString(), details};
    }

}
