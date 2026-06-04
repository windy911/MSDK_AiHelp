package com.msdk.aihelp.config;

import android.content.Context;
import com.msdk.aihelp.model.UserInfo;

public class ConfigManager {

    private static ConfigManager instance;

    private Context appContext;
    private AiHelpConfig config;
    private UserInfo userInfo;
    private String language;
    private boolean initialized;

    private ConfigManager() {}

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public void init(Context context, AiHelpConfig config) {
        this.appContext = context.getApplicationContext();
        this.config = config;
        this.initialized = true;
    }

    public boolean isInitialized() { return initialized; }
    public Context getAppContext() { return appContext; }
    public AiHelpConfig getConfig() { return config; }

    public void setUserInfo(UserInfo userInfo) { this.userInfo = userInfo; }
    public UserInfo getUserInfo() { return userInfo; }
    public void clearUserInfo() { this.userInfo = null; }

    public void setLanguage(String language) { this.language = language; }
    public String getLanguage() { return language; }
}
