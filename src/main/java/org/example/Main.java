package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.conversations.ConversationsHistoryRequest;
import com.slack.api.methods.response.conversations.ConversationsHistoryResponse;
import com.slack.api.methods.request.users.UsersInfoRequest;
import com.slack.api.methods.response.users.UsersInfoResponse;
import com.slack.api.model.Message;
import com.slack.api.model.User;
import org.example.model.ChatCompletion;

import java.time.*;
import java.util.Collections;
import java.util.List;

/*
Programmatically reading messages from Slack with ChannelReaderSlackBot
 */
public class Main {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("SLACK_BOT_TOKEN, OPENAI_API_KEY or YOUR_CHANNEL_ID is missing");
            System.exit(1);
        }

        final String slackBotToken = args[0];
        final String openApiKey = args[1];
        final String yourChannelId = args[2];

        // This connects to the underlying Slack API infrastructure allowing to interact with the exposed methods.
        Slack slack = Slack.getInstance();
        MethodsClient methodsProvidedBySlackInstance = slack.methods(slackBotToken);

        var startTimeUTC = LocalDateTime.now().withHour(8).withMinute(0).withSecond(0).withNano(0);
        var endTimeUTC = LocalDateTime.now().withHour(18).withMinute(0).withSecond(0).withNano(0);

        long startTime = startTimeUTC.toEpochSecond(ZoneOffset.UTC);
        long endTime = endTimeUTC.toEpochSecond(ZoneOffset.UTC);

        ConversationsHistoryRequest request = ConversationsHistoryRequest.builder()
                .channel(yourChannelId)
                // Get the messages from that range
                .oldest(String.valueOf(startTime))
                .latest(String.valueOf(endTime))
                .build();

        try {
            // Get all the messages from Slack by channel id
            ConversationsHistoryResponse response = methodsProvidedBySlackInstance.conversationsHistory(request);
            if (response != null && response.isOk()) {
                List<Message> messages = response.getMessages();
                // Order from the older messages to the most recent messages
                Collections.reverse(messages);
                final var stringBuilder = new StringBuilder();
                for (Message message: messages) {

                    String userId = message.getUser();
                    String timestamp = formatTimestamp(message.getTs());

                    var userInfoRequest = UsersInfoRequest.builder()
                            .user(userId)
                            .build();
                    // Get more info to the user
                    UsersInfoResponse userInfoResponse =
                            methodsProvidedBySlackInstance.usersInfo(userInfoRequest);

                    if (userInfoResponse != null && userInfoResponse.isOk()) {
                        User user = userInfoResponse.getUser();
                        stringBuilder.append(user.getName())
                                .append(" ")
                                .append(timestamp)
                                .append(" ")
                                .append(message.getText())
                                .append("\n");
                    }
                }
                // Use to reach OpenAI in order to summarize the conversation of the current work day
                final var chatGPTClient = new ChatGPTClient(openApiKey);
                String rep = chatGPTClient.summarize(stringBuilder.toString());
                var objectMapper = new ObjectMapper();
                var chatCompletion = objectMapper.readValue(rep, ChatCompletion.class);
                System.out.println(breakLinesAfterDot(chatCompletion.choices.get(0).message.getContent()));
            } else {
                assert response != null;
                System.out.println("Failed to fetch messages: " + response.getError());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String breakLinesAfterDot(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            result.append(currentChar);
            if (currentChar == '.') {
                result.append("\n"); // Add a line break after '.'
            }
        }
        return result.toString();
    }

    private static String formatTimestamp(String ts) {
        double timestamp = Double.parseDouble(ts);
        Instant instant = Instant.ofEpochSecond((long) timestamp);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        return dateTime.toString();
    }

}
