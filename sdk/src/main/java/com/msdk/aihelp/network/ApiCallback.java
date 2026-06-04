package com.msdk.aihelp.network;

public interface ApiCallback<T> {
    void onSuccess(T result);
    void onError(int code, String message);
}
