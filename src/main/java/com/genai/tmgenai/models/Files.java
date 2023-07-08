package com.genai.tmgenai.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class Files {
    @Id
    private UUID Id = UUID.randomUUID();

    private LocalDateTime createdAt;
    private String fileId;
    private String identifier;
    private String vertical;

    @Column(columnDefinition = "TEXT")
    private String summary;
    @Column(columnDefinition = "TEXT")
    private String presquestInfo;
    private String quotesPageUrl;

}
