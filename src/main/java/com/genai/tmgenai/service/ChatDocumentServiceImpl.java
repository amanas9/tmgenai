package com.genai.tmgenai.service;

import com.genai.tmgenai.PineConeEmbeddingstoreCustomImpl;
import com.genai.tmgenai.common.models.ChatHistory;
import com.genai.tmgenai.common.models.UserEnum;
import com.genai.tmgenai.common.repositories.ChatHistoryRepository;
import com.genai.tmgenai.dto.*;
import com.google.protobuf.Struct;
import dev.langchain4j.chain.ConversationalChain;
import dev.langchain4j.data.document.DocumentSegment;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResultHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.language.StreamingLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingLanguageModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.PineconeEmbeddingStore;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;


import static dev.langchain4j.model.openai.OpenAiModelName.*;
import static java.time.Duration.ofSeconds;
import static java.util.stream.Collectors.joining;

@Service
@Slf4j
public class ChatDocumentServiceImpl implements ChatDocumentService{

    private final RestService restService;
    private final FileEmbeddingService fileEmbeddingService;

    @Autowired
    private ChatHistoryRepository chatHistoryRepository;

    @Value("${key.opnenapikey}")
    private String OPENAI_API_KEY;

    @Autowired
    private ConversationalChain conversationalChain;

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
        setQuestionInDB(question, UserEnum.Customer);

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

        List<EmbeddingMatch<DocumentSegment>> relevantEmbeddings = pinecone.findRelevant(questionEmbedding, 5,filter);


        // Create a prompt for the model that includes question and relevant embeddings

        PromptTemplate promptTemplate = PromptTemplate.from(
                "Answer the following question to the best of your ability:\n"
                        + "\n"
                        + "Question:\n"
                        + "{{question}}\n"
                        + "\n"
                        + "Base your answer on the following information and be specific in answering questions and answer in not more than 3 lines:\n"
                        + "{{information}}");

        String information = relevantEmbeddings.stream()
                .map(match -> match.embedded().get().text())
                .collect(joining("\n\n"));

        log.info("information : {}",information);


        Map<String, Object> variables = new HashMap<>();
        variables.put("question", questionString);
        variables.put("information", information);

        Prompt prompt = promptTemplate.apply(variables);


        // Send prompt to the model

      //  AiMessage aiMessage = chatModel.sendUserMessage(prompt).get();

        AiMessage aiMessage = AiMessage.from(conversationalChain.execute(prompt.text()));


        // See an answer from the model

        Answer answer1 = new Answer();
        answer1.setAnswer(aiMessage.text());
        answer1.setQuestion(question);
        setAnswerInDB(answer1,UserEnum.BOT);
        return answer1;
    }

    @Override
    public Answer chat(Question question, HttpServletResponse response, HttpServletRequest request) throws URISyntaxException, IOException {
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

        List<EmbeddingMatch<DocumentSegment>> relevantEmbeddings = pinecone.findRelevant(questionEmbedding, 5,filter);




        // Create a prompt for the model that includes question and relevant embeddings

        PromptTemplate promptTemplate = PromptTemplate.from(
                "Answer the following question to the best of your ability :\n"
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

        StreamingLanguageModel model = OpenAiStreamingLanguageModel.withApiKey(OPENAI_API_KEY);

//        final AsyncContext asyncContext = request.startAsync();
//        final PrintWriter writer = response.getWriter();
//        response.setContentType("text/plain; charset=utf-8");

        model.process(prompt, new StreamingResultHandler() {

            @SneakyThrows
            @Override
            public void onPartialResult(String partialResult) {
                System.out.println(partialResult);
             //   writer.write(partialResult);
               //writer.flush();
            }

            @Override
            public void onComplete() {
//                writer.close();
//                asyncContext.complete();
            }

            @Override
            public void onError(Throwable error) {
               // asyncContext.complete();
            }
        });


        // See an answer from the model


        return null;
    }
    public void setQuestionInDB(Question question, UserEnum userEnum) {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setCreatedAt(LocalDateTime.now());
        chatHistory.setContent(question.getQuestion());
        chatHistory.setFileId(question.getFileId());
        chatHistory.setUserType(userEnum);
        chatHistoryRepository.save(chatHistory);
    }

    @Override
    public void setAnswerInDB(Answer answer, UserEnum userEnum) {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setCreatedAt(LocalDateTime.now());
        chatHistory.setContent(answer.getAnswer());
        chatHistory.setFileId(answer.getQuestion().getFileId());
        chatHistory.setUserType(userEnum);
        chatHistoryRepository.save(chatHistory);
    }

    @Override
    public List<ChatHistoryResponse> getData(String fileId) {
        Sort sortByCreatedAt = Sort.by("createdAt").ascending();
        List<ChatHistory> data = chatHistoryRepository.findByFileId(fileId,sortByCreatedAt);
        List<ChatHistoryResponse> responseData = getChatHistory(data);
        return responseData;
    }

    private List<ChatHistoryResponse> getChatHistory(List<ChatHistory> data) {
        List<ChatHistoryResponse> chatHistoryList = new ArrayList<>();
        for (ChatHistory chatHistory : data){
            ChatHistoryResponse response = new ChatHistoryResponse();
            response.setContent(chatHistory.getContent());
            response.setDateTime(chatHistory.getCreatedAt());
            response.setFileId(chatHistory.getFileId());
            response.setUserType(chatHistory.getUserType());
            chatHistoryList.add(response);
        }
        return chatHistoryList;

    }

}

