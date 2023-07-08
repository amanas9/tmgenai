package com.genai.tmgenai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Data
public class MotorPremiumRequest  {

    private static final Logger LOG = LoggerFactory.getLogger("motor-premium-request");

    private int businessType;

    private String vehicleId;

    private String rtoId;

    private String rtoDesp;

    private String requestId;

    String make;

    String model;

    String fuel;

    String variant;

    private String registrationNo;

    private String customerName;

    private String customerFirstName;

    private String customerLastName;

    String registrationDate;

    String isPaid = "No";

    String type = "async";

    private String userStateCode;

    private String vertical;

    private String utmSource = "(direct)";

    private String utmMedium = "(none)";

    String previousInsurer;

    private boolean prevClaim;


    private boolean expiryInTwoDays;

    private String prevPolicyNo;

    private String previousPolicyType;

    private boolean eligibleForNCB;


    private boolean unknownNCBFlag;

     private int prevNCB;

    private String applicableNCB;

    private String quoteId;

    private String policyType;

    private Double idv;


    private Boolean renewalDetailsModified;

    private String prevPolicyCopy;

    private Comprehensive comprehensive = new Comprehensive();

    private TP tp = new TP();

    private OD od = new OD();
}

@Data
class Comprehensive extends AddOns {
    Comprehensive(List<String> addOns) {
        super(addOns);
    }

    public Comprehensive() {
        super();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}

@Data

class TP extends AddOns {
    TP(List<String> addOns) {
        super(addOns);
    }

    public TP() {
        super();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}

@Data
class OD extends AddOns {
    OD (List<String> addOns) {
        super(addOns);
    }

    public OD() {
        super();
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
@Data
abstract class AddOns {
    private Integer paOwnerDriver = 0;
    private Boolean zeroDep = false;
    private Boolean rsa = false;
    private Boolean engineProtect = false;
    private Boolean ncbProtect = false;
    private Boolean antiTheft = false;
    private Boolean secureTowing = false;
    private Boolean tyreSecure = false;
    private Boolean emergencyTransportAndHotelExpense = false;
    private Boolean repairOfGlassRubberPlasticParts = false;
    private Boolean consumableCover = false;
    private Boolean fullInvoiceCover = false;
    private Boolean keyReplacement = false;
    private Boolean fibretank = false;
    private Boolean geographicExt = false;
    private Boolean dailyCashAllowance = false;
    private Boolean personalBaggage = false;
    private Boolean drivingTuitionCoverage = false;
    private Boolean accidentalHospitalization = false;
    private Boolean hydrostaticLockCover = false;
    private Boolean imt23 = false;
    private Boolean tppd = false;

    public AddOns() {

    }
    public AddOns(List<String> addOns) {
        if (addOns != null) {
            for (int i = 0; i < addOns.size(); i++) {
                var addOn = addOns.get(i);

                if (addOn.equalsIgnoreCase("Zero Depreciation")) {
                    this.zeroDep = true;
                } else if (addOn.equalsIgnoreCase("NCB Protect")) {
                    this.ncbProtect = true;
                } else if (addOn.equalsIgnoreCase("Engine Protect")) {
                    this.engineProtect = true;
                } else if (addOn.equalsIgnoreCase("Road Side Assistance")) {
                    this.rsa = true;
                } else if (addOn.equalsIgnoreCase("Key and Lock Replacement")) {
                    this.keyReplacement = true;
                } else if (addOn.equalsIgnoreCase("IMT 23")) {
                    this.imt23 = true;
                } else if (addOn.equalsIgnoreCase("Secure Towing")) {
                    this.secureTowing = true;
                } else if (addOn.equalsIgnoreCase("Tyre Secure")) {
                    this.tyreSecure = true;
                } else if (addOn.equalsIgnoreCase("Emergency Transport And Hotel Expense")) {
                    this.emergencyTransportAndHotelExpense = true;
                } else if (addOn.equalsIgnoreCase("Repair of Glass Rubber Plastic Parts")) {
                    this.repairOfGlassRubberPlasticParts = true;
                } else if (addOn.equalsIgnoreCase("Consumable Cover")) {
                    this.consumableCover = true;
                } else if (addOn.equalsIgnoreCase("Return to Invoice")) {
                    this.fullInvoiceCover = true;
                } else if (addOn.equalsIgnoreCase("Fibretank")) {
                    this.fibretank = true;
                } else if (addOn.equalsIgnoreCase("Geographic Ext")) {
                    this.geographicExt = true;
                } else if (addOn.equalsIgnoreCase("Daily Cash Allowance")) {
                    this.dailyCashAllowance = true;
                } else if (addOn.equalsIgnoreCase("Personal Baggage")) {
                    this.personalBaggage = true;
                } else if (addOn.equalsIgnoreCase("Driving Tuition Coverage")) {
                    this.drivingTuitionCoverage = true;
                } else if (addOn.equalsIgnoreCase("Accidental Hospitalization")) {
                    this.accidentalHospitalization = true;
                } else if (addOn.equalsIgnoreCase("Hydrostatic Lock Cover")) {
                    this.hydrostaticLockCover = true;
                } else if (addOn.equalsIgnoreCase("Third Party Property Damage")) {
                    this.tppd = true;
                }
            }
        }
    }
}
