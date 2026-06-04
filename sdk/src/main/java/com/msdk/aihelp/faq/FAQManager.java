package com.msdk.aihelp.faq;

import com.google.gson.reflect.TypeToken;
import com.msdk.aihelp.config.ConfigManager;
import com.msdk.aihelp.model.FAQItem;
import com.msdk.aihelp.model.FAQSection;
import com.msdk.aihelp.network.ApiCallback;
import com.msdk.aihelp.network.ApiService;
import com.msdk.aihelp.network.HttpClient;
import com.msdk.aihelp.storage.CacheManager;

import java.util.List;

public class FAQManager {

    private static FAQManager instance;
    private final CacheManager cacheManager = new CacheManager();

    private FAQManager() {}

    public static synchronized FAQManager getInstance() {
        if (instance == null) {
            instance = new FAQManager();
        }
        return instance;
    }

    public void getSections(ApiCallback<List<FAQSection>> callback) {
        List<FAQSection> cached = cacheManager.get("faq_sections");
        if (cached != null) {
            callback.onSuccess(cached);
            return;
        }

        String url = ApiService.buildUrl(
                ConfigManager.getInstance().getConfig().getDomain(),
                ApiService.PATH_FAQ_SECTIONS);

        HttpClient.getInstance().get(url,
                new TypeToken<List<FAQSection>>(){}.getType(),
                new ApiCallback<List<FAQSection>>() {
                    @Override
                    public void onSuccess(List<FAQSection> result) {
                        cacheManager.putFAQSections(result);
                        callback.onSuccess(result);
                    }

                    @Override
                    public void onError(int code, String message) {
                        callback.onError(code, message);
                    }
                });
    }

    public void getItems(String sectionId, ApiCallback<List<FAQItem>> callback) {
        String url = ApiService.buildUrl(
                ConfigManager.getInstance().getConfig().getDomain(),
                ApiService.PATH_FAQ_ITEMS, sectionId);

        HttpClient.getInstance().get(url,
                new TypeToken<List<FAQItem>>(){}.getType(), callback);
    }

    public void getDetail(String faqId, ApiCallback<FAQItem> callback) {
        FAQItem cached = cacheManager.get("faq_detail_" + faqId);
        if (cached != null) {
            callback.onSuccess(cached);
            return;
        }

        String url = ApiService.buildUrl(
                ConfigManager.getInstance().getConfig().getDomain(),
                ApiService.PATH_FAQ_DETAIL, faqId);

        HttpClient.getInstance().get(url, FAQItem.class, new ApiCallback<FAQItem>() {
            @Override
            public void onSuccess(FAQItem result) {
                cacheManager.putFAQDetail(faqId, result);
                callback.onSuccess(result);
            }

            @Override
            public void onError(int code, String message) {
                callback.onError(code, message);
            }
        });
    }

    public void search(String keyword, ApiCallback<List<FAQItem>> callback) {
        String url = ApiService.buildUrl(
                ConfigManager.getInstance().getConfig().getDomain(),
                ApiService.PATH_FAQ_SEARCH) + "?q=" + keyword;

        HttpClient.getInstance().get(url,
                new TypeToken<List<FAQItem>>(){}.getType(), callback);
    }

    public void submitFeedback(String faqId, boolean helpful) {
        String url = ApiService.buildUrl(
                ConfigManager.getInstance().getConfig().getDomain(),
                ApiService.PATH_FAQ_FEEDBACK, faqId);

        HttpClient.getInstance().post(url,
                new FeedbackBody(helpful), Void.class,
                new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {}
                    @Override
                    public void onError(int code, String message) {}
                });
    }

    private static class FeedbackBody {
        final boolean helpful;
        FeedbackBody(boolean helpful) { this.helpful = helpful; }
    }
}
