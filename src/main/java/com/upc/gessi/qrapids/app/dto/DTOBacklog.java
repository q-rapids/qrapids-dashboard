package com.upc.gessi.qrapids.app.dto;

public class DTOBacklog {

    private String issue_url;
    private String issue_id;

    public DTOBacklog(String issue_url, String issue_id) {
        this.issue_url = issue_url;
        this.issue_id = issue_id;
    }

    public String getIssue_url() {
        return issue_url;
    }

    public void setIssue_url(String issue_url) {
        this.issue_url = issue_url;
    }

    public String getIssue_id() {
        return issue_id;
    }

    public void setIssue_id(String issue_id) {
        this.issue_id = issue_id;
    }
}
