package com.genai.tmgenai.common.repositories;

import com.genai.tmgenai.common.models.ChatHistory;
import lombok.Data;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, UUID> {
    List<ChatHistory> findByFileId(String fileIds, Sort sort);

}
