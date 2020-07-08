public class ManualTesting {
    public static void runComparator(String pathOrig, String pathRound, String outputPath) {
        DiffGenerator diff = new DiffGenerator(pathOrig, pathRound, outputPath);
        try {
            diff.prepareFolderAndRun();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        runComparator("/home/vivgupta/OOXMLcomp/generate/original","/home/vivgupta/OOXMLcomp/generate/roundtripped","/home/vivgupta/OOXMLcomp/generate/output");
    }
}
