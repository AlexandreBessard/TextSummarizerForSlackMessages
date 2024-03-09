package org.example;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.conversations.ConversationsHistoryRequest;
import com.slack.api.methods.response.conversations.ConversationsHistoryResponse;
import com.slack.api.methods.request.users.UsersInfoRequest;
import com.slack.api.methods.response.users.UsersInfoResponse;
import com.slack.api.model.Message;
import com.slack.api.model.User;
import com.slack.api.model.block.LayoutBlock;

import java.time.*;
import java.util.Collections;
import java.util.List;

/*
Programmatically reading messages from Slack with ChannelReaderSlackBot
 */
public class ChannelReaderSlackBot {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("SLACK_BOT_TOKEN or YOUR_CHANNEL_ID is missing");
            System.exit(1);
        }
        final String slackBotToken = args[0];
        final String yourChannelId = args[1];

        Slack slack = Slack.getInstance();
        MethodsClient methodsProvidedBySlackInstance = slack.methods(slackBotToken);

        var startTimeUTC = LocalDateTime.now().withHour(8).withMinute(0).withSecond(0).withNano(0);
        var endTimeUTC = LocalDateTime.now().withHour(18).withMinute(0).withSecond(0).withNano(0);

        long startTime = startTimeUTC.toEpochSecond(ZoneOffset.UTC);
        long endTime = endTimeUTC.toEpochSecond(ZoneOffset.UTC);

        System.out.println("Start Time: " + startTime);
        System.out.println("End Time: " + endTime);

        ConversationsHistoryRequest request = ConversationsHistoryRequest.builder()
                .channel(yourChannelId)
                // Get the messages from that range
                .oldest(String.valueOf(startTime))
                .latest(String.valueOf(endTime))
                .build();

        System.out.println("Request -> " + request);

        try {
            // Get all the messages from slack by channel id
            ConversationsHistoryResponse response = methodsProvidedBySlackInstance.conversationsHistory(request);
            System.out.println(response);
            if (response != null && response.isOk()) {
                List<Message> messages = response.getMessages();
                // Order from the older messages to the most recent messages
                Collections.reverse(messages);
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
                        System.out.println("User: " + user.getName());
                        System.out.println("Timestamp: " + timestamp);
                        System.out.println("Message: " + message.getText());
                        System.out.println();
                    }
                }
            } else {
                assert response != null;
                System.out.println("Failed to fetch messages: " + response.getError());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String formatTimestamp(String ts) {
        double timestamp = Double.parseDouble(ts);
        Instant instant = Instant.ofEpochSecond((long) timestamp);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        return dateTime.toString();
    }

}
