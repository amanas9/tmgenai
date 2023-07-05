package com.genai.tmgenai.service;

import com.genai.tmgenai.PineConeEmbeddingstoreCustomImpl;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentLoader;
import dev.langchain4j.data.document.DocumentSegment;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.CharacterSplitter;
import dev.langchain4j.data.document.splitter.SentenceSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.PineconeEmbeddingStore;
import dev.langchain4j.store.embedding.PineconeEmbeddingStoreImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static dev.langchain4j.data.document.DocumentType.PDF;
import static dev.langchain4j.model.openai.OpenAiModelName.TEXT_EMBEDDING_ADA_002;
import static java.time.Duration.ofSeconds;

@Service
public class FileEmbeddingService {

    @Value("${key.opnenapikey}")
    private String OPENAI_API_KEY;

    public void embedFile(MultipartFile multipartFile,String fileId) throws IOException {

        File file = new File("/Users/amankumar/Downloads"  + fileId + ".pdf");
        multipartFile.transferTo(file);
        DocumentLoader documentLoader = DocumentLoader.from(Paths.get(file.getPath()), PDF);
        Document document = documentLoader.load();
        document.text();




        // Split document into segments (one paragraph per segment)

        DocumentSplitter splitter = new CharacterSplitter(1500,15);
       List<DocumentSegment> documentSegments = splitter.split(document);
       documentSegments.forEach(documentSegment -> {
           documentSegment.metadata().add("file_id",fileId);
       });



        // Embed segments (convert them into semantic vectors)

        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(OPENAI_API_KEY) // https://platform.openai.com/account/api-keys
                .modelName(TEXT_EMBEDDING_ADA_002)
                .timeout(ofSeconds(15))
                .build();

        List<Embedding> embeddings = embeddingModel.embedAll(documentSegments).get();

        // Store embeddings into embedding store for further search / retrieval

        PineConeEmbeddingstoreCustomImpl pinecone = new PineConeEmbeddingstoreCustomImpl("1d0899b3-7abf-40be-a267-ac208d572ed3", "asia-southeast1-gcp-free", "bca6a53", "documents", "default");

//
        List<String> addeds= pinecone.addAll(embeddings, documentSegments);

        System.out.println(addeds);

    }
}
