package com.genai.tmgenai;

public class PromptConstants {
    public static final String PROMPT_FOR_INITIAL_SUMMARY =  "your task is to generate a summary about a policy from the information from a policy document\n\n"
            + "Summarize the information in a concise way, delimited by triple backticks in the form of points. You have to use values vertical,make, model,variant,policy number,insurer, registration_number, engine_number, chassis_number, rto, customer_info,issuance_date, add_ons,total_premium, issuance_date, customer_email, customer_mobile, manufacturing_year,cubic_capacity,policy type, policy name, customer name ,date of birth, gender, benefits, issuance date, period of insurance in the summary if and only if they are available in the information below. use not more than 10 lines\n";


}
