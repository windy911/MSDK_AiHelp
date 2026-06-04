package com.msdk.aihelp.model;

import java.util.HashMap;
import java.util.Map;

public class UserInfo {

    private final String userId;
    private final String userName;
    private final String serverId;
    private final Map<String, String> customData;

    private UserInfo(Builder builder) {
        this.userId = builder.userId;
        this.userName = builder.userName;
        this.serverId = builder.serverId;
        this.customData = builder.customData;
    }

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getServerId() { return serverId; }
    public Map<String, String> getCustomData() { return customData; }

    public static class Builder {
        private String userId;
        private String userName;
        private String serverId;
        private Map<String, String> customData = new HashMap<>();

        public Builder setUserId(String userId) { this.userId = userId; return this; }
        public Builder setUserName(String userName) { this.userName = userName; return this; }
        public Builder setServerId(String serverId) { this.serverId = serverId; return this; }
        public Builder addCustomData(String key, String value) {
            this.customData.put(key, value);
            return this;
        }

        public UserInfo build() {
            if (userId == null || userId.isEmpty()) {
                throw new IllegalArgumentException("userId is required");
            }
            return new UserInfo(this);
        }
    }
}
