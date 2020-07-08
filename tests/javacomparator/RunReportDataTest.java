import org.junit.Test;

import static org.junit.Assert.*;

public class RunReportDataTest {
    @Test
    public void getPercentageNoDiffs_divisionByZero_shouldReturn100() {
        RunReportData testObject = new RunReportData("docx");
        assert(testObject.getPercentageNoDiff()==100);
    }
    @Test
    public void getPercentageNoDiffs_generalFraction_shouldReturnAppropriateDivision() {
        RunReportData testObject = new RunReportData("docx");
        testObject.totalFilesMatched = 5;
        testObject.numberOfFileNoDiff = 3;
        assert(testObject.getPercentageNoDiff()==60);
    }
}