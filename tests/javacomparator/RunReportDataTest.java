import org.junit.Assert;
import org.junit.Test;


public class RunReportDataTest {
    public static final float FULL_PERCENTAGE = 100f;
    public static final String FILE_TYPE_NAME_DOCX = "docx";
    public static final int TEST_NUMBER_OF_FILE_NO_DIFF = 3;
    public static final int TEST_NUMBER_OF_FILE_MATCHED = 5;
    public static final float PRECISION_FOR_FLOATING_POINT_MATCH = 1e-6f;
    public static final String TEST_TAG_TO_ENTER = "c";
    public static final Integer INITIAL_VALUE_OF_ENTRY = 5;
    public static final float TEST_LATENCY_PERCENTILE_CALCULATION_CASE1 = 99;
    public static final float TEST_LATENCY_PERCENTILE_CALCULATION_CASE2 = 50;
    public static final int TEST_LATENCY_METRIC_ON_RANGE_LOW = 112;
    public static final int TEST_LATENCY_METRIC_ON_RANGE_HIGH = 183;

    @Test
    public void getPercentageNoDiffs_divisionByZero_shouldReturnFullPercentage() {
        RunReportData testObject = new RunReportData(FILE_TYPE_NAME_DOCX);
        Assert.assertEquals(testObject.getPercentageNoDiff(),FULL_PERCENTAGE,1e-6f);
    }

    @Test
    public void getPercentageNoDiffs_generalFraction_shouldReturnAppropriateDivision() {
        RunReportData testObject = new RunReportData(FILE_TYPE_NAME_DOCX);
        testObject.totalFilesMatched = TEST_NUMBER_OF_FILE_MATCHED;
        testObject.numberOfFileNoDiff = TEST_NUMBER_OF_FILE_NO_DIFF;
        Assert.assertEquals(testObject.getPercentageNoDiff(),60f,1e-6f);
    }

    @Test
    public void addTagCount_startingFromZero_shouldRegisterTheChange(){
        RunReportData testObject = new RunReportData(FILE_TYPE_NAME_DOCX);
        testObject.addTagCount(TEST_TAG_TO_ENTER);
        Assert.assertEquals(testObject.tagCausingDiff.get(TEST_TAG_TO_ENTER) , Integer.valueOf(1));
    }

    @Test
    public void addTagCount_startingFromFixed_shouldRegisterTheChange(){
        RunReportData testObject = new RunReportData(FILE_TYPE_NAME_DOCX);
        testObject.tagCausingDiff.put(TEST_TAG_TO_ENTER,INITIAL_VALUE_OF_ENTRY);
        testObject.addTagCount(TEST_TAG_TO_ENTER);
        Assert.assertEquals(testObject.tagCausingDiff.get(TEST_TAG_TO_ENTER) , Integer.valueOf(INITIAL_VALUE_OF_ENTRY+1));
    }

    @Test
    public void addTypeCount_startingFromZero_shouldRegisterTheChange(){
        RunReportData testObject = new RunReportData(FILE_TYPE_NAME_DOCX);
        testObject.addTypeCount(TEST_TAG_TO_ENTER);
        Assert.assertEquals(testObject.typeCausingDiff.get(TEST_TAG_TO_ENTER) , Integer.valueOf(1));
    }

    @Test
    public void addTypeCount_startingFromFixed_shouldRegisterTheChange(){
        RunReportData testObject = new RunReportData(FILE_TYPE_NAME_DOCX);
        testObject.typeCausingDiff.put(TEST_TAG_TO_ENTER,INITIAL_VALUE_OF_ENTRY);
        testObject.addTypeCount(TEST_TAG_TO_ENTER);
        Assert.assertEquals(testObject.typeCausingDiff.get(TEST_TAG_TO_ENTER) , Integer.valueOf(INITIAL_VALUE_OF_ENTRY+1));
    }

    @Test
    public void percentileLatency_correctFlooring_shouldBehaveIdeallyAsSame(){
        RunReportData testObject = new RunReportData(FILE_TYPE_NAME_DOCX);
        for(int i=1;i<= 100;i++){
            testObject.addTimeForFile(i);
        }
        Assert.assertEquals(RunReportData.percentileLatency(testObject.timeTakenPerFile,TEST_LATENCY_PERCENTILE_CALCULATION_CASE1),
                TEST_LATENCY_PERCENTILE_CALCULATION_CASE1,
                PRECISION_FOR_FLOATING_POINT_MATCH);
        Assert.assertEquals(RunReportData.percentileLatency(testObject.timeTakenPerFile,TEST_LATENCY_PERCENTILE_CALCULATION_CASE2),
                TEST_LATENCY_PERCENTILE_CALCULATION_CASE2,
                PRECISION_FOR_FLOATING_POINT_MATCH);
    }

    @Test
    public void totalTimeTaken_testOnRange_shouldReturnSum(){
        RunReportData testObject = new RunReportData(FILE_TYPE_NAME_DOCX);
        float totatSumOfRange = 0;
        for(int i= TEST_LATENCY_METRIC_ON_RANGE_LOW;i<= TEST_LATENCY_METRIC_ON_RANGE_HIGH;i++){
            testObject.addTimeForFile(i);
            totatSumOfRange += i;
        }
        Assert.assertEquals(testObject.totalTimeTaken(),totatSumOfRange,PRECISION_FOR_FLOATING_POINT_MATCH);
    }

    @Test
    public void averageTimeTaken(){
        RunReportData testObject = new RunReportData(FILE_TYPE_NAME_DOCX);
        float averageOfRange = (TEST_LATENCY_METRIC_ON_RANGE_HIGH+TEST_LATENCY_METRIC_ON_RANGE_LOW)/2.0f;
        for(int i= TEST_LATENCY_METRIC_ON_RANGE_LOW;i<= TEST_LATENCY_METRIC_ON_RANGE_HIGH;i++){
            testObject.addTimeForFile(i);
        }
        Assert.assertEquals(testObject.averageTimeTaken(),averageOfRange,PRECISION_FOR_FLOATING_POINT_MATCH);
    }

    @Test
    public void maximumTimeTaken(){
        RunReportData testObject = new RunReportData(FILE_TYPE_NAME_DOCX);
        float maximumOfRange = TEST_LATENCY_METRIC_ON_RANGE_HIGH;
        for(int i= TEST_LATENCY_METRIC_ON_RANGE_LOW;i<= TEST_LATENCY_METRIC_ON_RANGE_HIGH;i++){
            testObject.addTimeForFile(i);
        }
        Assert.assertEquals(testObject.maximumTimeTaken(),maximumOfRange,PRECISION_FOR_FLOATING_POINT_MATCH);
    }
}