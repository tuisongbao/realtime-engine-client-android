package com.tuisongbao.engine.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Created by user on 15-8-18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Response {

    @JsonIgnore
    public static final String ERROR = "error";

    @JsonIgnore
    public static final String OK = "ok";

    @JsonProperty("status")
    public String status;

    @JsonProperty("message")
    public String message;

    @JsonProperty("time")
    public Long time;

    public boolean ok() {
        return status != null && status.equalsIgnoreCase(Response.OK);
    }
}
