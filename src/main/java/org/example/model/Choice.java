package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Choice {
    @JsonProperty("index")
    public int index;
    @JsonProperty("message")
    public Message message;
    @JsonProperty("logprobs")
    public Object logprobs;
    @JsonProperty("finish_reason")
    public String finishReason;
}
