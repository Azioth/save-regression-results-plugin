package com.indemand.automation;

import java.util.LinkedList;

/**
 * Created by ssandri on 04/06/2016.
 */
public class TestCase {

    private String test_name;
    private String test_duration;
    private String test_start;
    private String test_finish;
    private LinkedList<TestMethod> test_methods = new LinkedList();

    public TestCase(String test_name, String test_duration, String test_start, String test_finish) {
        this.test_name = test_name;
        this.test_duration = test_duration;
        this.test_start = test_start;
        this.test_finish = test_finish;
    }

    public void addTestMethod(TestMethod inputTestMethod) {
        test_methods.add(inputTestMethod);
    }
}
