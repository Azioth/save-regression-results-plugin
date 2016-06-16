package ar.com.azioth;

/**
 * Created by ssandri on 04/06/2016.
 */
public class TestSuite {

    private String suite_name;
    private String suite_start;
    private String suite_finish;
    private String suite_tested_url;
    private String suite_passed;
    private String suite_failed;
    private String suite_skipped;
    private String suite_total;
    private TestCase suite_tests;


    public TestSuite(String suite_name, String suite_start, String suite_finish, String suite_tested_url, String suite_passed, String suite_failed, String suite_skipped, String suite_total) {
        this.suite_name = suite_name;
        this.suite_start = suite_start;
        this.suite_finish = suite_finish;
        this.suite_tested_url = suite_tested_url;
        this.suite_passed = suite_passed;
        this.suite_failed = suite_failed;
        this.suite_skipped = suite_skipped;
        this.suite_total = suite_total;
    }

    public void setTestCase(TestCase suiteTestCase) {
        this.suite_tests = suiteTestCase;
    }
}
