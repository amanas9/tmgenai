package com.genai.tmgenai.service;

import com.genai.tmgenai.common.models.ChatHistory;
import com.genai.tmgenai.common.models.UserEnum;
import com.genai.tmgenai.dto.Answer;
import com.genai.tmgenai.dto.ChatHistoryResponse;
import com.genai.tmgenai.dto.FileServiceResponse;
import com.genai.tmgenai.dto.Question;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface ChatDocumentService {
    public String embedFile(MultipartFile file,String fileId) throws URISyntaxException, IOException;

    public FileServiceResponse uploadFile(MultipartFile file) throws URISyntaxException;

    public Answer chat(Question question) throws URISyntaxException, IOException;

    public void setQuestionInDB (Question question, UserEnum userEnum);
    public void setAnswerInDB (Answer answer, UserEnum userEnum);
    List<ChatHistoryResponse> getData(String fileId);
}
