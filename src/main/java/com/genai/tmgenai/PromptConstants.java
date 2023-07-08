package com.genai.tmgenai;

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


}
