package com.genai.tmgenai.service;

import com.genai.tmgenai.dto.Answer;
import com.genai.tmgenai.dto.FileServiceResponse;
import com.genai.tmgenai.dto.Question;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;

public interface ChatDocumentService {
    public String embedFile(MultipartFile file,String fileId) throws URISyntaxException, IOException;

    public FileServiceResponse uploadFile(MultipartFile file) throws URISyntaxException;

    public Answer chat(Question question) throws URISyntaxException, IOException;

}
