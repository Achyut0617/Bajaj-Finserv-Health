package com.bajaj.qualifier.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// ── Request body sent to generateWebhook ──────────────────────────────────────
public class WebhookRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("regNo")
    private String regNo;

    @JsonProperty("email")
    private String email;

    public WebhookRequest() {}

    public WebhookRequest(String name, String regNo, String email) {
        this.name  = name;
        this.regNo = regNo;
        this.email = email;
    }

    public String getName()            { return name; }
    public void   setName(String n)    { this.name = n; }

    public String getRegNo()           { return regNo; }
    public void   setRegNo(String r)   { this.regNo = r; }

    public String getEmail()           { return email; }
    public void   setEmail(String e)   { this.email = e; }
}
