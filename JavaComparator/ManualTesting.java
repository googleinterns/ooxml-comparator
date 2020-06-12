import org.json.simple.JSONObject;

import java.util.ArrayList;

public class ManualTesting {

    public static void testDocx() {
        DocxFile curFile = new DocxFile("/home/vivgupta/OOXMLcomp/generate/original/Kix/center_tab_stop_docx", false);
        curFile.debug();
        //curFile.printAllJsons();
        //curFile.printJsons("word_document.xml.json");
        ArrayList<JSONObject> temp = curFile.getJson();
        JsonUtility jsonFunc = new JsonUtility();
        for (JSONObject file : temp) {
            JsonUtility.recurse_visit(file, null, true, true);
        }
    }

    public static void testPptx() {
        PptxFile curFile = new PptxFile("/home/vivgupta/OOXMLcomp/generate/original/Punch/00007492-ENGLISH_pptx", false);

        //curFile.printAllJsons();
        //curFile.printJsons("word_document.xml.json");
        curFile.debug();
    }

    public static void testXlsx() {
        XlsxFile curFile = new XlsxFile("/home/vivgupta/OOXMLcomp/generate/original/Ritz/00049902-ENGLISH_xlsx", false);

        //curFile.printAllJsons();
        //curFile.printJsons("word_document.xml.json");
        curFile.debug();
    }

    public static void testComparator(){
        Comparator comparator = new Comparator("/home/vivgupta/OOXMLcomp/generate/original/Kix/center_tab_stop_docx","/home/vivgupta/OOXMLcomp/generate/roundtripped/Kix/center_tab_stop_docx");
        try {
            comparator.compareText();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public static void testComparator2(){
        Comparator comparator = new Comparator("/home/vivgupta/OOXMLcomp/generate/original/Kix/VR_Bank_GmbH_docx","/home/vivgupta/OOXMLcomp/generate/roundtripped/Kix/VR_Bank_GmbH_docx");
        try {
            comparator.compareText();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public static void testComparator3(){
        Comparator comparator = new Comparator("/home/vivgupta/OOXMLcomp/generate/original/Punch/00007492-ENGLISH_pptx","/home/vivgupta/OOXMLcomp/generate/roundtripped/Punch/00007492-ENGLISH_pptx");
        try {
            comparator.compareText();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public static void testComparator4(){
        Comparator comparator = new Comparator("/home/vivgupta/OOXMLcomp/generate/original/Ritz/00049976-ENGLISH_xlsx","/home/vivgupta/OOXMLcomp/generate/roundtripped/Ritz/00049976-ENGLISH_xlsx");
        try {
            comparator.compareText();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public static void testComparator5(){
        Comparator comparator = new Comparator("/home/vivgupta/OOXMLcomp/generate/original/Ritz/00049936-ENGLISH_xlsx","/home/vivgupta/OOXMLcomp/generate/roundtripped/Ritz/00049936-ENGLISH_xlsx");
        try {
            comparator.compareText();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        //System.out.println("Hello World !!");
        //testDocx();
        //testPptx();
        //testXlsx();
        //testComparator();
        //testComparator2();
        //testComparator3();
        //testComparator4();
        testComparator5();
    }
}
