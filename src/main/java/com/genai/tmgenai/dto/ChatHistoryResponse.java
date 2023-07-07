package com.genai.tmgenai.dto;

import com.genai.tmgenai.common.models.UserEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatHistoryResponse {
    private String content;
    private UserEnum userType;
    private LocalDateTime dateTime;
    private String fileId;
}
