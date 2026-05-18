package com.ai.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JiraIssueRequest {

    private Fields fields;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Fields {

        private Project project;
        private String summary;
        private String description;
        private IssueType issuetype;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Project {

        private String key;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IssueType {

        private String id;
        private String name;
    }
}