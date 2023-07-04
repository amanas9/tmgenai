package com.genai.tmgenai.service;

import com.genai.tmgenai.dto.Answer;
import com.genai.tmgenai.dto.FileServiceResponse;
import com.genai.tmgenai.dto.Question;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;

public interface ChatDocumentService {
    public void embedFile(MultipartFile file,String fileId) throws URISyntaxException, IOException;

    public FileServiceResponse uploadFile(MultipartFile file) throws URISyntaxException;

    public Answer chat(Question question) throws URISyntaxException, IOException;

    public Answer chat(Question question, HttpServletResponse response, HttpServletRequest request) throws URISyntaxException, IOException;

}
