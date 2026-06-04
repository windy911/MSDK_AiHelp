package com.msdk.aihelp.config;

import org.junit.Test;
import static org.junit.Assert.*;

public class AiHelpConfigTest {

    @Test
    public void builder_setsAllFields() {
        AiHelpConfig config = new AiHelpConfig.Builder()
                .setDomain("https://example.com")
                .setAppId("app_123")
                .setAppSecret("secret_456")
                .setThemeColor(0xFFFF0000)
                .setLogoResId(12345)
                .build();

        assertEquals("https://example.com", config.getDomain());
        assertEquals("app_123", config.getAppId());
        assertEquals("secret_456", config.getAppSecret());
        assertEquals(0xFFFF0000, config.getThemeColor());
        assertEquals(12345, config.getLogoResId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_missingDomain_throws() {
        new AiHelpConfig.Builder()
                .setAppId("app_123")
                .setAppSecret("secret_456")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_missingAppId_throws() {
        new AiHelpConfig.Builder()
                .setDomain("https://example.com")
                .setAppSecret("secret_456")
                .build();
    }
}
