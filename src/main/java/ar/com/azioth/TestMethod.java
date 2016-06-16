package ar.com.azioth;

/**
 * Created by ssandri on 04/06/2016.
 */
public class TestMethod {
    private String testmethod_name;
    private String testmethod_class;
    private String testmethod_result;
    private String testmethod_analysis;
    private String testmethod_exception;
    private String testmethod_ex_message;
    private String testmethod_duration;
    private String testmethod_start;
    private String testmethod_finish;
    private String testmethod_bug_url;

    public TestMethod(String testmethod_name, String testmethod_class, String testmethod_result, String testmethod_duration, String testmethod_start, String testmethod_finish) {
        this.testmethod_name = testmethod_name;
        this.testmethod_class = testmethod_class;
        this.testmethod_result = testmethod_result;
        this.testmethod_analysis = "NOT ANALYZED";
        this.testmethod_duration = testmethod_duration;
        this.testmethod_start = testmethod_start;
        this.testmethod_finish = testmethod_finish;
        this.testmethod_exception = "";
        this.testmethod_ex_message = "";
        this.testmethod_bug_url = "";
    }

    public void setException(String testmethod_exception) {
        this.testmethod_exception = testmethod_exception;
    }

    public void setExceptionMessage(String testmethod_ex_message) {
        this.testmethod_ex_message = testmethod_ex_message;
    }
}
