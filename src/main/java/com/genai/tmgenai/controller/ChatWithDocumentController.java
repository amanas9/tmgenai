package com.genai.tmgenai.controller;

import com.genai.tmgenai.dto.FileServiceResponse;
import com.genai.tmgenai.dto.Question;
import com.genai.tmgenai.service.ChatDocumentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;

@RestController()
@RequestMapping("api/v1/")
public class ChatWithDocumentController {

    private final ChatDocumentService chatDocumentService;

    @Autowired
    public ChatWithDocumentController(ChatDocumentService chatDocumentService) {
        this.chatDocumentService = chatDocumentService;
    }

    @PostMapping("document")
    public ResponseEntity<Object> uploadPolicyDocument( @RequestParam("file") MultipartFile file) throws URISyntaxException, IOException {
        FileServiceResponse fileServiceResponse = chatDocumentService.uploadFile(file);
        if(fileServiceResponse == null){
            return ResponseEntity.internalServerError().body("Error in uploading file");
        }
      //analyze file
      chatDocumentService.embedFile(file,fileServiceResponse.getFileResponseMeta().getFileId());
      return ResponseEntity.ok(fileServiceResponse);
    }

    @PostMapping("chat")
    public ResponseEntity<Object> chat(@RequestBody Question question) throws URISyntaxException, IOException {
        return ResponseEntity.ok(chatDocumentService.chat(question));
    }


    @PostMapping(value = "streamingChat",produces = "text/plain")
    public ResponseEntity<Object> streamingChat(HttpServletResponse response, HttpServletRequest request, @RequestBody Question question) throws URISyntaxException, IOException {
        return ResponseEntity.ok(chatDocumentService.chat(question,response,request));
    }






}
