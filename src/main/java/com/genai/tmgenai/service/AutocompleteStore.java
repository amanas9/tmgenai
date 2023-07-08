package com.genai.tmgenai.service;

import com.genai.tmgenai.dto.AutoCompleteDetails;

import java.util.ArrayList;
import java.util.List;

import com.genai.tmgenai.dto.Autocomplete;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.genai.tmgenai.dto.AutoCompleteDetails.CATEGORY.*;
import static com.genai.tmgenai.dto.AutoCompleteDetails.VERTICAL.*;


@Slf4j
@Service
public class AutocompleteStore {


    private Autocomplete autocomplete;

    public Autocomplete getAutocomplete() {
        return autocomplete;
    }

    private List<AutoCompleteDetails> detailsList = new ArrayList<>();


    public AutocompleteStore()
    {
        loadDetails();
        initializeStore(detailsList);
    }

    private void initializeStore(List<AutoCompleteDetails> detailsList) {

        autocomplete = new Autocomplete();

        for(AutoCompleteDetails autoCompleteDetails : detailsList)
        {
            autocomplete.insert(autoCompleteDetails.getQuestion(), autoCompleteDetails);
        }

        log.error("loaded questionaries. size : {}", autocomplete.getSize());
    }

    public void addQuestion(String question, AutoCompleteDetails.VERTICAL vertical, boolean suggestion)
    {
        if (suggestion==true) {
            AutoCompleteDetails newQuestion = new AutoCompleteDetails(question, vertical, GENERAL);
            autocomplete.insert(newQuestion.getQuestion(), newQuestion);
            detailsList.add(newQuestion);
        }
    }

    private void loadDetails() {

        ArrayList tmpDetailsList = new ArrayList<>();

        tmpDetailsList.add(new AutoCompleteDetails("What is the registration number?", FW, GENERAL));
        tmpDetailsList.add(new AutoCompleteDetails("how to submit claim?", FW, GENERAL));
        tmpDetailsList.add(new AutoCompleteDetails("how to claim?", FW, GENERAL));
        tmpDetailsList.add(new AutoCompleteDetails("give me benefits.", FW, GENERAL));
        tmpDetailsList.add(new AutoCompleteDetails("What are terms & condition.", FW, GENERAL));
        tmpDetailsList.add(new AutoCompleteDetails("What is expiry date?", FW, GENERAL));
        tmpDetailsList.add(new AutoCompleteDetails("explain tax benefits.", LIFE, GENERAL));
        tmpDetailsList.add(new AutoCompleteDetails("what is cover amount", LIFE, GENERAL));

        this.detailsList = tmpDetailsList;
    }
    public List<AutoCompleteDetails> getDetailsList() {
        return List.copyOf(detailsList);
    }
}
