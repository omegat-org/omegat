package org.omegat.gui.issues;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.omegat.gui.issues.IssuesPanelController.ALL_TYPES;

public class IssuesTypeListModelTest {

    /**
     * Tests for the calculateData method in IssuesTypeListModel.
     * The method groups issues by their types and returns a list of map
     * entries including a summary for all types.
     */

    @Test
    public void testCalculateData_NoIssues() {
        // Test calculateData with no issues
        List<IIssue> issues = new ArrayList<>();
        IssuesTypeListModel model = new IssuesTypeListModel(issues);

        List<Map.Entry<String, Long>> result = model.calculateData(issues);

        assertEquals(1, result.size());
        assertEquals("All", result.get(0).getKey());
        assertEquals(Long.valueOf(0), result.get(0).getValue());
    }

    @Test
    public void testCalculateData_SingleType() {
        // Test calculateData with a single type of issues
        IIssue issue = mock(IIssue.class);
        when(issue.getTypeName()).thenReturn("Error");

        List<IIssue> issues = new ArrayList<>();
        issues.add(issue);

        IssuesTypeListModel model = new IssuesTypeListModel(issues);
        List<Map.Entry<String, Long>> result = model.calculateData(issues);

        assertEquals(2, result.size());
        assertEquals(ALL_TYPES, result.get(0).getKey());
        assertEquals(Long.valueOf(1), result.get(0).getValue());
        assertEquals("Error", result.get(1).getKey());
        assertEquals(Long.valueOf(1), result.get(1).getValue());
    }

    @Test
    public void testCalculateData_MultipleTypes() {
        // Test calculateData with multiple types of issues
        IIssue tagIssue = mock(IIssue.class);
        when(tagIssue.getTypeName()).thenReturn("Tag");

        IIssue ltIssue = mock(IIssue.class);
        when(ltIssue.getTypeName()).thenReturn("LanguageTool");

        List<IIssue> issues = new ArrayList<>();
        issues.add(tagIssue);
        issues.add(ltIssue);
        issues.add(tagIssue);

        IssuesTypeListModel model = new IssuesTypeListModel(issues);
        List<Map.Entry<String, Long>> result = model.calculateData(issues);

        assertEquals(3, result.size());
        assertEquals(ALL_TYPES, result.get(0).getKey());
        assertEquals(Long.valueOf(3), result.get(0).getValue());
        assertEquals("LanguageTool", result.get(1).getKey());
        assertEquals(Long.valueOf(1), result.get(1).getValue());
        assertEquals("Tag", result.get(2).getKey());
        assertEquals(Long.valueOf(2), result.get(2).getValue());
    }

    @Test
    public void testCalculateData_SortedOutput() {
        // Test calculateData with sorted output by type names
        IIssue issueA = mock(IIssue.class);
        when(issueA.getTypeName()).thenReturn("A");

        IIssue issueB = mock(IIssue.class);
        when(issueB.getTypeName()).thenReturn("B");

        IIssue issueC = mock(IIssue.class);
        when(issueC.getTypeName()).thenReturn("C");

        List<IIssue> issues = new ArrayList<>();
        issues.add(issueC);
        issues.add(issueA);
        issues.add(issueB);

        IssuesTypeListModel model = new IssuesTypeListModel(issues);
        List<Map.Entry<String, Long>> result = model.calculateData(issues);

        assertEquals(4, result.size());
        assertEquals(ALL_TYPES, result.get(0).getKey());
        assertEquals(Long.valueOf(3), result.get(0).getValue());
        assertEquals("A", result.get(1).getKey());
        assertEquals(Long.valueOf(1), result.get(1).getValue());
        assertEquals("B", result.get(2).getKey());
        assertEquals(Long.valueOf(1), result.get(2).getValue());
        assertEquals("C", result.get(3).getKey());
        assertEquals(Long.valueOf(1), result.get(3).getValue());
    }
}