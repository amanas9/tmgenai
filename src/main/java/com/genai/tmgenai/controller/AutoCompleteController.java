package com.genai.tmgenai.controller;

import com.genai.tmgenai.dto.AutoCompleteDetails;
import com.genai.tmgenai.dto.Question;
import com.genai.tmgenai.service.AutocompleteStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;

@RestController()
@RequestMapping("api/v1/autocomplete/")
public class AutoCompleteController {

    @Autowired
    AutocompleteStore autocompleteStore;

    @GetMapping("/suggestions")
    public ResponseEntity<Object> chat(@RequestParam String prefix, @RequestParam AutoCompleteDetails.VERTICAL vertical) throws URISyntaxException, IOException {
        return ResponseEntity.ok(autocompleteStore.getAutocomplete().giveSuggestions(prefix, vertical));
    }
}
