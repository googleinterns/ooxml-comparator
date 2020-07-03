import java.util.*;

public class FileComparator{
    String file_orig, file_tripped;
    String file_extension;

    FileComparator(String Path1, String Path2) {
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

    /**
     * The main comparision logic for comparision of two strings
     * @param content1 List of string of content for tag1
     * @param content2 List of string of content for tag2
     * @return whether the content of the two tags are same or not
     */
    private boolean comparisionLogic(ArrayList<String> content1, ArrayList<String> content2) {
        ArrayList<Character> delim = new ArrayList<>(Arrays.asList(' ', '\t', '\n'));
        StringBuilder sb = new StringBuilder();
        for (String s : content1) {
            try{
                float val = Float.parseFloat(s);
                val = Math.round(val*100)/100.0f;
                s = String.valueOf(val);
            }catch(NumberFormatException e){
                // not a float!!
            }

            for(int i=0;i<s.length();i++){
                if(!delim.contains(s.charAt(i))){
                   sb.append(s.charAt(i));
                }
            }
        }
        String stringVal1 = (sb.toString());
        sb = new StringBuilder();
        for (String s : content2) {
            try{
                float val = Float.parseFloat(s);
                val = Math.round(val*100)/100.0f;
                s = String.valueOf(val);
            }catch(NumberFormatException e){
                // not a float!!
            }
            for(int i=0;i<s.length();i++){
                if(!delim.contains(s.charAt(i))){
                    sb.append(s.charAt(i));
                }
            }
        }
        String stringVal2 = sb.toString();
        return stringVal1.equals(stringVal2);
    }

    public ArrayList<DiffObject> diffReport;

    private void compareContentByOrder(String tagComp, String FileType, ArrayList<ArrayList<String>> TagContents1, ArrayList<ArrayList<String>> TagContents2){
        if(TagContents2.size()!=TagContents1.size()){
            StatusLogger.AddRecordInfoExec("NUMBER OF "+tagComp+" in "+FileType+" are not same!!");
            ArrayList<String> fillerData  = new ArrayList<>();
            while(TagContents1.size()<TagContents2.size()){
                TagContents1.add(fillerData);
            }
            while(TagContents1.size()>TagContents2.size()){
                TagContents2.add(fillerData);
            }
        }

        int runTagCount = Math.min(TagContents1.size(),TagContents2.size());

        int matched = 0;
        for(int i=0;i<runTagCount;i++){
            boolean isSame = comparisionLogic(TagContents1.get(i),TagContents2.get(i));
            matched+=1;
            if(!isSame){
                diffReport.add(new DiffObject(tagComp,TagContents1.get(i),TagContents2.get(i),"CONTENT DIFFERENT"));
            }
        }
        StatusLogger.AddRecordInfoExec("MATCHED : "+ matched);
    }

    private void compareContentByID(ArrayList<ArrayList<String>> TagContents1, ArrayList<ArrayList<String>> TagContents2, String diffMsg){
        if(TagContents2.size()!=TagContents1.size()){
            StatusLogger.AddRecordInfoExec("NUMBER OF c in "+file_extension+" are not same!!");
            ArrayList<String> fillerData  = new ArrayList<>();
            while(TagContents1.size()<TagContents2.size()){
                TagContents1.add(fillerData);
            }
            while(TagContents1.size()>TagContents2.size()){
                TagContents2.add(fillerData);
            }
        }

        HashMap<String, ArrayList<String>> cellValues = new HashMap<>();
        for(ArrayList<String> it:TagContents1){
            cellValues.put(it.get(0),it);
        }

        int matched = 0;
        for(ArrayList<String> it:TagContents2){
            if(cellValues.containsKey(it.get(0))){
                matched+=1;
                ArrayList<String> actualContent = cellValues.get(it.get(0));
                boolean isSame = comparisionLogic(actualContent,it);
                if(!isSame){
                    diffReport.add(new DiffObject("c",actualContent,it,diffMsg));
                }
            }
        }
        StatusLogger.AddRecordInfoExec("MATCHED : "+ matched);
    }

    /**
     * Fucntion that runs the main comparision for Text in the two files
     * @return Diff objects for each differences found
     */
    public ArrayList<DiffObject> CompareText() {
        diffReport = new ArrayList<>();

        switch (file_extension) {
            case "docx": {
                DocxFile file1 = new DocxFile(file_orig, false);
                DocxFile file2 = new DocxFile(file_tripped, true);

                ArrayList<ArrayList<String>> runTagContents1 = file1.getTextContent();
                ArrayList<ArrayList<String>> runTagContents2 = file2.getTextContent();
                compareContentByOrder("w:r", "docx", runTagContents1, runTagContents2);

                ArrayList<ArrayList<String>> commentContents1 = file1.getCommentContent();
                ArrayList<ArrayList<String>> commentContents2 = file2.getCommentContent();

                compareContentByOrder("w:comment", "docx", commentContents1, commentContents2);
                break;
            }
            case "pptx": {

                PptxFile file1 = new PptxFile(file_orig, false);
                PptxFile file2 = new PptxFile(file_tripped, true);

                ArrayList<ArrayList<String>> runTagContents1 = file1.getTextContent();
                ArrayList<ArrayList<String>> runTagContents2 = file2.getTextContent();

                StatusLogger.AddRecordInfoDebug("BEFORE Content Comparator");
                compareContentByOrder("a:r", "pptx", runTagContents1, runTagContents2);
                StatusLogger.AddRecordInfoDebug("AFTER Content Comparator");

                ArrayList<ArrayList<String>> commentContents1 = file1.getCommentContent();
                ArrayList<ArrayList<String>> commentContents2 = file2.getCommentContent();

                compareContentByOrder("p:cm", "pptx", commentContents1, commentContents2);
                break;
            }
            case "xlsx": {

                XlsxFile file1 = new XlsxFile(file_orig, false);
                XlsxFile file2 = new XlsxFile(file_tripped, true);

                file1.loadSharedStrings();
                file2.loadSharedStrings();
                StatusLogger.AddRecordInfoDebug("After Shared String Loading");

                ArrayList<ArrayList<String>> runTagContents1 = file1.GetTextContent();
                ArrayList<ArrayList<String>> runTagContents2 = file2.GetTextContent();

                compareContentByID(runTagContents1, runTagContents2, "Cell value different");
                StatusLogger.AddRecordInfoDebug("AFTER Content Comparator");

                ArrayList<ArrayList<String>> CommentTagContents1 = file1.getCommentContent();
                ArrayList<ArrayList<String>> CommentTagContents2 = file2.getCommentContent();
                StatusLogger.AddRecordInfoDebug("After GetCommentContent");

                compareContentByID(CommentTagContents1, CommentTagContents2, "Cell Comment Different");
                break;
            }
        }
        return diffReport;
    }
}
