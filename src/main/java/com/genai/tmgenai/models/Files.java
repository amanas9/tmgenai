package com.genai.tmgenai.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class Files {
    @Id
    private UUID Id = UUID.randomUUID();
    private String fileId;
    private String identifier;
    private String vertical;

    private String summary;

}
