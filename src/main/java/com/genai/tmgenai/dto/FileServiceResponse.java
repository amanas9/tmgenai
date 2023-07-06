package com.genai.tmgenai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FileServiceResponse {
    @JsonProperty("processInfo")
    private FileResponseMeta fileResponseMeta;
    private String summary;
}

