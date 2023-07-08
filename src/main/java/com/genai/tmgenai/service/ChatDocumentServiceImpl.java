package com.genai.tmgenai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genai.tmgenai.PineConeEmbeddingstoreCustomImpl;
import com.genai.tmgenai.common.models.ChatHistory;
import com.genai.tmgenai.common.models.UserEnum;
import com.genai.tmgenai.common.repositories.ChatHistoryRepository;
import com.genai.tmgenai.dto.*;
import com.genai.tmgenai.models.Files;
import com.genai.tmgenai.repository.FilesRepository;
import com.google.protobuf.Struct;
import dev.langchain4j.chain.ConversationalChain;
import dev.langchain4j.data.document.DocumentSegment;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;

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

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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

    @Autowired
    private FilesRepository filesRepository;

    @Value("${key.opnenapikey}")
    private String OPENAI_API_KEY;

    @Autowired
    private ConversationalChain conversationalChain;

    public static final String HEADER_X_BROKER = "x-broker";
    public static final String HEADER_X_TENANT = "x-tenant";
    public static final String TURTLEMINT = "turtlemint";

    @Autowired
    public ChatDocumentServiceImpl(RestService restService, FileEmbeddingService fileEmbeddingService) {
        this.restService = restService;
        this.fileEmbeddingService = fileEmbeddingService;
    }
    @Override
    public Files embedFile(MultipartFile file,String fileId) throws URISyntaxException, IOException {

            Files files = fileEmbeddingService.embedFile(file, fileId);
            return files;


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
        String information = relevantEmbeddings.stream()
                .map(match -> match.embedded().get().text())
                .collect(joining("\n\n"));
        // Check for greetings and generate appropriate responses
        List<String> userGreeting = Arrays.asList("hi","hello","goodmorning","good morning","goodevening","good evening","goodafternoon","good afternoon");
        String assistantGreetingResponse = "Hello! How can I assist you today?";
        List<String> userThanks = Arrays.asList("thank you","thanks","welcome","thank you so much","");
        String assistantThanksResponse = "You're welcome! I'm here to help.";
        String response = "";

        // Check for greetings
        if (userGreeting.contains(questionString.toLowerCase())){
            response = assistantGreetingResponse;
        }
        // Check for thanks
        else if (userThanks.contains(questionString.toLowerCase())) {
            response = assistantThanksResponse;
        }
        else {
            response = getAiresponse(questionString,assistantGreetingResponse,information); // Your response generation logic
        }

        Answer answer1 = new Answer();
        answer1.setAnswer(response);
        answer1.setQuestion(question);
        setAnswerInDB(answer1,UserEnum.BOT);
        autocompleteStore.addQuestion(questionString, AutoCompleteDetails.VERTICAL.FW,question.isSuggestion());

        return answer1;
    }

    @Autowired
    AutocompleteStore autocompleteStore;
    private String getAiresponse(String questionString, String assistantGreetingResponse, String additionalInstructions) {
        PromptTemplate promptTemplate = PromptTemplate.from(
                "{{question}}\n"
                        + "{{response}}"
                        + "{{additionalInstructions}}"
        );
        Map<String, Object> variables = new HashMap<>();
        variables.put("question", questionString);
        variables.put("response", assistantGreetingResponse);
        variables.put("additionalInstructions",additionalInstructions);
        Prompt prompt = promptTemplate.apply(variables);
        AiMessage aiMessage = AiMessage.from(conversationalChain.execute(prompt.text()));
        System.out.println("Message "+aiMessage.text());
        return aiMessage.text();
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

    @Override
    public AnswerResponse chatting(Question question) throws URISyntaxException, IOException {
        setQuestionInDB(question, UserEnum.Customer);

        String questionString = question.getQuestion();

        questionString = questionString.toLowerCase();

        List<String> quotesWord1 = Arrays.asList("share","get","renewal");
        List<String> quotesWord2 = Arrays.asList("quote","quotes","premium","amount","payment");


        boolean firstList = false;
        for(String word1 : quotesWord1) {
            if(questionString.contains(word1)) {
                firstList = true;
                break;
            }
        }

        boolean secondList = false;
        for(String word1 : quotesWord2) {
            if(questionString.contains(word1)) {
                secondList = true;
                break;
            }
        }

        boolean isQuoteRequest = false;
        if(firstList && secondList) {
            isQuoteRequest = true;
        }


        if (isQuoteRequest){
            String resulturl =  createPremiumRequest(question);
            if(resulturl != null){
                Answer answer = new Answer();
                answer.setAnswer("Please find quotes here: " +  resulturl);
                answer.setQuestion(question);
                AnswerResponse answerResponse = new AnswerResponse();
                answerResponse.setAnswer(answer);
                answerResponse.setVertical("COMMON");
                return answerResponse;
            }
        }


        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(OPENAI_API_KEY) // https://platform.openai.com/account/api-keys
                .modelName(TEXT_EMBEDDING_ADA_002)
                .timeout(ofSeconds(15))
                .build();

        PineConeEmbeddingstoreCustomImpl pinecone = new PineConeEmbeddingstoreCustomImpl("1d0899b3-7abf-40be-a267-ac208d572ed3", "asia-southeast1-gcp-free", "bca6a53", "documents", "default");

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

        AnswerResponse answerResponse = new AnswerResponse();
        answerResponse.setAnswer(answer1);
        answerResponse.setFileId(question.getFileId());
        Files file = filesRepository.findByFileId(question.getFileId());
        answerResponse.setVertical(file.getVertical()==null?"COMMON":file.getVertical());
        return answerResponse;
    }



    private String createPremiumRequest(Question question) throws JsonProcessingException {
        Files files = filesRepository.findByFileId(question.getFileId());
        MotorRequest motorRequest = new MotorRequest();
        motorRequest.setVertical(files.getVertical());
        MotorPremiumRequest motorPremiumRequest = new MotorPremiumRequest();
        JsonNode jsonNode = new ObjectMapper().readTree(files.getPresquestInfo());
        motorPremiumRequest.setVertical(files.getVertical());

        String regNum =  jsonNode.get("registration_number").asText();
        String make =  jsonNode.get("make").asText();
        String model =  jsonNode.get("model").asText();
        String variant =  jsonNode.get("variant").asText();
        String fuelType =  jsonNode.get("fuel").asText();
        String year =  jsonNode.get("year").asText();

        motorPremiumRequest.setYear(year);

        motorPremiumRequest.setRegistrationNo(regNum);
        motorPremiumRequest.setMake(make);
        motorPremiumRequest.setModel(model);
        motorPremiumRequest.setVariant(variant);
        motorPremiumRequest.setFuel(fuelType);
        motorPremiumRequest.setRegistrationDate(motorPremiumRequest.getRegistrationDate()+year);
        if(regNum != null && regNum.length()>=10){
            String rto_id = regNum.substring(0, 4);
            motorPremiumRequest.setRtoId(rto_id);
            motorPremiumRequest.setUserStateCode(rto_id.substring(0, 2));
            motorRequest.setStateCode(rto_id.substring(0, 2));

        }




        motorRequest.setMotorPremiumRequest(motorPremiumRequest);
        try {
            String url = "https://pro.turtlemint.com/api/platform/v0/premiums/request";
            log.info("MOTOR REQUEST {} {}",url, new  ObjectMapper().writeValueAsString(motorRequest));
            var response = restService.postForEntity(new URI(url),
                    new HttpEntity<>(motorRequest, getDefaultHeaders()),
                    MotorResponse.class
            );

            if(response != null && response.getBody() != null && response.getBody().getData() != null ){
               MotorResponseData motorResponseData = response.getBody().getData();
                if(motorResponseData.getPremiumRequest() != null){
                     return motorResponseData.getPremiumRequest().getResultURL();

                }
            }

            log.info("MOTOR RESPONSE {}",new ObjectMapper().writeValueAsString(response.getBody()));

        } catch (Exception e) {
            log.info("Exception in create motor request ",e);

        }

        return null;

    }

    private HttpHeaders getDefaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_X_BROKER, TURTLEMINT);
        headers.set(HEADER_X_TENANT, TURTLEMINT);
        return headers;
    }

}

