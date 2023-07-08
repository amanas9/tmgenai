package com.genai.tmgenai.dto;

import lombok.Data;

@Data
public class MotorResponseData {
    private PremiumRequestResponse premiumRequest;
    private boolean valid;
    private boolean renewalsValid;
}