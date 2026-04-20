package com.bajaj.qualifier.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookResponse {

    @JsonProperty("webhook")
    private String webhook;

    @JsonProperty("accessToken")
    private String accessToken;

    public WebhookResponse() {}

    public String getWebhook()              { return webhook; }
    public void   setWebhook(String w)      { this.webhook = w; }

    public String getAccessToken()          { return accessToken; }
    public void   setAccessToken(String t)  { this.accessToken = t; }

    @Override
    public String toString() {
        return "WebhookResponse{webhook='" + webhook + "', accessToken='" + accessToken + "'}";
    }
}
