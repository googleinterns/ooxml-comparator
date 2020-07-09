import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class FileComparatorTest {
    public static final String ORIGINAL_FILE_PATH = "Path/to/Folder/for/docx";
    public static final String ROUNDTRIPPED_FILE_PATH = "Path/to/Folder/for/docx";

    public static final String TEST_STRING_ALPHANUMERIC1 = "ABC123";
    public static final String TEST_STRING_ALPHANUMERIC2 = "XYZ456";
    public static final String TEST_STRING_ALPHANUMERIC3 = "abc123";
    public static final String TEST_STRING_ALPHANUMERIC4 = "abc124";

    public static final String TEST_STRING_FLOATING_POINT = "16.000000001";
    public static final String TEST_STRING_INTEGER = "16";

    @Test
    public void comparisionLogic_singleStringSame_shouldReturnSame(){
        FileComparator testObject = new FileComparator(ORIGINAL_FILE_PATH, ROUNDTRIPPED_FILE_PATH);

        ArrayList<String> contentOrigingal = new ArrayList<>();
        ArrayList<String> contentRoundTripped = new ArrayList<>();

        contentOrigingal.add(TEST_STRING_ALPHANUMERIC1);
        contentRoundTripped.add(TEST_STRING_ALPHANUMERIC1);

        assertTrue(testObject.comparisionLogic(contentOrigingal,contentRoundTripped));
    }

    @Test
    public void comparisionLogic_singleStringDifferent_shouldReturnDifferent(){
        FileComparator testObject = new FileComparator(ORIGINAL_FILE_PATH,ROUNDTRIPPED_FILE_PATH);

        ArrayList<String> contentOrigingal = new ArrayList<>();
        ArrayList<String> contentRoundTripped = new ArrayList<>();

        contentOrigingal.add(TEST_STRING_ALPHANUMERIC1);
        contentRoundTripped.add(TEST_STRING_ALPHANUMERIC2);

        assertFalse(testObject.comparisionLogic(contentOrigingal,contentRoundTripped));
    }

    @Test
    public void comparisionLogic_multipleStringSameOrder_shouldReturnSame(){
        FileComparator testObject = new FileComparator(ORIGINAL_FILE_PATH, ROUNDTRIPPED_FILE_PATH);

        ArrayList<String> contentOrigingal = new ArrayList<>();
        ArrayList<String> contentRoundTripped = new ArrayList<>();

        contentOrigingal.add(TEST_STRING_ALPHANUMERIC2);
        contentOrigingal.add(TEST_STRING_ALPHANUMERIC1);
        contentRoundTripped.add(TEST_STRING_ALPHANUMERIC2);
        contentRoundTripped.add(TEST_STRING_ALPHANUMERIC1);

        assertTrue(testObject.comparisionLogic(contentOrigingal,contentRoundTripped));
    }

    @Test
    public void comparisionLogic_multipleStringDifferentOrder_shouldReturnDifferent(){
        FileComparator testObject = new FileComparator(ORIGINAL_FILE_PATH, ROUNDTRIPPED_FILE_PATH);

        ArrayList<String> contentOrigingal = new ArrayList<>();
        ArrayList<String> contentRoundTripped = new ArrayList<>();

        contentOrigingal.add(TEST_STRING_ALPHANUMERIC1);
        contentOrigingal.add(TEST_STRING_ALPHANUMERIC2);
        contentRoundTripped.add(TEST_STRING_ALPHANUMERIC2);
        contentRoundTripped.add(TEST_STRING_ALPHANUMERIC1);

        assertFalse(testObject.comparisionLogic(contentOrigingal,contentRoundTripped));
    }

    @Test
    public void comparisionLogic_singleFloatSame_shouldReturnSame(){
        FileComparator testObject = new FileComparator(ORIGINAL_FILE_PATH, ROUNDTRIPPED_FILE_PATH);

        ArrayList<String> contentOrigingal = new ArrayList<>();
        ArrayList<String> contentRoundTripped = new ArrayList<>();

        contentOrigingal.add(TEST_STRING_FLOATING_POINT);
        contentRoundTripped.add(TEST_STRING_FLOATING_POINT);

        assertTrue(testObject.comparisionLogic(contentOrigingal,contentRoundTripped));
    }

    @Test
    public void comparisionLogic_singleFloatPrecisionChange_shouldReturnSame(){
        FileComparator testObject = new FileComparator(ORIGINAL_FILE_PATH, ROUNDTRIPPED_FILE_PATH);

        ArrayList<String> contentOrigingal = new ArrayList<>();
        ArrayList<String> contentRoundTripped = new ArrayList<>();

        contentOrigingal.add(TEST_STRING_FLOATING_POINT);
        contentRoundTripped.add(TEST_STRING_INTEGER);

        assertTrue(testObject.comparisionLogic(contentOrigingal,contentRoundTripped));
    }

    @Test
    public void comparisionLogic_multipleFloatPrecisionChangeMixed_shouldReturnSame(){
        FileComparator testObject = new FileComparator(ORIGINAL_FILE_PATH, ROUNDTRIPPED_FILE_PATH);

        ArrayList<String> contentOrigingal = new ArrayList<>();
        ArrayList<String> contentRoundTripped = new ArrayList<>();

        contentOrigingal.add(TEST_STRING_FLOATING_POINT);
        contentOrigingal.add(TEST_STRING_ALPHANUMERIC1);
        contentRoundTripped.add(TEST_STRING_INTEGER);
        contentRoundTripped.add(TEST_STRING_ALPHANUMERIC1);

        assertTrue(testObject.comparisionLogic(contentOrigingal,contentRoundTripped));
    }

    @Test
    public void comparisionLogic_multipleFloatPrecisionChangeMixedComcatanation_shouldReturnSame(){
        FileComparator testObject = new FileComparator(ORIGINAL_FILE_PATH, ROUNDTRIPPED_FILE_PATH);

        ArrayList<String> contentOrigingal = new ArrayList<>();
        ArrayList<String> contentRoundTripped = new ArrayList<>();

        contentOrigingal.add(TEST_STRING_FLOATING_POINT);
        contentOrigingal.add(TEST_STRING_ALPHANUMERIC1);
        contentRoundTripped.add(TEST_STRING_INTEGER+TEST_STRING_ALPHANUMERIC1);

        assertTrue(testObject.comparisionLogic(contentOrigingal,contentRoundTripped));
    }

    @Test
    public void comparisionLogic_singleBoldCharacters_shouldReturnSame(){
        FileComparator testObject = new FileComparator(ORIGINAL_FILE_PATH, ROUNDTRIPPED_FILE_PATH);

        ArrayList<String> contentOrigingal = new ArrayList<>();
        ArrayList<String> contentRoundTripped = new ArrayList<>();

        contentOrigingal.add(TEST_STRING_ALPHANUMERIC1);
        contentRoundTripped.add(TEST_STRING_ALPHANUMERIC3);

        assertTrue(testObject.comparisionLogic(contentOrigingal,contentRoundTripped));
    }

    @Test
    public void comparisionLogic_singleStringPartialMatch_shouldReturnDifferent(){
        FileComparator testObject = new FileComparator(ORIGINAL_FILE_PATH, ROUNDTRIPPED_FILE_PATH);

        ArrayList<String> contentOrigingal = new ArrayList<>();
        ArrayList<String> contentRoundTripped = new ArrayList<>();

        contentOrigingal.add(TEST_STRING_ALPHANUMERIC1);
        contentRoundTripped.add(TEST_STRING_ALPHANUMERIC4);

        assertFalse(testObject.comparisionLogic(contentOrigingal,contentRoundTripped));
    }



}