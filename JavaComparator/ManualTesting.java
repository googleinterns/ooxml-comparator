public class ManualTesting {

    public static void runComparator(String pathOrig, String pathRound, String outputPath){
        DiffGenerator diff = new DiffGenerator(pathOrig,pathRound,outputPath);
        try {
            diff.prepareFolderAndRun();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        //runComparator("/home/vivgupta/OOXMLcomp/generate/original","/home/vivgupta/OOXMLcomp/generate/roundtripped","/home/vivgupta/OOXMLcomp/generate/output");
        runComparator("/home/vivgupta/ooxml-comparator/DataParser/Corpus (250 files from docx, pptx, xlsx)/Original/generated","/home/vivgupta/ooxml-comparator/DataParser/Corpus (250 files from docx, pptx, xlsx)/Roundtripped/generated","/home/vivgupta/OOXMLcomp/generate/output");
        //runComparator("/home/vivgupta/ooxml-comparator/DataParser/Lab/Orig","/home/vivgupta/ooxml-comparator/DataParser/Lab/Round","/home/vivgupta/ooxml-comparator/DataParser/Lab/out");
    }
}
