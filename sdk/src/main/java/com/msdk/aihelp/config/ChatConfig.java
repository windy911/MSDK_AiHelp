package com.msdk.aihelp.config;

public class ChatConfig {

    private final String welcomeMessage;

    private ChatConfig(Builder builder) {
        this.welcomeMessage = builder.welcomeMessage;
    }

    public String getWelcomeMessage() { return welcomeMessage; }

    public static class Builder {
        private String welcomeMessage;

        public Builder setWelcomeMessage(String welcomeMessage) {
            this.welcomeMessage = welcomeMessage;
            return this;
        }

        public ChatConfig build() {
            return new ChatConfig(this);
        }
    }
}
