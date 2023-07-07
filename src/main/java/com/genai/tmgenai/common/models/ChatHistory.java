package com.genai.tmgenai.common.models;


import jakarta.persistence.*;
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
    @Column(columnDefinition = "text")
    private String content;
    @Enumerated(EnumType.STRING)
    private UserEnum userType;
}
