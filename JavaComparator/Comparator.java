import java.util.ArrayList;

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

    public ArrayList<DiffObject> CompareText() {
        ArrayList<DiffObject> diffReport = new ArrayList<DiffObject>();

        if (file_extension.equals("docx")) {
            DocxFile file1 = new DocxFile(file_orig, false);
            DocxFile file2 = new DocxFile(file_tripped, true);

            ArrayList<ArrayList<String>> runTagContents1 = file1.GetTextContent();
            ArrayList<ArrayList<String>> runTagContents2 = file2.GetTextContent();

            System.out.println(runTagContents1.toString());
            System.out.println(runTagContents2.toString());

            int runTagCount = runTagContents1.size();
            if(runTagContents2.size()!=runTagCount){
                System.out.println("NUMBER OF RUNTAGS ARE NOT SAME!!");
                return diffReport;
            }

            for(int i=0;i<runTagCount;i++){
                boolean isSame = CompareContent(runTagContents1.get(i),runTagContents2.get(i));
                if(!isSame){
                    diffReport.add(new DiffObject("w:r",runTagContents1.get(i),runTagContents2.get(i),"RUN TAG CONTENT DIFFERENT"));
                }
            }
        }
        else if (file_extension.equals("pptx")) {

            PptxFile file1 = new PptxFile(file_orig, false);
            PptxFile file2 = new PptxFile(file_tripped, true);

            ArrayList<ArrayList<String>> runTagContents1 = file1.GetTextContent();
            ArrayList<ArrayList<String>> runTagContents2 = file2.GetTextContent();

//            System.out.println(runTagContents1.toString());
//            System.out.println(runTagContents2.toString());

            int runTagCount = runTagContents1.size();
            System.out.println(runTagContents1.size());
            System.out.println(runTagContents2.size());

//            if(runTagContents2.size()!=runTagCount){
//                System.out.println("NUMBER OF RUNTAGS ARE NOT SAME!!");
//                throw new Exception("HYPOTHESIS FAILED");
//            }

            System.out.println(runTagContents1);
            System.out.println(runTagContents2);

            for(int i=0;i<runTagCount;i++){
                boolean isSame = CompareContent(runTagContents1.get(i),runTagContents2.get(i));
                if(!isSame){
                    diffReport.add(new DiffObject("a:r",runTagContents1.get(i),runTagContents2.get(i),"RUN TAG CONTENT DIFFERENT"));
                }
            }
        }
        else if(file_extension.equals("xls")){
            // TODO: Not working fine!! Some comarisions fails... Looking into it.
            
            XlsxFile file1 = new XlsxFile(file_orig, false);
            XlsxFile file2 = new XlsxFile(file_tripped, true);

            file1.loadSharedStrings();
            file2.loadSharedStrings();

            ArrayList<ArrayList<String>> runTagContents1 = file1.GetTextContent();
            ArrayList<ArrayList<String>> runTagContents2 = file2.GetTextContent();

//            System.out.println(runTagContents1.toString());
//            System.out.println(runTagContents2.toString());

            int runTagCount = runTagContents1.size();
            System.out.println(runTagContents1.size());
            System.out.println(runTagContents2.size());

//            if(runTagContents2.size()!=runTagCount){
//                System.out.println("NUMBER OF RUNTAGS ARE NOT SAME!!");
//                throw new Exception("HYPOTHESIS FAILED");
//            }

            System.out.println(runTagContents1);
            System.out.println(runTagContents2);

            for(int i=0;i<runTagCount;i++){
                boolean isSame = CompareContent(runTagContents1.get(i),runTagContents2.get(i));
                if(!isSame){
                    diffReport.add(new DiffObject("c",runTagContents1.get(i),runTagContents2.get(i),"CELL TAG CONTENT DIFFERENT"));
                }
            }
            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        }
        return diffReport;
    }
}
