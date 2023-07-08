package com.genai.tmgenai.controller;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.genai.tmgenai.dto.AutoCompleteDetails;
import com.genai.tmgenai.dto.Question;
import com.genai.tmgenai.service.AutocompleteStore;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Data;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/api/v1/autocomplete")
public class AutoCompleteController {

    @Autowired
    AutocompleteStore autocompleteStore;

    @GetMapping("/suggestions")
    public ResponseEntity<Object> chat(@RequestParam String prefix, @RequestParam AutoCompleteDetails.VERTICAL vertical) throws URISyntaxException, IOException {
        return ResponseEntity.ok(autocompleteStore.getAutocomplete().giveSuggestions(prefix, vertical));
    }


//    @RequestBody
//    @GetMapping(value = "/getQuotes", produces =  MediaType.APPLICATION_JSON_VALUE)
//    public JSONArray getQuotes()
//    {
//
//        System.out.println("hello");
//        JSONArray jsonArray = new JSONArray();
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("insurer",new String("HDFC"));
//        jsonObject.put("premium",new String("14000"));
//        jsonObject.put("cashless",new String("available"));
//        jsonObject.put("ncb",new String("0"));
//        jsonObject.put("cover_amount",new String("180000"));
//        jsonObject.put("logo",new String("https://d5ng0zjhhq362.cloudfront.net/images/HDFC.png"));
//        jsonObject.put("paymentLink",new String("https://www.hdfcergo.com/renew-hdfc-ergo-policy"));
//        jsonArray.put(jsonObject);
//        System.out.println(jsonArray);
//        return jsonArray;
//    }

    @GetMapping("/getQuotes")
    public ResponseEntity<Object> getQuotes()
    {
        List<Quote> list = new ArrayList<>();

        Quote quote = new Quote();

        quote.setInsurer("ICICI");
        quote.setPaymentLink("https://www.icicilombard.com/renew-policy-online#/AllRenewal");
        quote.setLogo("https://d5ng0zjhhq362.cloudfront.net/images/ICICILOMBARD.png");
        quote.setCashless("available");
        quote.setNcb("0");
        quote.setPremium("12,130");
        quote.setCover_amount("225,000");
        list.add(quote);

        quote = new Quote();
        quote.setInsurer("HDFC");
        quote.setPaymentLink("https://www.hdfcergo.com/renew-hdfc-ergo-policy");
        quote.setLogo("https://d5ng0zjhhq362.cloudfront.net/images/HDFC.png");
        quote.setCashless("available");
        quote.setNcb("0");
        quote.setPremium("12,010");
        quote.setCover_amount("220,000");

        list.add(quote);

        quote = new Quote();

        quote.setInsurer("BAJAJ");
        quote.setPaymentLink("https://www.bajajallianz.com/renewal/four-wheeler-motor-renewal/commercial-vehicle-renewal.html");
        quote.setLogo("https://d5ng0zjhhq362.cloudfront.net/images/BAJAJ.png");
        quote.setCashless("not available");
        quote.setNcb("0");
        quote.setPremium("10,531");
        quote.setCover_amount("190,000");
        list.add(quote);

        quote = new Quote();
        quote.setInsurer("STAR");
        quote.setPaymentLink("https://web.starhealth.in/customerportal/instant-renewal/");
        quote.setLogo("https://d5ng0zjhhq362.cloudfront.net/images/STAR.png");
        quote.setCashless("available");
        quote.setNcb("0");
        quote.setPremium("11,130");
        quote.setCover_amount("180,000");
        list.add(quote);

        quote = new Quote();
        quote.setInsurer("Aditya Birla");
        quote.setPaymentLink("https://www.adityabirlacapital.com/healthinsurance/buy-insurance-online/#!/renewal-renew-policy");
        quote.setLogo("https://d5ng0zjhhq362.cloudfront.net/images/ADITYABIRLAHEALTH.png");
        quote.setCashless("available");
        quote.setNcb("0");
        quote.setPremium("11,500");
        quote.setCover_amount("180,000");
        list.add(quote);

        quote = new Quote();
        quote.setInsurer("Royal Sundaram");
        quote.setPaymentLink("https://dtcapplive.royalsundaram.in/instarenew/home\n");
        quote.setLogo("https://d5ng0zjhhq362.cloudfront.net/images/royalsundaram.png");
        quote.setCashless("available");
        quote.setNcb("0");
        quote.setPremium("11,200");
        quote.setCover_amount("172,000");

        list.add(quote);
        return ResponseEntity.ok(list);
    }


}
@Data
class InsurerQuotes
{
    List<Quote> quoteList;
}
@Data class Quote
{
    String insurer;
    String paymentLink;
    String cover_amount;
    String cashless;
    String ncb;
    String logo;
    String premium;
}
