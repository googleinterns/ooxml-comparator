import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Comparator {
    String file_orig, file_tripped;
    String file_extension;

    Comparator(String Path1, String Path2) {
        file_orig = Path1;
        file_tripped = Path2;

        if (Path1.endsWith("docx") && Path2.endsWith("docx")) {
            file_extension = "docx";
        } else if (Path1.endsWith("pptx") && Path2.endsWith("pptx")) {
            file_extension = "pptx";
        } else if (Path1.endsWith("xlsx") && Path2.endsWith("xlsx")) {
            file_extension = "xlsx";
        } else {
            file_extension = "invalid";
            System.out.println("Either Path is invalid / File types do not match");
        }
    }

    public boolean CompareContent(ArrayList<String> content1, ArrayList<String> content2) {

        StringBuilder sb = new StringBuilder();
        for (String s : content1) {
            sb.append(s);
        }
        String stringVal1 = (sb.toString());
        sb = new StringBuilder();
        for (String s : content2) {
            sb.append(s);
        }
        String stringVal2 = sb.toString();

        return stringVal1.equals(stringVal2);

    }
    public ArrayList<DiffObject> diffReport;

    private void compareContent(String tagComp,String FileType,ArrayList<ArrayList<String>> TagContents1,ArrayList<ArrayList<String>> TagContents2){
        int runTagCount = TagContents1.size();
        if(TagContents2.size()!=runTagCount){
            StatusLogger.AddRecordINFO("NUMBER OF "+tagComp+" in "+FileType+" are not same!!");
        }
        System.out.println(TagContents1);
        System.out.println(TagContents2);
        for(int i=0;i<runTagCount;i++){
            boolean isSame = CompareContent(TagContents1.get(i),TagContents2.get(i));
            if(!isSame){
                diffReport.add(new DiffObject(tagComp,TagContents1.get(i),TagContents2.get(i),"CONTENT DIFFERENT"));
            }
        }
    }

    private void compareContentXLSX(String tagComp,String FileType,ArrayList<ArrayList<String>> TagContents1,ArrayList<ArrayList<String>> TagContents2){
        int runTagCount = TagContents1.size();
        System.out.println(TagContents1.size());
        System.out.println(TagContents2.size());

        if(TagContents2.size()!=runTagCount){
            System.out.println("NUMBER OF RUNTAGS ARE NOT SAME!!");
            StatusLogger.AddRecordINFO("NUMBER OF RUNTAGS in XLSX are not same!!");
        }

        System.out.println(TagContents1);
        System.out.println(TagContents2);

        HashMap<String, ArrayList<String>> cellValues = new HashMap<String, ArrayList<String>>();
        for(ArrayList<String> it:TagContents1){
            cellValues.put(it.get(0),it);
        }
        int extraCell = 0;
        int matched = 0;
        for(ArrayList<String> it:TagContents2){
            if(cellValues.containsKey(it.get(0))){
                matched+=1;
                ArrayList<String> actualContent = cellValues.get(it.get(0));
                boolean isSame = CompareContent(actualContent,it);
                if(!isSame){
                    diffReport.add(new DiffObject(tagComp,actualContent,it,"CELL TAG CONTENT DIFFERENT"));
                }
            }else{
                extraCell+=1;
            }
        }
    }

    public ArrayList<DiffObject> CompareText() {
        diffReport = new ArrayList<DiffObject>();

        if (file_extension.equals("doc")) {
            DocxFile file1 = new DocxFile(file_orig, false);
            DocxFile file2 = new DocxFile(file_tripped, true);

            ArrayList<ArrayList<String>> runTagContents1 = file1.GetTextContent();
            ArrayList<ArrayList<String>> runTagContents2 = file2.GetTextContent();
            compareContent("w:r","docx",runTagContents1,runTagContents2);

            System.out.println(runTagContents1);
            System.out.println(runTagContents2);

            ArrayList<ArrayList<String>> commentContents1 = file1.GetCommentContent();
            ArrayList<ArrayList<String>> commentContents2 = file2.GetCommentContent();

            compareContent("w:comment","docx",commentContents1,commentContents2);
        }
        else if (file_extension.equals("ppt")) {

            PptxFile file1 = new PptxFile(file_orig, false);
            PptxFile file2 = new PptxFile(file_tripped, true);

            ArrayList<ArrayList<String>> runTagContents1 = file1.GetTextContent();
            ArrayList<ArrayList<String>> runTagContents2 = file2.GetTextContent();
            compareContent("a:r","pptx",runTagContents1,runTagContents2);

            System.out.println("HERE");
            ArrayList<ArrayList<String>> commentContents1 = file1.GetCommentContent();
            System.out.println("THERE");
            ArrayList<ArrayList<String>> commentContents2 = file2.GetCommentContent();
            System.out.println(commentContents1);
            System.out.println(commentContents2);
            compareContent("p:cm","pptx",commentContents1,commentContents2);
        }
        else if(file_extension.equals("xlsx")){
            // TODO: Not working fine!! Some comarisions fails... Looking into it.

            XlsxFile file1 = new XlsxFile(file_orig, false);
            XlsxFile file2 = new XlsxFile(file_tripped, true);

            file1.loadSharedStrings();
            file2.loadSharedStrings();

            ArrayList<ArrayList<String>> runTagContents1 = file1.GetTextContent();
            ArrayList<ArrayList<String>> runTagContents2 = file2.GetTextContent();

            compareContentXLSX("c","xlsx",runTagContents1,runTagContents2);

            ArrayList<ArrayList<String>> CommentTagContents1 = file1.GetCommentContent();
            ArrayList<ArrayList<String>> CommentTagContents2 = file2.GetCommentContent();
            System.out.println("HERE");
            System.out.println(CommentTagContents1);
            System.out.println(CommentTagContents2);
            compareContentXLSX("c","xlsx",CommentTagContents1,CommentTagContents2);
        }
        return diffReport;
    }
}
