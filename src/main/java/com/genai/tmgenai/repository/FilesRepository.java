package com.genai.tmgenai.repository;

import com.genai.tmgenai.models.Files;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FilesRepository extends JpaRepository<Files, UUID> {
    Files save(Files file);
}
