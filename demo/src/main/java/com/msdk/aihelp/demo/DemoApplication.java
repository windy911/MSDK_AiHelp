package com.msdk.aihelp.demo;

import android.app.Application;

import com.msdk.aihelp.MSDKAiHelp;
import com.msdk.aihelp.config.AiHelpConfig;

public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AiHelpConfig config = new AiHelpConfig.Builder()
                .setDomain("https://cs-demo.yourgame.com")
                .setAppId("demo_001")
                .setAppSecret("demo_secret")
                .setThemeColor(0xFF1A73E8)
                .build();
        MSDKAiHelp.init(this, config);
    }
}
