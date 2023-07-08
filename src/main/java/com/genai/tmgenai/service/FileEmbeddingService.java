package com.genai.tmgenai.service;

import com.genai.tmgenai.PineConeEmbeddingstoreCustomImpl;
import com.genai.tmgenai.PromptConstants;
import com.genai.tmgenai.dto.Question;
import com.genai.tmgenai.models.Files;
import com.genai.tmgenai.repository.FilesRepository;
import com.google.protobuf.Struct;
import dev.langchain4j.chain.ConversationalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentLoader;
import dev.langchain4j.data.document.DocumentSegment;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.CharacterSplitter;
import dev.langchain4j.data.document.splitter.SentenceSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.PineconeEmbeddingStore;
import dev.langchain4j.store.embedding.PineconeEmbeddingStoreImpl;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.data.document.DocumentType.PDF;
import static dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO;
import static dev.langchain4j.model.openai.OpenAiModelName.TEXT_EMBEDDING_ADA_002;
import static java.time.Duration.ofSeconds;
import static java.util.stream.Collectors.joining;

@Service
@Slf4j
@EnableAsync
public class FileEmbeddingService {

    @Value("${key.opnenapikey}")
    private String OPENAI_API_KEY;

    @Autowired
    private ConversationalChain conversationalChain;

    @Autowired
    private FilesRepository filesRepository;

    private PineConeEmbeddingstoreCustomImpl pinecone = new PineConeEmbeddingstoreCustomImpl("1d0899b3-7abf-40be-a267-ac208d572ed3", "asia-southeast1-gcp-free", "bca6a53", "documents", "default");

    public String embedFile(MultipartFile multipartFile,String fileId) throws IOException {



        log.info("splitting");
        File file = new File("/Users/amankumar/Downloads"  + fileId + ".pdf");

        multipartFile.transferTo(file);

        DocumentLoader documentLoader = DocumentLoader.from(Paths.get(file.getPath()), PDF);
        Document document = documentLoader.load();
        document.text();

        log.info("splitted");




        // Split document into segments (one paragraph per segment)

        DocumentSplitter splitter = new CharacterSplitter(1000,15);
       List<DocumentSegment> documentSegments = splitter.split(document);

        documentSegments.forEach(documentSegment -> {
           documentSegment.metadata().add("file_id", fileId);
       });

        String informationForVertical = documentSegments.stream().limit(4).map(DocumentSegment::text).collect(joining("\n\n"));

                log.info("getting embeddding");
        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(OPENAI_API_KEY) // https://platform.openai.com/account/api-keys
                .modelName(TEXT_EMBEDDING_ADA_002)
                .timeout(ofSeconds(15))
                .build();

        List<Embedding> embeddings = embeddingModel.embedAll(documentSegments).get();

        log.info("got embeddding");

        // Store embeddings into embedding store for further search / retrieval



        List<String> addeds= pinecone.addAll(embeddings, documentSegments);

        log.info("added to pinecone");

        System.out.println(fileId);



        new Thread(() -> saveFileDetails(fileId,informationForVertical)).start();
        log.info("saved to pinecone");

        return "";

    }

    @Async
    public void saveFileDetails(String fileId,String informationForVertical) {
        log.info("Saving file details for file id {}", fileId);
        Files files = new Files();
        files.setFileId(fileId);
       // String summary =  getInitialSummary(fileId);
        String vertical =  getPromptTemplateForVertical(informationForVertical);

        if(vertical.contains("TW"))
            vertical = "TW";
        if(vertical.contains("FW"))
            vertical = "FW";
        if(vertical.contains("CV"))
            vertical = "CV";
        if(vertical.contains("HEALTH"))
            vertical = "HEALTH";

        files.setVertical(vertical);
        String summary =  getInitialSummary(fileId, vertical);
        files.setSummary(summary);

        filesRepository.save(files);
    }

//    private String getVertical(String summary) {
//         summary = summary.toLowerCase();
//        if(summary.contains("registration number") || summary.contains("registration no") || summary.contains("registration no."))
//            return "Motor";
//         if(summary.contains("motor"))
//            return "Motor";
//         if (summary.contains("health"))
//            return "Health";
//         return null;
//
//    }

    private String getInitialSummary(String fileId,String vertical) {
        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(OPENAI_API_KEY) // https://platform.openai.com/account/api-keys
                .modelName(TEXT_EMBEDDING_ADA_002)
                .timeout(ofSeconds(15))
                .build();

        Struct filter = Struct.newBuilder().putFields("file_id", com.google.protobuf.Value.newBuilder().setStringValue(fileId).build()).build();


        Embedding questionEmbedding = embeddingModel.embed(PromptConstants.PROMPT_FOR_INITIAL_SUMMARY_MOTOR).get();
        if(vertical.equalsIgnoreCase("Health"))
            questionEmbedding = embeddingModel.embed(PromptConstants.PROMPT_FOR_INITIAL_SUMMARY_HEALTH).get();


        List<EmbeddingMatch<DocumentSegment>> relevantEmbeddings = new ArrayList<>();

        if(vertical.equalsIgnoreCase("Health"))
            relevantEmbeddings = pinecone.findRelevant(questionEmbedding, 10, filter);
        else
            relevantEmbeddings = pinecone.findRelevant(questionEmbedding, 6, filter);



        String information = relevantEmbeddings.stream()
                .map(match -> match.embedded().get().text())
                .collect(joining("\n\n"));

        log.info("information : {}",information);

        PromptTemplate promptTemplate = PromptTemplate.from(
                        PromptConstants.PROMPT_FOR_INITIAL_SUMMARY_MOTOR
                        + "```{{information}}```\n"
        );
        if(vertical.equalsIgnoreCase("Health"))
            promptTemplate = PromptTemplate.from(
                    PromptConstants.PROMPT_FOR_INITIAL_SUMMARY_HEALTH
                            + "```{{information}}```\n"
            );



        Map<String, Object> variables = new HashMap<>();

        variables.put("information", information);

        Prompt prompt = promptTemplate.apply(variables);

//        ChatLanguageModel chatModel = OpenAiChatModel.builder()
//                .apiKey(OPENAI_API_KEY) // https://platform.openai.com/account/api-keys
//                .modelName(GPT_3_5_TURBO)
//                .temperature(0.5)
//                .logResponses(true)
//                .logRequests(true)
//                .build();



        AiMessage aiMessage = AiMessage.from(conversationalChain.execute(prompt.text()));

      //  AiMessage aiMessage = chatModel.sendUserMessage(prompt).get();

        System.out.println(aiMessage.text());
        if(aiMessage.text()!=null){
            DocumentSegment documentSegment = DocumentSegment.from(aiMessage.text());
            documentSegment.metadata().add("file_id", fileId);
            Embedding embedding = embeddingModel.embed(documentSegment).get();
            pinecone.add(embedding, documentSegment);
        }
        return aiMessage.text();
    }

    private String getPromptTemplateForVertical(String information) {
        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(OPENAI_API_KEY) // https://platform.openai.com/account/api-keys
                .modelName(TEXT_EMBEDDING_ADA_002)
                .timeout(ofSeconds(15))
                .build();



        PromptTemplate promptTemplate = PromptTemplate.from(
                PromptConstants.PROMPT_FOR_GETTING_VERTICAL
                        +" ```{{information}}```\n");

        Map<String, Object> variables = new HashMap<>();

        variables.put("information", information);

        Prompt prompt = promptTemplate.apply(variables);

        AiMessage aiMessage = AiMessage.from(conversationalChain.execute(prompt.text()));


        return aiMessage.text();
    }

}
