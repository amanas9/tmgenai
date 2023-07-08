package com.genai.tmgenai.dto;

import lombok.Data;

@Data
class UTMParams {
    private String utmSource = "(direct)";
    private String utmMedium = "(none)";

    public UTMParams() {

    }
}
public class MotorRequest {
    private String _id;
    private String requestType;
    private String quoteId;
    private boolean isAsync;
    private String vertical;
    private String userMobile;
    private String customerName;
    private String stateCode;
    private String policyType;
    private boolean initialReqFlag;
    private UTMParams utmParams;
    private MotorPremiumRequest motorPremiumRequest;
    private Boolean isRenewal = true;
    private String expiryDt;
    private String tenant = "turtlemint";
    private String clientId;
}
