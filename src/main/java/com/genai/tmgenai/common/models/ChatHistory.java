package com.genai.tmgenai.common.models;


import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class ChatHistory {
    @Id
    private UUID id = UUID.randomUUID();
    private LocalDateTime createdAt;
    private String fileId;
    private String content;
    @Enumerated(EnumType.STRING)
    private UserEnum userType;
}
