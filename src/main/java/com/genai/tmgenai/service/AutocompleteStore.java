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

        detailsList.add(new AutoCompleteDetails("My premium is too high, can I get cheaper quotes?",FW,GENERAL));
        detailsList.add(new AutoCompleteDetails("What does NCB mean?",FW,GENERAL));
        detailsList.add(new AutoCompleteDetails("Why should I renew my policy?",HEALTH,GENERAL));
        detailsList.add(new AutoCompleteDetails("How much will I get on a policy claim?",HEALTH,GENERAL));
        detailsList.add(new AutoCompleteDetails("Why should I renew my policy?",COMMON,GENERAL));
        detailsList.add(new AutoCompleteDetails("How much will I get on a policy claim?",COMMON,GENERAL));
        detailsList.add(new AutoCompleteDetails("My premium is too high, can I get cheaper quotes?",TW,GENERAL));
        detailsList.add(new AutoCompleteDetails("What does NCB mean?",TW,GENERAL));
        detailsList.add(new AutoCompleteDetails("My premium is too high, can I get cheaper quotes?",CV,GENERAL));
        detailsList.add(new AutoCompleteDetails("What does NCB mean?",CV,GENERAL));
        this.detailsList = tmpDetailsList;
    }
    public List<AutoCompleteDetails> getDetailsList() {
        return List.copyOf(detailsList);
    }
}
