package ar.com.azioth;

import com.google.gson.Gson;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Created by ssandri on 04/06/2016.
 */
@Mojo( name = "submit-results-plugin", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST )
public class SaveRegressionResultsPlugin extends AbstractMojo {

    @Parameter(defaultValue = "http://localhost:8000/", property = "korribanEndpoint", required = true)
    protected String korribanEndpoint;
    @Parameter (defaultValue = "https://qamgmt01.indemandterp.com/", property = "clarityEnvironment", required = true)
    protected String clarityEnvironment;
    @Parameter (defaultValue = "${project.build.directory}/surefire-reports/testng-results.xml", property = "testNGResults", required = true)
    protected String testNGResults;
    private Document doc;
    private TestCase testCase;
    private TestSuite testSuite;
    private String requestBody;

    public void execute() throws MojoExecutionException, MojoFailureException {

        parseXML();
        getTestCaseInfo();
        getTestMethodsInfo();
        getTestSuiteInfo();
        createBodyRequest();
        saveResults();

    }

    private void saveResults() {
        try{
            URL korribanSuitesResource = new URL(korribanEndpoint + "korriban-api/v1/suites");
            byte[] outputInBytes = requestBody.getBytes("UTF-8");
            HttpURLConnection httpURLConnection = (HttpURLConnection) korribanSuitesResource.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type","application/json");
            httpURLConnection.setDoOutput(true);

            DataOutputStream outputStream = new DataOutputStream(httpURLConnection.getOutputStream());
            outputStream.write(outputInBytes);
            outputStream.flush();
            outputStream.close();
            System.out.println(httpURLConnection.getHeaderField("Content-Location"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createBodyRequest() {
        // Convert TestSuite Object to JSON String
        Gson gson = new Gson();
        requestBody = gson.toJson(testSuite);
        System.out.println(requestBody);
    }


    private void getTestSuiteInfo() {
        // Obtain Test Suite Info
        Element testngResultsNode = doc.getDocumentElement();
        Node suiteNode = doc.getElementsByTagName("suite").item(0);
        // Suite name
        String suiteName = suiteNode.getAttributes().getNamedItem("name").getNodeValue();
        // Amount of passed test methods
        String suitePassed = testngResultsNode.getAttribute("passed");
        // Amount of failed test methods
        String suiteFailed = testngResultsNode.getAttribute("failed");
        // Amount of skipped test methods
        String suiteSkipped = testngResultsNode.getAttribute("skipped");
        // Amount of total test methods
        String suiteTotal = testngResultsNode.getAttribute("total");
        // Format suite starting date time from ISO-8601 to PST
        LocalDateTime testSuiteStartedAt = LocalDateTime.ofInstant(Instant.parse(suiteNode.getAttributes().getNamedItem("started-at").getNodeValue()), ZoneId.of("America/Los_Angeles"));
        String testSuiteStartedAt_formatted = testSuiteStartedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        // Format suite finishing date time from ISO-8601 to PST
        LocalDateTime testSuiteFinishedAt = LocalDateTime.ofInstant(Instant.parse(suiteNode.getAttributes().getNamedItem("finished-at").getNodeValue()), ZoneId.of("America/Los_Angeles"));
        String testSuiteFinishedAt_formatted = testSuiteFinishedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        // Create TestSuite Object
        testSuite = new TestSuite(suiteName, testSuiteStartedAt_formatted, testSuiteFinishedAt_formatted, clarityEnvironment,
                suitePassed, suiteFailed, suiteSkipped, suiteTotal);
        // Add TestCase to TestSuite Object
        testSuite.setTestCase(testCase);
    }

    private void getTestCaseInfo() {
        // Obtain Test Case Info
        Node testNode = doc.getElementsByTagName("test").item(0);
        // Test case Name
        String testCaseName = testNode.getAttributes().getNamedItem("name").getNodeValue();
        // Test case Duration
        String testCaseDuration = testNode.getAttributes().getNamedItem("duration-ms").getNodeValue();
        // Obtain and format test case starting date time from ISO-8601 to PST
        LocalDateTime testCaseStartedAt = LocalDateTime.ofInstant(Instant.parse(testNode.getAttributes().getNamedItem("started-at").getNodeValue()), ZoneId.of("America/Los_Angeles"));
        String testCaseStartedAt_formatted = testCaseStartedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        // Obtain and format test case finishing date time from ISO-8601 to PST
        LocalDateTime testCaseFinishedAt = LocalDateTime.ofInstant(Instant.parse(testNode.getAttributes().getNamedItem("finished-at").getNodeValue()), ZoneId.of("America/Los_Angeles"));
        String testCaseFinishedAt_formatted = testCaseFinishedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        // Create TestCase Object
        testCase = new TestCase(testCaseName, testCaseDuration, testCaseStartedAt_formatted, testCaseFinishedAt_formatted);
    }

    private void getTestMethodsInfo() {
        // Obtain Test Case Info
        Node testNode = doc.getElementsByTagName("test").item(0);
        // Obtain Test Method Info
        NodeList classesList = testNode.getChildNodes();
        for (int i = 0; i < classesList.getLength(); i++){
            Node classNodeItem = classesList.item(i);
            // getChildNodes also includes test and comments, in order to avoid null pointers, we need to verify that the node is a class
            if (classNodeItem.getNodeName().equals("class")){
                // Obtain Test method Class name
                String className = classNodeItem.getAttributes().getNamedItem("name").getNodeValue();
                NodeList testMethodsList = classNodeItem.getChildNodes();
                for (int j = 0; j <  testMethodsList.getLength(); j++){
                    Node testMethodNode = testMethodsList.item(j);
                    // getChildNodes also includes test and comments, in order to avoid null pointers, we need to verify that the node is a test-method
                    if (testMethodNode.getNodeName().equals("test-method")){
                        NamedNodeMap testMethodAtrributes = testMethodNode.getAttributes();
                        // If the Node does not have an 'is-config' attribute is a valid test-method
                        if (testMethodAtrributes.getNamedItem("is-config") == null) {
                            // Obtain Test method nname
                            String testMethodName = testMethodAtrributes.getNamedItem("name").getNodeValue();
                            // Obtain Test method result
                            String testMethodResult = testMethodAtrributes.getNamedItem("status").getNodeValue();
                            // Obtain Test method duration
                            String testMethodDuration = testMethodAtrributes.getNamedItem("duration-ms").getNodeValue();
                            // Obtain and format Test method starting date time from ISO-8601 to PST
                            LocalDateTime testMethodStartedAt = LocalDateTime.ofInstant(Instant.parse(testMethodAtrributes.getNamedItem("started-at").getNodeValue()), ZoneId.of("America/Los_Angeles"));
                            String testMethodStartedAt_formatted = testMethodStartedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            // Obtain and format Test method finishing date time from ISO-8601 to PST
                            LocalDateTime testMethodFinishedAt = LocalDateTime.ofInstant(Instant.parse(testMethodAtrributes.getNamedItem("finished-at").getNodeValue()), ZoneId.of("America/Los_Angeles"));
                            String testMethodFinishedAt_formatted = testMethodFinishedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            // Create TestMethod Object
                            TestMethod testMethod = new TestMethod(testMethodName, className, testMethodResult, testMethodDuration, testMethodStartedAt_formatted, testMethodFinishedAt_formatted);
                            // Add TestMethod to TestCase LinkedList of TestMethods
                            testCase.addTestMethod(testMethod);
                        }
                    }
                }
            }
        }
    }

    private void parseXML() {
        try {
            File inputFile = new File(testNGResults);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
