package com.genai.tmgenai.dto;

import com.genai.tmgenai.service.AutocompleteStore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class AutoCompleteDetails {

    public enum CATEGORY{
        GENERAL, RENEWAL, CLAIM;
    }
    public enum VERTICAL{
        FW, TW, CV, HEALTH, LIFE, COMMON
    };

    String question;



    VERTICAL vertical;

    CATEGORY category;
    public AutoCompleteDetails(String question, VERTICAL vertical, CATEGORY category){
        this.category = category;
        this.vertical = vertical;
        this.question = question;
    }

}
