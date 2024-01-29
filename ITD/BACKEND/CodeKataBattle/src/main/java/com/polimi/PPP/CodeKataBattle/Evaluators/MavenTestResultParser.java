package com.polimi.PPP.CodeKataBattle.Evaluators;
import javax.xml.parsers.*;

import com.polimi.PPP.CodeKataBattle.Exceptions.ErrorInParsingMavenResultsException;
import org.w3c.dom.*;
import java.io.File;

import com.polimi.PPP.CodeKataBattle.Exceptions.MavenBuildFailedException;

public class MavenTestResultParser {

    public Float parseTestResults(String reportsDirectoryPath) throws MavenBuildFailedException, ErrorInParsingMavenResultsException {
        File reportsDir = new File(reportsDirectoryPath);
        File[] reportFiles = reportsDir.listFiles((dir, name) -> name.endsWith("SolutionTest.xml"));

        if (reportFiles != null) {
            for (File report : reportFiles) {
                return parseReportFile(report);
            }
        }
        throw new MavenBuildFailedException("No reports found");
    }

    private Float parseReportFile(File report) throws ErrorInParsingMavenResultsException {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(report);
            doc.getDocumentElement().normalize();

            NodeList testSuiteNodes = doc.getElementsByTagName("testsuite");
            for (int i = 0; i < testSuiteNodes.getLength(); i++) {
                Node testSuiteNode = testSuiteNodes.item(i);

                if (testSuiteNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element testSuiteElement = (Element) testSuiteNode;
                    float tests = Float.parseFloat(testSuiteElement.getAttribute("tests"));
                    float failures = Float.parseFloat(testSuiteElement.getAttribute("failures"));
                    float errors = Float.parseFloat(testSuiteElement.getAttribute("errors"));
                    float skipped = Float.parseFloat(testSuiteElement.getAttribute("skipped"));

                    //System.out.println("Total tests: " + tests + ", failed: " +failures);

                    return 1f - (failures + errors + skipped) / tests;
                }
            }
        } catch (Exception e) {
            throw new ErrorInParsingMavenResultsException("Error in parsing maven results");
        }

        throw new ErrorInParsingMavenResultsException("Error in parsing maven results");
    }
}
