package com.genai.tmgenai.service;

import com.genai.tmgenai.PineConeEmbeddingstoreCustomImpl;
import com.genai.tmgenai.dto.Answer;
import com.genai.tmgenai.dto.FileResponseMeta;
import com.genai.tmgenai.dto.FileServiceResponse;
import com.genai.tmgenai.dto.Question;
import com.google.protobuf.Struct;
import dev.langchain4j.data.document.DocumentSegment;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.PineconeEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.langchain4j.model.openai.OpenAiModelName.*;
import static java.time.Duration.ofSeconds;
import static java.util.stream.Collectors.joining;

@Service
@Slf4j
public class ChatDocumentServiceImpl implements ChatDocumentService{

    private final RestService restService;
    private final FileEmbeddingService fileEmbeddingService;

    @Value("${key.opnenapikey}")
    private String OPENAI_API_KEY;

    @Autowired
    public ChatDocumentServiceImpl(RestService restService, FileEmbeddingService fileEmbeddingService) {
        this.restService = restService;
        this.fileEmbeddingService = fileEmbeddingService;
    }
    @Override
    public String embedFile(MultipartFile file,String fileId) throws URISyntaxException, IOException {

            String summary = fileEmbeddingService.embedFile(file, fileId);
            return summary;


    }

    public FileServiceResponse uploadFile(MultipartFile file) throws URISyntaxException {
//        var headers = new HttpHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//        body.add("file", file.getResource());
//        body.add("path", "/renewals/bulk-upload/" + file.getOriginalFilename());
//        body.add("cloudSource", "AWS_S3");
//        body.add("broker", "turtlemint");
//        body.add("bucket", "policyrenewal-stage");
//        body.add("tag", "Document");
//
//        HttpEntity<MultiValueMap<String, Object>> requestEntity= new HttpEntity<>(body, headers);
//
//        return restService.postForEntity(
//                new URI( "https://ninja.twilight.turtle-feature.com" + "/api/files/v1/upload"),
//                requestEntity,
//                FileServiceResponse.class
//        ).getBody();
        FileServiceResponse fileServiceResponse =  new FileServiceResponse();
        FileResponseMeta meta = new FileResponseMeta();
        meta.setFileId(UUID.randomUUID().toString());
        fileServiceResponse.setFileResponseMeta(meta);
        return fileServiceResponse;
    }

    @Override
    public Answer chat(Question question) throws URISyntaxException, IOException {

        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(OPENAI_API_KEY) // https://platform.openai.com/account/api-keys
                .modelName(TEXT_EMBEDDING_ADA_002)
                .timeout(ofSeconds(15))
                .build();

        PineConeEmbeddingstoreCustomImpl pinecone = new PineConeEmbeddingstoreCustomImpl("1d0899b3-7abf-40be-a267-ac208d572ed3", "asia-southeast1-gcp-free", "bca6a53", "documents", "default");



        String questionString = question.getQuestion();

        Embedding questionEmbedding = embeddingModel.embed(questionString).get();


        Struct filter = Struct.newBuilder().putFields("file_id", com.google.protobuf.Value.newBuilder().setStringValue(question.getFileId()).build()).build();



        // Find relevant embeddings in embedding store by semantic similarity

        List<EmbeddingMatch<DocumentSegment>> relevantEmbeddings = pinecone.findRelevant(questionEmbedding, 10,filter);

        System.out.println("relevantEmbeddings : " + relevantEmbeddings);


        // Create a prompt for the model that includes question and relevant embeddings

        PromptTemplate promptTemplate = PromptTemplate.from(
                "Answer the following question to the best of your ability:\n"
                        + "\n"
                        + "Question:\n"
                        + "{{questionString}}\n"
                        + "\n"
                        + "Base your answer on the below information from a policy document: \n"
                        + "{{information}}");

        String information = relevantEmbeddings.stream()
                .map(match -> match.embedded().get().text())
                .collect(joining("\n\n"));

        log.info("information : {}",information);



        Map<String, Object> variables = new HashMap<>();
        variables.put("questionString", question);
        variables.put("information", information);

        Prompt prompt = promptTemplate.apply(variables);


        // Send prompt to the model

        ChatLanguageModel chatModel = OpenAiChatModel.builder()
                .apiKey(OPENAI_API_KEY) // https://platform.openai.com/account/api-keys
                .modelName(GPT_3_5_TURBO)
                .temperature(0.4)
                .logResponses(true)
                .logRequests(true)
                .build();

        AiMessage aiMessage = chatModel.sendUserMessage(prompt).get();


        // See an answer from the model

        Answer answer1 = new Answer();
       answer1.setAnswer(aiMessage.text());
        answer1.setQuestion(question);

        return answer1;
    }

}

