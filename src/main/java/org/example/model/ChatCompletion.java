package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.model.Choice;
import org.example.model.Usage;

import java.util.List;

public class ChatCompletion {
    @JsonProperty("id")
    public String id;
    @JsonProperty("object")
    public String object;
    @JsonProperty("created")
    public long created;
    @JsonProperty("model")
    public String model;
    @JsonProperty("choices")
    public List<Choice> choices;
    @JsonProperty("usage")
    public Usage usage;
    @JsonProperty("system_fingerprint")
    public String systemFingerprint;
}
