package com.genai.tmgenai.service;

import com.genai.tmgenai.PineConeEmbeddingstoreCustomImpl;
import com.genai.tmgenai.PromptConstants;
import com.genai.tmgenai.dto.Question;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
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
public class FileEmbeddingService {

    @Value("${key.opnenapikey}")
    private String OPENAI_API_KEY;

    @Autowired
    private ConversationalChain conversationalChain;

    private PineConeEmbeddingstoreCustomImpl pinecone = new PineConeEmbeddingstoreCustomImpl("1d0899b3-7abf-40be-a267-ac208d572ed3", "asia-southeast1-gcp-free", "bca6a53", "documents", "default");

    public String embedFile(MultipartFile multipartFile,String fileId) throws IOException {

        File file = new File("/Users/amankumar/Downloads"  + fileId + ".pdf");
        multipartFile.transferTo(file);
        DocumentLoader documentLoader = DocumentLoader.from(Paths.get(file.getPath()), PDF);
        Document document = documentLoader.load();
        document.text();


        // Split document into segments (one paragraph per segment)

        DocumentSplitter splitter = new CharacterSplitter(1000,15);
       List<DocumentSegment> documentSegments = splitter.split(document);

        documentSegments.forEach(documentSegment -> {
           documentSegment.metadata().add("file_id", fileId);
       });

        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(OPENAI_API_KEY) // https://platform.openai.com/account/api-keys
                .modelName(TEXT_EMBEDDING_ADA_002)
                .timeout(ofSeconds(15))
                .build();

        List<Embedding> embeddings = embeddingModel.embedAll(documentSegments).get();

        // Store embeddings into embedding store for further search / retrieval

        List<String> addeds= pinecone.addAll(embeddings, documentSegments);


        System.out.println(fileId);

        return getInitialSummary(fileId);



    }

    private String getInitialSummary(String fileId) {
        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(OPENAI_API_KEY) // https://platform.openai.com/account/api-keys
                .modelName(TEXT_EMBEDDING_ADA_002)
                .timeout(ofSeconds(15))
                .build();

        Embedding questionEmbedding = embeddingModel.embed(PromptConstants.PROMPT_FOR_INITIAL_SUMMARY).get();

        Struct filter = Struct.newBuilder().putFields("file_id", com.google.protobuf.Value.newBuilder().setStringValue(fileId).build()).build();

        // Find relevant embeddings in embedding store by semantic similarity
        List<EmbeddingMatch<DocumentSegment>> relevantEmbeddings = pinecone.findRelevant(questionEmbedding,6,filter);

        String information = relevantEmbeddings.stream()
                .map(match -> match.embedded().get().text())
                .collect(joining("\n\n"));

        log.info("information : {}",information);

        PromptTemplate promptTemplate = PromptTemplate.from(
                        PromptConstants.PROMPT_FOR_INITIAL_SUMMARY
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

    private Prompt getPromptTemplateForVertical(String information ){
        PromptTemplate promptTemplate = PromptTemplate.from(
                "determine the value of vertical as json key it's value by following below steps from information below delimited by triple backticks:\n"
                        + "1.determine  if the given information is about health policy or motor policy\n"
                        + "2.if it is health policy, then the vertical will be HEALTH\n"
                        + "3.if it is a motor policy, then vertical will be TW if the make model variant belongs to a two wheeler, FW if it belongs to four wheeler and CV if belongs to commercial vehicle\n"
                        + "/n"
                        + "give the output only as a json object with vertical as key\n"
                        +" ```{{information}}```\n");

        Map<String, Object> variables = new HashMap<>();

        variables.put("information", information);

        Prompt prompt = promptTemplate.apply(variables);

        return prompt;
    }

}
