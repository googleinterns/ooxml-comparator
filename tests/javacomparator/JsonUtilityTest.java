import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class JsonUtilityTest {

    public static final String TEST_JSON_TREE_SIMPLE_NO_MATCH = "{\"a\":\"abc\"}";
    public static final String TEST_JSON_TREE_SIMPLE_DIRECT_FULL_MATCH = "{\"X\":\"abc\"}";

    public static final String TEST_JSON_TREE_COMPLEX_NO_MATCH = "{\"a\" : [ { \"c\":\"\",}, {\"b\":\"abc\"} ,{\"x\":\"xyz\"} ] }";
    public static final String TEST_JSON_TREE_COMPLEX_DIRECT_FULL_MATCH = "{\"X\" : [ { \"c\":\"\",}, {\"b\":\"abc\"} ,{\"x\":\"xyz\"} ] }";
    public static final String TEST_JSON_TREE_COMPLEX_DIRECT_SUBTREE_MATCH = "{\"a\" : [ { \"c\":\"\",}, {\"X\":\"abc\"} ,{\"x\":\"xyz\"} ] }";
    public static final String TEST_JSON_TREE_COMPLEX_SINGLE_MATCH = "{\"X\":\"abc\"}";

    public static final String TEST_JSON_TREE_COMPLEX_NON_NESTED_SUBTREE_MATCH = "{\"a\" : [ { \"c\":\"\",}, {\"X\":\"abc\"} ,{\"X\":\"xyz\"} ] }";
    public static final String TEST_JSON_TREE_COMPLEX_NON_NESTED_SUBTREE_MATCH_OCCURENCE_ONE = "{\"X\":\"abc\"}";
    public static final String TEST_JSON_TREE_COMPLEX_NON_NESTED_SUBTREE_MATCH_OCCURENCE_TWO = "{\"X\":\"xyz\"}";

    public static final String TEST_JSON_TREE_COMPLEX_NESTED_SUBTREE_MATCH = "{\"X\" : [ { \"c\":\"\",}, {\"b\":\"abc\"} ,{\"X\":\"xyz\"} ] }";
    public static final String TEST_JSON_TREE_COMPLEX_NESTED_SUBTREE_MATCH_OCCURENCE_ONE = "{\"X\" : [ { \"c\":\"\",}, {\"b\":\"abc\"} ,{\"X\":\"xyz\"} ] }";
    public static final String TEST_JSON_TREE_COMPLEX_NESTED_SUBTREE_MATCH_OCCURENCE_TWO = "{\"X\":\"xyz\"}";

    public static final String TEST_JSON_SEARCH_MATCH_STRING_ONE = "";
    public static final String TEST_JSON_SEARCH_MATCH_STRING_TWO = "abc";
    public static final String TEST_JSON_SEARCH_MATCH_STRING_THREE = "xyz";

    public static final String TEST_JSON_SEARCH_STRING = "X";

    public JSONParser jsonParser = new JSONParser();

    public JSONObject getJsonFromString(String stringToParse){
        try {
            return (JSONObject) jsonParser.parse(stringToParse);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Test
    public void extractTag_noMatchFoundSimple_shouldReturnEmptyList(){
        JSONObject testTree = getJsonFromString(TEST_JSON_TREE_SIMPLE_NO_MATCH);
        ArrayList<String> tagsToSearch = new ArrayList<>();
        tagsToSearch.add(TEST_JSON_SEARCH_STRING);

        ArrayList<JSONObject> matchSubtree = JsonUtility.extractTag(testTree,tagsToSearch);
        assertTrue(matchSubtree.isEmpty());
    }

    @Test
    public void extractTag_matchFoundSimple_shouldReturnTheWholeTree(){
        JSONObject testTree = getJsonFromString(TEST_JSON_TREE_SIMPLE_DIRECT_FULL_MATCH);
        ArrayList<String> tagsToSearch = new ArrayList<>();
        tagsToSearch.add(TEST_JSON_SEARCH_STRING);

        ArrayList<JSONObject> expectedResult = new ArrayList<>();
        expectedResult.add(testTree);

        ArrayList<JSONObject> matchSubtree = JsonUtility.extractTag(testTree,tagsToSearch);
        assertEquals(expectedResult.toString(),matchSubtree.toString());
    }

    @Test
    public void extractTag_noMatchFoundComplex_shouldReturnEmptyList(){
        JSONObject testTree = getJsonFromString(TEST_JSON_TREE_COMPLEX_NO_MATCH);
        ArrayList<String> tagsToSearch = new ArrayList<>();
        tagsToSearch.add(TEST_JSON_SEARCH_STRING);

        ArrayList<JSONObject> matchSubtree = JsonUtility.extractTag(testTree,tagsToSearch);
        assertTrue(matchSubtree.isEmpty());
    }

    @Test
    public void extractTag_matchFoundComplexFullTree_shouldReturnTheWholeTree(){
        JSONObject testTree = getJsonFromString(TEST_JSON_TREE_COMPLEX_DIRECT_FULL_MATCH);
        ArrayList<String> tagsToSearch = new ArrayList<>();
        tagsToSearch.add(TEST_JSON_SEARCH_STRING);

        ArrayList<JSONObject> expectedResult = new ArrayList<>();
        expectedResult.add(testTree);

        ArrayList<JSONObject> matchSubtree = JsonUtility.extractTag(testTree,tagsToSearch);
        assertEquals(expectedResult.toString(),matchSubtree.toString());
    }

    @Test
    public void extractTag_matchFoundComplexSubTree_shouldReturnTheMatchedSubtree(){
        JSONObject testTree = getJsonFromString(TEST_JSON_TREE_COMPLEX_DIRECT_SUBTREE_MATCH);
        ArrayList<String> tagsToSearch = new ArrayList<>();
        tagsToSearch.add(TEST_JSON_SEARCH_STRING);

        ArrayList<JSONObject> expectedResult = new ArrayList<>();
        JSONObject matchJsonData = getJsonFromString(TEST_JSON_TREE_COMPLEX_SINGLE_MATCH);
        expectedResult.add(matchJsonData);

        ArrayList<JSONObject> matchSubtree = JsonUtility.extractTag(testTree,tagsToSearch);
        assertEquals(expectedResult.toString(),matchSubtree.toString());
    }

    @Test
    public void extractTag_matchFoundComplexSubTreeNonNested_shouldReturnMultipleMatchedSubtree(){
        JSONObject testTree = getJsonFromString(TEST_JSON_TREE_COMPLEX_NON_NESTED_SUBTREE_MATCH);
        ArrayList<String> tagsToSearch = new ArrayList<>();
        tagsToSearch.add(TEST_JSON_SEARCH_STRING);

        ArrayList<JSONObject> expectedResult = new ArrayList<>();
        JSONObject matchJsonData = getJsonFromString(TEST_JSON_TREE_COMPLEX_NON_NESTED_SUBTREE_MATCH_OCCURENCE_ONE);
        expectedResult.add(matchJsonData);
        matchJsonData = getJsonFromString(TEST_JSON_TREE_COMPLEX_NON_NESTED_SUBTREE_MATCH_OCCURENCE_TWO);
        expectedResult.add(matchJsonData);

        ArrayList<JSONObject> matchSubtree = JsonUtility.extractTag(testTree,tagsToSearch);
        assertEquals(expectedResult.toString(),matchSubtree.toString());
    }

    @Test
    public void extractTag_matchFoundComplexSubTreeNested_shouldReturnMultipleMatchedSubtree(){
        JSONObject testTree = getJsonFromString(TEST_JSON_TREE_COMPLEX_NESTED_SUBTREE_MATCH);
        ArrayList<String> tagsToSearch = new ArrayList<>();
        tagsToSearch.add(TEST_JSON_SEARCH_STRING);

        ArrayList<JSONObject> expectedResult = new ArrayList<>();
        JSONObject matchJsonData = getJsonFromString(TEST_JSON_TREE_COMPLEX_NESTED_SUBTREE_MATCH_OCCURENCE_ONE);
        expectedResult.add(matchJsonData);
        matchJsonData = getJsonFromString(TEST_JSON_TREE_COMPLEX_NESTED_SUBTREE_MATCH_OCCURENCE_TWO);
        expectedResult.add(matchJsonData);

        ArrayList<JSONObject> matchSubtree = JsonUtility.extractTag(testTree,tagsToSearch);
        assertEquals(expectedResult.toString(),matchSubtree.toString());
    }

    @Test
    public void getTextContent_seenTagNeededNoMatchSubtree_shouldReturnEmptylist(){
        JSONObject testTree = getJsonFromString(TEST_JSON_TREE_SIMPLE_NO_MATCH);
        ArrayList<String> tagsToSearch = new ArrayList<>();
        tagsToSearch.add(TEST_JSON_SEARCH_STRING);

        ArrayList<String> textResult = JsonUtility.getTextContent(testTree,tagsToSearch,false);
        assertTrue(textResult.isEmpty());
    }

    @Test
    public void getTextContent_noSeenTagNeededNoMatchSubtree_shouldReturnAllTextInSubtree(){
        JSONObject testTree = getJsonFromString(TEST_JSON_TREE_SIMPLE_NO_MATCH);
        ArrayList<String> tagsToSearch = new ArrayList<>();
        tagsToSearch.add(TEST_JSON_SEARCH_STRING);

        ArrayList<String> expectedResult = new ArrayList<>();
        expectedResult.add(TEST_JSON_SEARCH_MATCH_STRING_TWO);

        ArrayList<String> textResult = JsonUtility.getTextContent(testTree,tagsToSearch,true);
        assertEquals(expectedResult.toString(),textResult.toString());
    }

    @Test
    public void getTextContent_seenTagNeededMatchSubtree_shouldReturnAppropiateSubtreeText(){
        JSONObject testTree = getJsonFromString(TEST_JSON_TREE_SIMPLE_DIRECT_FULL_MATCH);
        ArrayList<String> tagsToSearch = new ArrayList<>();
        tagsToSearch.add(TEST_JSON_SEARCH_STRING);

        ArrayList<String> expectedResult = new ArrayList<>();
        expectedResult.add(TEST_JSON_SEARCH_MATCH_STRING_TWO);

        ArrayList<String> textResult = JsonUtility.getTextContent(testTree,tagsToSearch,false);
        assertEquals(expectedResult.toString(),textResult.toString());
    }

    @Test
    public void getTextContent_noSeenTagNeededMatchSubtree_shouldReturnAllTextInSubtree(){
        JSONObject testTree = getJsonFromString(TEST_JSON_TREE_SIMPLE_DIRECT_FULL_MATCH);
        ArrayList<String> tagsToSearch = new ArrayList<>();
        tagsToSearch.add(TEST_JSON_SEARCH_STRING);

        ArrayList<String> expectedResult = new ArrayList<>();
        expectedResult.add(TEST_JSON_SEARCH_MATCH_STRING_TWO);

        ArrayList<String> textResult = JsonUtility.getTextContent(testTree,tagsToSearch,false);
        assertEquals(expectedResult.toString(),textResult.toString());
    }

    @Test
    public void getTextContext_seenTagNeededComplexTree_shouldReturnExactFullTree(){
        JSONObject testTree = getJsonFromString(TEST_JSON_TREE_COMPLEX_DIRECT_FULL_MATCH);
        ArrayList<String> tagsToSearch = new ArrayList<>();
        tagsToSearch.add(TEST_JSON_SEARCH_STRING);

        ArrayList<String> expectedResult = new ArrayList<>();
        expectedResult.add(TEST_JSON_SEARCH_MATCH_STRING_ONE);
        expectedResult.add(TEST_JSON_SEARCH_MATCH_STRING_TWO);
        expectedResult.add(TEST_JSON_SEARCH_MATCH_STRING_THREE);

        ArrayList<String> textResult = JsonUtility.getTextContent(testTree,tagsToSearch,false);
        assertEquals(expectedResult.toString(),textResult.toString());
    }

    @Test
    public void getTextContext_seenTagNeededComplexTree_shouldReturnExactSubTree(){
        JSONObject testTree = getJsonFromString(TEST_JSON_TREE_COMPLEX_DIRECT_SUBTREE_MATCH);
        ArrayList<String> tagsToSearch = new ArrayList<>();
        tagsToSearch.add(TEST_JSON_SEARCH_STRING);

        ArrayList<String> expectedResult = new ArrayList<>();
        expectedResult.add(TEST_JSON_SEARCH_MATCH_STRING_TWO);


        ArrayList<String> textResult = JsonUtility.getTextContent(testTree,tagsToSearch,false);
        assertEquals(expectedResult.toString(),textResult.toString());
    }

    @Test
    public void getTextContext_seenTagNeededComplexNonNestedTree_shouldReturnExactSubTree(){
        JSONObject testTree = getJsonFromString(TEST_JSON_TREE_COMPLEX_NON_NESTED_SUBTREE_MATCH);
        ArrayList<String> tagsToSearch = new ArrayList<>();
        tagsToSearch.add(TEST_JSON_SEARCH_STRING);

        ArrayList<String> expectedResult = new ArrayList<>();
        expectedResult.add(TEST_JSON_SEARCH_MATCH_STRING_TWO);
        expectedResult.add(TEST_JSON_SEARCH_MATCH_STRING_THREE);


        ArrayList<String> textResult = JsonUtility.getTextContent(testTree,tagsToSearch,false);
        assertEquals(expectedResult.toString(),textResult.toString());
    }

    @Test
    public void getTextContext_seenTagNeededComplexNestedTree_shouldReturnExactSubTree(){
        JSONObject testTree = getJsonFromString(TEST_JSON_TREE_COMPLEX_NESTED_SUBTREE_MATCH);
        ArrayList<String> tagsToSearch = new ArrayList<>();
        tagsToSearch.add(TEST_JSON_SEARCH_STRING);

        ArrayList<String> expectedResult = new ArrayList<>();
        expectedResult.add(TEST_JSON_SEARCH_MATCH_STRING_ONE);
        expectedResult.add(TEST_JSON_SEARCH_MATCH_STRING_TWO);
        expectedResult.add(TEST_JSON_SEARCH_MATCH_STRING_THREE);


        ArrayList<String> textResult = JsonUtility.getTextContent(testTree,tagsToSearch,false);
        assertEquals(expectedResult.toString(),textResult.toString());
    }
}