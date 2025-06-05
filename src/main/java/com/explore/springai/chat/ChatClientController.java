package com.explore.springai.chat;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/openai/chat")
public class ChatClientController {

    private  final ChatClient chatClient;

    private static final String DEFAULT_PROMPT = "你是一个聊天助手，请根据用户提问回答！";

    @Autowired
    public ChatClientController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.defaultSystem(DEFAULT_PROMPT).build();
    }

    /**
     * ChatClient 简单调用
     */
    @GetMapping("/simple/chat1")
    public String simpleChat(@RequestParam String message) {
        return chatClient
                .prompt(message).call().content();
    }

    /**
     * ChatClient 简单调用
     * 默认使用 InMemoryChatMemory
     * * @param message 消息
     *
     * @param chatId 会话ID
     */
    @GetMapping("/simple/chat2")
    public String simpleChat(@RequestParam String message, @RequestParam String chatId) {
        return chatClient
                .prompt(message)
                .advisors(a -> a
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, chatId) // 设置聊天会话ID
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100)) // 设置聊天记录检索数量  
                .call().content();
    }

    /**
     * ChatClient 流式调用
     */
    @GetMapping("/stream/chat")
    public Flux<String> streamChat(@RequestParam String message,
                                   HttpServletResponse response) {

        response.setCharacterEncoding("UTF-8");
        return chatClient.prompt(message).stream().content();
    }

    /**
     * ChatClient 流式响应
     */
    @GetMapping(value = "/stream/response", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .stream()
                .content()
                .map(content -> ServerSentEvent.<String>builder()
                        .data(content)
                        .build());
    }


    @GetMapping("/custom")
    public String customModel(
            @RequestParam(value = "model", defaultValue = "deepseek-ai/DeepSeek-R1-Distill-Qwen-7B") String model,
            @RequestParam(value = "message", defaultValue = "介绍以下自己") String message) {
        Prompt prompt = new Prompt(new UserMessage(message),
                OpenAiChatOptions.builder()
                        .model(model)
                        .build());
        return chatClient.prompt(prompt).call().content();
    }

    @GetMapping("/custom/stream")
    public Flux<String> customModelStream(
            @RequestParam(value = "model", defaultValue = "deepseek-ai/DeepSeek-R1-Distill-Qwen-7B") String model,
            @RequestParam(value = "temperature") Double temperature,
            @RequestParam(value = "frequencyPenalty") Double frequencyPenalty,
            @RequestParam(value = "message", defaultValue = "介绍以下自己") String message,
            HttpServletResponse response) {

        response.setCharacterEncoding("UTF-8");
        Prompt prompt = new Prompt(new UserMessage(message),
                OpenAiChatOptions.builder()
                        .model(model)
                        .temperature(temperature)
                        .frequencyPenalty(frequencyPenalty)
                        .build());
        return chatClient.prompt(prompt).stream().content();
    }

}
