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
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private String OPENAI_API_KEY

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

