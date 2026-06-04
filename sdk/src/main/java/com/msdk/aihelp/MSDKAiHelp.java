package com.msdk.aihelp;

import android.content.Context;
import android.content.Intent;
import com.msdk.aihelp.callback.AiHelpEventListener;
import com.msdk.aihelp.callback.UnreadCountCallback;
import com.msdk.aihelp.chat.ChatActivity;
import com.msdk.aihelp.chat.ChatManager;
import com.msdk.aihelp.config.AiHelpConfig;
import com.msdk.aihelp.config.ChatConfig;
import com.msdk.aihelp.config.ConfigManager;
import com.msdk.aihelp.config.FAQConfig;
import com.msdk.aihelp.faq.FAQActivity;
import com.msdk.aihelp.model.UserInfo;
import com.msdk.aihelp.network.ApiCallback;
import com.msdk.aihelp.network.ApiService;
import com.msdk.aihelp.network.HttpClient;
import com.msdk.aihelp.util.Logger;

public class MSDKAiHelp {

    private static AiHelpEventListener eventListener;

    public static void init(Context context, AiHelpConfig config) {
        if (context == null || config == null) {
            throw new IllegalArgumentException("context and config must not be null");
        }
        ConfigManager.getInstance().init(context, config);
        Logger.i("MSDKAiHelp initialized: appId=" + config.getAppId());
        if (eventListener != null) {
            eventListener.onInitialized(true, "success");
        }
    }

    public static void openChat() {
        openChat(null);
    }

    public static void openChat(ChatConfig chatConfig) {
        checkInitialized();
        Context context = ConfigManager.getInstance().getAppContext();
        Intent intent = new Intent(context, ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (chatConfig != null && chatConfig.getWelcomeMessage() != null) {
            intent.putExtra(ChatActivity.EXTRA_CHAT_CONFIG, chatConfig.getWelcomeMessage());
        }
        context.startActivity(intent);
    }

    public static void openFAQ() {
        openFAQ(null);
    }

    public static void openFAQ(FAQConfig faqConfig) {
        checkInitialized();
        Context context = ConfigManager.getInstance().getAppContext();
        Intent intent = new Intent(context, FAQActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (faqConfig != null) {
            intent.putExtra(FAQActivity.EXTRA_SECTION_ID, faqConfig.getSectionId());
            intent.putExtra(FAQActivity.EXTRA_SHOW_CONTACT, faqConfig.isShowContactUs());
        }
        context.startActivity(intent);
    }

    public static void setUser(UserInfo userInfo) {
        checkInitialized();
        ConfigManager.getInstance().setUserInfo(userInfo);
    }

    public static void clearUser() {
        checkInitialized();
        ConfigManager.getInstance().clearUserInfo();
    }

    public static void getUnreadCount(UnreadCountCallback callback) {
        checkInitialized();
        int localCount = ChatManager.getInstance().getUnreadCount();
        if (localCount > 0) {
            callback.onResult(localCount);
            return;
        }

        String url = ApiService.buildUrl(
                ConfigManager.getInstance().getConfig().getDomain(),
                ApiService.PATH_UNREAD_COUNT);
        HttpClient.getInstance().get(url, Integer.class, new ApiCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                callback.onResult(result != null ? result : 0);
            }
            @Override
            public void onError(int code, String message) {
                callback.onResult(0);
            }
        });
    }

    public static void setEventListener(AiHelpEventListener listener) {
        eventListener = listener;
    }

    public static void setLanguage(String language) {
        checkInitialized();
        ConfigManager.getInstance().setLanguage(language);
    }

    private static void checkInitialized() {
        if (!ConfigManager.getInstance().isInitialized()) {
            throw new IllegalStateException("MSDKAiHelp.init() must be called before using SDK");
        }
    }
}
