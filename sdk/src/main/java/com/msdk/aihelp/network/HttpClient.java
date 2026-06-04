package com.msdk.aihelp.network;

import com.google.gson.Gson;
import com.msdk.aihelp.config.ConfigManager;
import com.msdk.aihelp.util.Logger;
import com.msdk.aihelp.util.ThreadUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpClient {

    private static HttpClient instance;
    private final OkHttpClient client;
    private final Gson gson;

    private HttpClient() {
        client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        gson = new Gson();
    }

    public static synchronized HttpClient getInstance() {
        if (instance == null) {
            instance = new HttpClient();
        }
        return instance;
    }

    public <T> void get(String url, Type type, ApiCallback<T> callback) {
        Request request = newRequestBuilder(url).get().build();
        enqueue(request, type, callback);
    }

    public <T> void post(String url, Object body, Type type, ApiCallback<T> callback) {
        String json = gson.toJson(body);
        RequestBody requestBody = RequestBody.create(json,
                MediaType.parse("application/json; charset=utf-8"));
        Request request = newRequestBuilder(url).post(requestBody).build();
        enqueue(request, type, callback);
    }

    public void uploadFile(String url, File file, ApiCallback<String> callback) {
        RequestBody fileBody = RequestBody.create(file, MediaType.parse("image/jpeg"));
        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .build();
        Request request = newRequestBuilder(url).post(body).build();
        enqueue(request, String.class, callback);
    }

    private Request.Builder newRequestBuilder(String url) {
        Request.Builder builder = new Request.Builder().url(url);
        ConfigManager cm = ConfigManager.getInstance();
        if (cm.getConfig() != null) {
            builder.addHeader("X-App-Id", cm.getConfig().getAppId());
            builder.addHeader("X-App-Secret", cm.getConfig().getAppSecret());
        }
        if (cm.getUserInfo() != null) {
            builder.addHeader("X-User-Id", cm.getUserInfo().getUserId());
        }
        if (cm.getLanguage() != null) {
            builder.addHeader("X-Language", cm.getLanguage());
        }
        return builder;
    }

    private <T> void enqueue(Request request, Type type, ApiCallback<T> callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.e("HTTP request failed: " + request.url(), e);
                ThreadUtil.runOnMain(() -> callback.onError(-1, e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = response.body() != null ? response.body().string() : "";
                    if (!response.isSuccessful()) {
                        ThreadUtil.runOnMain(() -> callback.onError(response.code(), body));
                        return;
                    }
                    T result = gson.fromJson(body, type);
                    ThreadUtil.runOnMain(() -> callback.onSuccess(result));
                } catch (Exception e) {
                    Logger.e("HTTP parse failed", e);
                    ThreadUtil.runOnMain(() -> callback.onError(-2, e.getMessage()));
                }
            }
        });
    }

    public OkHttpClient getOkHttpClient() {
        return client;
    }
}
