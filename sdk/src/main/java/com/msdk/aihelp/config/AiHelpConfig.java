package com.msdk.aihelp.config;

public class AiHelpConfig {

    private final String domain;
    private final String appId;
    private final String appSecret;
    private final int themeColor;
    private final int logoResId;

    private AiHelpConfig(Builder builder) {
        this.domain = builder.domain;
        this.appId = builder.appId;
        this.appSecret = builder.appSecret;
        this.themeColor = builder.themeColor;
        this.logoResId = builder.logoResId;
    }

    public String getDomain() { return domain; }
    public String getAppId() { return appId; }
    public String getAppSecret() { return appSecret; }
    public int getThemeColor() { return themeColor; }
    public int getLogoResId() { return logoResId; }

    public static class Builder {
        private String domain;
        private String appId;
        private String appSecret;
        private int themeColor = 0xFF1A73E8;
        private int logoResId = 0;

        public Builder setDomain(String domain) { this.domain = domain; return this; }
        public Builder setAppId(String appId) { this.appId = appId; return this; }
        public Builder setAppSecret(String appSecret) { this.appSecret = appSecret; return this; }
        public Builder setThemeColor(int themeColor) { this.themeColor = themeColor; return this; }
        public Builder setLogoResId(int logoResId) { this.logoResId = logoResId; return this; }

        public AiHelpConfig build() {
            if (domain == null || domain.isEmpty()) {
                throw new IllegalArgumentException("domain is required");
            }
            if (appId == null || appId.isEmpty()) {
                throw new IllegalArgumentException("appId is required");
            }
            if (appSecret == null || appSecret.isEmpty()) {
                throw new IllegalArgumentException("appSecret is required");
            }
            return new AiHelpConfig(this);
        }
    }
}
