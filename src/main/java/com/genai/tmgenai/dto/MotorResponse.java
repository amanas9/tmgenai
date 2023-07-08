package com.genai.tmgenai.dto;

import lombok.Data;


@Data
public class MotorResponse {
    private String status;
    private MotorResponseData data;
    private String timestamp;

    public String getQuoteId() {

        if (this.data != null && this.data.getPremiumRequest() != null)
            return this.data.getPremiumRequest().getQuoteId();

        return null;
    }
}

