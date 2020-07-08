import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Class maintains the Data required for each type of editor and global results in form XLSX format.
 */
public class RunReportData {

    public String fileTypeName;
    public int totalFilesMatched, numberOfFileNoDiff, totalDiff, filesContainingTextDiffs, filesContainingCommentDiffs, filesWithOnlyTextDiff, filesWithOnlyCommentDiff;
    public HashMap<String, Integer> tagCausingDiff, typeCausingDiff;
    public ArrayList<Integer> timeTakenPerFile;

    /**
     * Initializes various structures that needs to be maintained to get various metrics.
     * @param fileTypeName the type of file the Object is responsible for.
     */
    RunReportData(String fileTypeName) {
        this.fileTypeName = fileTypeName;
        this.tagCausingDiff = new HashMap<>();
        this.typeCausingDiff = new HashMap<>();
        this.timeTakenPerFile = new ArrayList<>();
    }

    /**
     * Adds the time for the file to be considered while calculating latency.
     * @param timeValForFile time taken by the file in ms.
     */
    public void addTimeForFile(Integer timeValForFile) {
        timeTakenPerFile.add(timeValForFile);
    }

    /**
     * Add the count of the tags responsible for Diff.
     * @param tagName tag causing Diff.
     */
    public void addTagCount(String tagName) {
        Integer countTagDiff = tagCausingDiff.get(tagName);
        if (countTagDiff == null) {
            countTagDiff = 0;
        }
        tagCausingDiff.put(tagName, countTagDiff + 1);
    }

    /**
     * Add the count of the tags type responsible for Diff.
     * @param tagType tag causing the Diff.
     */
    public void addTypeCount(String tagType) {
        Integer countTagType = typeCausingDiff.get(tagType);
        if (countTagType == null) {
            countTagType = 0;
        }
        typeCausingDiff.put(tagType, countTagType + 1);
    }

    /**
     * Function to get the percentage of files having no Diffs
     * @return Percentage of files with no Diff.
     */
    public float getPercentageNoDiff() {
        return (numberOfFileNoDiff * 100.0f) / totalFilesMatched;
    }

    /**
     * Reports the Tag that causes the most number of Diff.
     * @return tag name causing the Diff.
     */
    public String getMostFreqTagCausingDiff() {
        String mostFrequentTag = "noStringAvail";
        Integer mostFrequentTagsCount = 0;
        for (Map.Entry<String, Integer> tagCountEntry : tagCausingDiff.entrySet()) {
            if (tagCountEntry.getValue() > mostFrequentTagsCount) {
                mostFrequentTagsCount = tagCountEntry.getValue();
                mostFrequentTag = tagCountEntry.getKey();
            }
        }
        return mostFrequentTag;
    }

    /**
     * Calculates the total time taken by all files of the specified type.
     * @return Value of total time taken.
     */
    public Integer totalTimeTaken() {
        Integer timeTakenInTotal = 0;
        for (Integer fileTimeTaken : timeTakenPerFile) {
            timeTakenInTotal += fileTimeTaken;
        }
        return timeTakenInTotal;
    }

    /**
     * Calculates the Average time taken by all files of the specified type.
     * @return Value of Average time taken.
     */
    public float averageTimeTaken() {
        Integer timeTakenAvgerage = 0;
        for (Integer fileTimeTaken : timeTakenPerFile) {
            timeTakenAvgerage += fileTimeTaken;
        }
        return ((float) timeTakenAvgerage) / timeTakenPerFile.size();
    }

    /**
     * Calculates the Maximum taken by any of the files of the specified type.
     * @return Value of Maximum time taken.
     */
    public float maximumTimeTaken() {
        float maximumTimeTakenCurrent = 0;
        for (Integer fileTimeTaken : timeTakenPerFile) {
            if (fileTimeTaken > maximumTimeTakenCurrent) {
                maximumTimeTakenCurrent = fileTimeTaken;
            }
        }
        return maximumTimeTakenCurrent;
    }

    /**
     * Calculates the percentile latency out of all the files time present
     * @param latencies list containing the time taken for each file.
     * @param percentile percentile that is required to be calculated.
     * @return the value with the required percentile.
     */
    public static long percentileLatency(ArrayList<Integer> latencies, double percentile) {
        if (latencies.isEmpty()) return 0;
        Collections.sort(latencies);
        int index = (int) Math.ceil(percentile / 100.0 * latencies.size());
        if (index > 0) index--;
        return latencies.get(index);
    }

    /**
     * Calculates the 99 percentile for the latency data.
     * @return returns the 99 percentile value
     */
    public float get99PercentileLatency() {
        return percentileLatency(timeTakenPerFile, 99.0);
    }

    /**
     * Calculates the 50 percentile for the latency data.
     * @return returns the 50 percentile value
     */
    public float get50PercentileLatency() {
        return percentileLatency(timeTakenPerFile, 50.0);
    }
}
