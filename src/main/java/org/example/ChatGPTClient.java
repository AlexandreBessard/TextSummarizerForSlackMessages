package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.Chat;
import org.example.model.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChatGPTClient {
    private final String openAIKey;
    private String endpoint = "https://api.openai.com/v1/chat/completions";
    private final String model = "gpt-3.5-turbo";
    private final float temperature = 1.0f;
    private final int max_tokens = 256;
    private final float top_p = 1.0f;
    private final int frequency_penalty = 0;
    private final int presence_penalty = 0;

    public ChatGPTClient(String openAIKey) {
        this.openAIKey = openAIKey;
    }

    public String summarize(String prompt) {
        try {
            Chat chat = buildChatWithPrompt(prompt);
            String jsonInput = convertChatToJson(chat);
            String jsonResponse = callOpenApi(jsonInput);
            return jsonResponse != null ? jsonResponse : "empty";
        } catch (IOException e) {
            e.printStackTrace();
            return "empty";
        }
    }

    private String callOpenApi(String jsonInput) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + openAIKey);
        connection.setDoOutput(true);

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(jsonInput.getBytes());
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } else {
            System.out.println("Error: " + responseCode);
            return null;
        }
    }

    private String convertChatToJson(Chat chat) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(chat);
    }

    private Chat buildChatWithPrompt(String prompt) {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", "You are a helpful assistant"));
        messages.add(new Message("user", prompt));
        messages.add(new Message("user", "Tl;dr"));
        return new Chat(new Chat.ChatBuilder().messages(messages));
    }

}
