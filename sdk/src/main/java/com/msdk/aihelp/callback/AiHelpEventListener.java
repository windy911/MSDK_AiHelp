package com.msdk.aihelp.callback;

public interface AiHelpEventListener {
    void onInitialized(boolean success, String message);
    void onSessionOpened();
    void onSessionClosed();
    void onUnreadCountChanged(int count);
}
