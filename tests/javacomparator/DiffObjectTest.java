import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class DiffObjectTest {
    public static final String TEST_TAG_NAME = "randomTag";
    public static final String TEST_TAG_TYPE = "0";
    public static final String TEST_CONTENT1 = "randomContent1";
    public static final String TEST_CONTENT2 = "randomContent2";
    public static final String TEST_CONTENT_REASON = "content Different";
    public static final String TEST_EMPTY_STRING = "";

    @Test
    public void getCsvEntry_fillingFixedStrings_shouldReturnSameInCSV(){
        ArrayList<String> content1 = new ArrayList<>();
        ArrayList<String> content2 = new ArrayList<>();
        content1.add(TEST_CONTENT1);
        content2.add(TEST_CONTENT2);

        DiffObject testObject = new DiffObject(TEST_TAG_NAME,
                TEST_TAG_TYPE,
                content1,
                content2,
                TEST_CONTENT_REASON);

        String[] actualCsvEntry = new String[5];
        actualCsvEntry[0] = TEST_TAG_NAME;
        actualCsvEntry[1] = TEST_TAG_TYPE;
        actualCsvEntry[2] = "["+TEST_CONTENT1+"]";
        actualCsvEntry[3] = "["+TEST_CONTENT2+"]";
        actualCsvEntry[4] = TEST_CONTENT_REASON;
        Assert.assertArrayEquals(testObject.getCsvEntry(),actualCsvEntry);
    }

    @Test
    public void getCsvEntry_fillingEmptyStrings_shouldReturnAllEmpty(){
        ArrayList<String> content1 = new ArrayList<>();
        ArrayList<String> content2 = new ArrayList<>();
        content1.add(TEST_EMPTY_STRING);
        content2.add(TEST_EMPTY_STRING);

        DiffObject testObject = new DiffObject(TEST_EMPTY_STRING,
                TEST_EMPTY_STRING,
                content1,
                content2,
                TEST_EMPTY_STRING);

        String[] actualCsvEntry = new String[5];
        actualCsvEntry[0] = TEST_EMPTY_STRING;
        actualCsvEntry[1] = TEST_EMPTY_STRING;
        actualCsvEntry[2] = "["+TEST_EMPTY_STRING+"]";
        actualCsvEntry[3] = "["+TEST_EMPTY_STRING+"]";
        actualCsvEntry[4] = TEST_EMPTY_STRING;
        Assert.assertArrayEquals(testObject.getCsvEntry(),actualCsvEntry);
    }
}