package com.genai.tmgenai.controller;

import com.genai.tmgenai.common.models.ChatHistory;
import com.genai.tmgenai.common.models.UserEnum;
import com.genai.tmgenai.dto.ChatHistoryResponse;
import com.genai.tmgenai.dto.FileServiceResponse;
import com.genai.tmgenai.dto.Question;
import com.genai.tmgenai.models.Files;
import com.genai.tmgenai.service.ChatDocumentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;

import static com.genai.tmgenai.PromptConstants.WelcomeAnswer;

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
      Files files = chatDocumentService.embedFile(file,fileServiceResponse.getFileResponseMeta().getFileId());
      files.setDefaultAnswer(WelcomeAnswer);
      return ResponseEntity.ok(files);
    }

    @PostMapping("chat")
    public ResponseEntity<Object> chat(@RequestBody Question question) throws URISyntaxException, IOException {

        return ResponseEntity.ok(chatDocumentService.chatting(question));
    }


    @PostMapping(value = "streamingChat",produces = "text/plain")
    public ResponseEntity<Object> streamingChat(HttpServletResponse response, HttpServletRequest request, @RequestBody Question question) throws URISyntaxException, IOException {
        return ResponseEntity.ok(chatDocumentService.chat(question,response,request));
    }
    @GetMapping("/data")
    public ResponseEntity<List<ChatHistoryResponse>> getData(@RequestParam("fileIds") String fileId) {
        return ResponseEntity.ok(chatDocumentService.getData(fileId));
    }

}
