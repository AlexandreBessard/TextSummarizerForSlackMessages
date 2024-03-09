package org.example;

public class ChatGPTClient {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("OPENAI_API_KEY is missing");
            System.exit(1);
        }
        final String apiKey = args[0];

    }

}
