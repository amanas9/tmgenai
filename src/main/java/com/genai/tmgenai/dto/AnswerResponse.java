package com.genai.tmgenai.dto;

import lombok.Data;

@Data
public class AnswerResponse {
    private Answer answer;
    private String vertical;

    private String fileId;

}
