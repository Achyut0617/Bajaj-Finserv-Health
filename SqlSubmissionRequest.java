package com.bajaj.qualifier.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SqlSubmissionRequest {

    @JsonProperty("finalQuery")
    private String finalQuery;

    public SqlSubmissionRequest() {}

    public SqlSubmissionRequest(String finalQuery) {
        this.finalQuery = finalQuery;
    }

    public String getFinalQuery()              { return finalQuery; }
    public void   setFinalQuery(String query)  { this.finalQuery = query; }
}
