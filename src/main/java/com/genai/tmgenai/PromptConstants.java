package com.genai.tmgenai;

import dev.langchain4j.data.document.DocumentSegment;

public class PromptConstants {
    public static final String PROMPT_FOR_INITIAL_SUMMARY_MOTOR =  "your task is to generate a summary about a policy from the information from a policy document\n\n"
            + "Summarize the information in a concise way, delimited by triple backticks in the form of points. You have to use values vertical,make, model,variant,policy number,insurance company name, registration_number, engine_number, chassis_number, rto, customer_info,issuance_date, add_ons,total_premium, issuance_date, customer_email, customer_mobile, manufacturing_year,cubic_capacity,policy type, policy name, customer name ,date of birth, gender, benefits, issuance date, period of insurance in the summary if and only if they are available in the information below. use not more than 10 lines\n";

    public static final String PROMPT_FOR_INITIAL_SUMMARY_HEALTH =  "your task is to generate a summary about a policy from the information from a policy document\n\n"
            + "Summarize the information in a concise way, delimited by triple backticks in the form of points. You have to use values vertical,policy number,insurance company name, , customer_info,issuance_date, add_ons,total_premium, issuance_date, customer_email, customer_mobile, ,policy type, policy name, customer name ,date of birth, gender, benefits, issuance date, period of insurance in the summary if and only if they are available in the information below. use not more than 10 lines\n";

    public static final String PROMPT_FOR_GETTING_VERTICAL = "determine the value of vertical as json key it's value by following below steps from information below delimited by triple backticks:\n"
            + "1.determine  if the given information is about health policy or motor policy\n"
            + "2.if it is health policy, then the vertical will be HEALTH\n"
            + "3.if it is a motor policy, then vertical will be TW if the make model variant belongs to a two wheeler, FW if it belongs to four wheeler and CV if belongs to commercial vehicle\n"
            + "/n"
            + "give the output only as a json object with vertical as key\n";



//    public static final String PROMPT_FOR_GETTING_PREQEST =
//            "Based on the below information, determine the value of fuel type. It's value can be petrol, diesel, cng, lpg, electric give the output only as a json object with fuel as key.\n"+
//            "determine the value of vehicle make. It's value can be any vehicle manufacturers in india. give the output only as a json object with make as key.\n"+
//            "determine the value of vehicle model. It's value is usally mentioned along with the title model. give the output only as a json object with model as key.\n"+
//            "determine the value of vehicle variant. It's value is usally mentioned along with the title variant. give the output only as a json object with variant as key.\n"+
//            "determine the value of registration number. give the output as a json object with registration_number as key.\n"+
//            "determine the value of manufacturing year of the vehicle. give the output as a json object with manufacturing_year as key.\n";
//
    //make, mdoel , year, variant, fuel, regisration number

    public static final String PROMPT_FOR_GETTING_PREQEST =
            "Based on the below information, determine value of fuel, make, model, variant, manufacturing year(give in YYYY format) and registration number. Return the output as a json object with the keys fuel,make,model,variant,year,registration_number.\n";

}
