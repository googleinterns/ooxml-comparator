import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RunReportData {
    public String fileType;
    public int totalFilesMatched, numberOfFileNoDiff, totalDiff;
    public HashMap<String, Integer> tagCausingDiff;
    public HashMap<String, Integer> typeCausingDiff;
    public int filesContainingTextDiffs, filesContainingCommentDiffs, filesWithOnlyTextDiff, filesWithOnlyCommentDiff;
    ArrayList<Integer> timeTaken;

    RunReportData(String fileType) {
        this.fileType = fileType;
        totalFilesMatched = 0;
        numberOfFileNoDiff = 0;
        totalDiff = 0;
        tagCausingDiff = new HashMap<String, Integer>();
        typeCausingDiff = new HashMap<>();
        typeCausingDiff.put("0", 0);
        typeCausingDiff.put("1", 0);
        filesContainingTextDiffs = 0;
        filesContainingCommentDiffs = 0;
        filesWithOnlyTextDiff = 0;
        filesWithOnlyCommentDiff = 0;
        timeTaken = new ArrayList<Integer>();
    }

    public void addTime(Integer timeVal) {
        timeTaken.add(timeVal);
    }

    public void addTag(String tag) {
        Integer count = tagCausingDiff.get(tag);
        if (count == null) {
            count = 0;
        }
        tagCausingDiff.put(tag, count + 1);
    }

    public void addType(String tag) {
        Integer count = typeCausingDiff.get(tag);
        if (count == null) {
            count = 0;
        }
        typeCausingDiff.put(tag, count + 1);
    }

    public float getPercentageNoDiff() {
        return (numberOfFileNoDiff * 100.0f) / totalFilesMatched;
    }

    public String getMostFreqTagCausingDiff() {
        String MostFreqTag = "noStringAvail";
        Integer Freq = 0;
        for (Map.Entry<String, Integer> v : tagCausingDiff.entrySet()) {
            if (v.getValue() > Freq) {
                Freq = v.getValue();
                MostFreqTag = v.getKey();
            }
        }
        return MostFreqTag;
    }

    public Integer totalTimeTaken() {
        Integer timeTakenTot = 0;
        for (Integer v : timeTaken) {
            timeTakenTot += v;
        }
        return timeTakenTot;
    }

    public float avgTimeTaken() {
        Integer timeTakenAvg = 0;
        for (Integer v : timeTaken) {
            timeTakenAvg += v;
        }
        return ((float) timeTakenAvg) / timeTaken.size();
    }

    public float maxTimeTaken() {
        float TakenMax = 0;
        for (Integer v : timeTaken) {
            if (v > TakenMax) {
                TakenMax = v;
            }
        }
        return TakenMax;
    }

    public static long percentile(ArrayList<Integer> latencies, double percentile) {
        if (latencies.isEmpty()) return 0;
        Collections.sort(latencies);
        int index = (int) Math.ceil(percentile / 100.0 * latencies.size());
        if (index > 0) index--;
        return latencies.get(index);
    }

    public float get99Percentile() {
        return percentile(timeTaken, 99.0);
    }

    public float get50Percentile() {
        return percentile(timeTaken, 50.0);
    }
}
