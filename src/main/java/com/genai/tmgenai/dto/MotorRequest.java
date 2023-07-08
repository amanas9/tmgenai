package com.genai.tmgenai.dto;

import lombok.Data;
import lombok.ToString;

@Data
class UTMParams {
    private String utmSource = "(direct)";
    private String utmMedium = "(none)";

    public UTMParams() {

    }
}
@ToString
@Data
public class MotorRequest {
    private String _id = "123456780";
    private String requestType = "INITIAL";
    private String quoteId;
    private Boolean isAsync = true;
    private String vertical;
    private String userMobile = "";
    private String customerName = "Mr test";
    private String stateCode;
    private String policyType = "comprehensive";
    private boolean initialReqFlag = true;
    private UTMParams utmParams = new UTMParams();
    private MotorPremiumRequest motorPremiumRequest;
    private Boolean isRenewal = true;
    private String expiryDt = "12/12/2023";
    private String tenant = "turtlemint";
    private String clientId="5c90df45e4b0f2135296ddd7";
}
