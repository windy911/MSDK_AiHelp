package com.msdk.aihelp.chat;

import com.msdk.aihelp.config.ConfigManager;
import com.msdk.aihelp.model.Message;
import com.msdk.aihelp.network.ApiCallback;
import com.msdk.aihelp.network.ApiService;
import com.msdk.aihelp.network.HttpClient;
import com.msdk.aihelp.network.WebSocketClient;
import com.msdk.aihelp.storage.MessageDatabase;
import com.msdk.aihelp.util.ImageCompressor;
import com.msdk.aihelp.util.Logger;
import com.msdk.aihelp.util.ThreadUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatManager implements WebSocketClient.Listener {

    public enum ConnectionState { DISCONNECTED, CONNECTING, CONNECTED }

    public interface ChatCallback {
        void onMessageReceived(Message message);
        void onMessageStatusChanged(String clientMsgId, Message.Status status);
        void onConnectionStateChanged(ConnectionState state);
        void onSessionStarted(String sessionId);
        void onSessionEnded(String reason);
    }

    private static ChatManager instance;

    private final WebSocketClient webSocketClient;
    private final List<Message> messages = new ArrayList<>();
    private ChatCallback callback;
    private ConnectionState connectionState = ConnectionState.DISCONNECTED;
    private String currentSessionId;
    private int unreadCount;

    private ChatManager() {
        webSocketClient = new WebSocketClient();
        webSocketClient.setListener(this);
    }

    public static synchronized ChatManager getInstance() {
        if (instance == null) {
            instance = new ChatManager();
        }
        return instance;
    }

    public void setCallback(ChatCallback callback) {
        this.callback = callback;
    }

    public void connect() {
        setConnectionState(ConnectionState.CONNECTING);
        String token = buildAuthToken();
        webSocketClient.connect(token);
    }

    public void disconnect() {
        webSocketClient.disconnect();
        setConnectionState(ConnectionState.DISCONNECTED);
    }

    public void sendTextMessage(String content) {
        Message msg = Message.createText(content, Message.Direction.SEND);
        addMessage(msg);
        persistMessage(msg);
        webSocketClient.send(msg);
    }

    public void sendImageMessage(File imageFile) {
        Message msg = Message.createImage("file://" + imageFile.getAbsolutePath(), Message.Direction.SEND);
        addMessage(msg);
        if (callback != null) callback.onMessageReceived(msg);

        ThreadUtil.runOnDb(() -> {
            try {
                File cacheDir = ConfigManager.getInstance().getAppContext().getCacheDir();
                File compressed = ImageCompressor.compress(imageFile, cacheDir);
                uploadAndSend(compressed, msg);
            } catch (IOException e) {
                Logger.e("Image compress failed", e);
                ThreadUtil.runOnMain(() -> {
                    msg.setStatus(Message.Status.FAILED);
                    if (callback != null) callback.onMessageStatusChanged(msg.getClientMsgId(), Message.Status.FAILED);
                });
            }
        });
    }

    private void uploadAndSend(File compressedFile, Message placeholderMsg) {
        String uploadUrl = ApiService.buildUrl(
                ConfigManager.getInstance().getConfig().getDomain(),
                ApiService.PATH_UPLOAD);

        HttpClient.getInstance().uploadFile(uploadUrl, compressedFile, new ApiCallback<String>() {
            @Override
            public void onSuccess(String imageUrl) {
                placeholderMsg.setContent(imageUrl);
                placeholderMsg.setStatus(Message.Status.SENT);
                persistMessage(placeholderMsg);
                webSocketClient.send(placeholderMsg);
                if (callback != null) callback.onMessageStatusChanged(placeholderMsg.getClientMsgId(), Message.Status.SENT);
            }

            @Override
            public void onError(int code, String message) {
                Logger.e("Image upload failed: " + code + " " + message, null);
                placeholderMsg.setStatus(Message.Status.FAILED);
                if (callback != null) callback.onMessageStatusChanged(placeholderMsg.getClientMsgId(), Message.Status.FAILED);
            }
        });
    }

    public void loadHistory() {
        if (currentSessionId == null) return;
        ThreadUtil.runOnDb(() -> {
            MessageDatabase db = MessageDatabase.getInstance(
                    ConfigManager.getInstance().getAppContext());
            List<Message> history = db.getMessages(currentSessionId, 100);
            ThreadUtil.runOnMain(() -> {
                messages.clear();
                messages.addAll(history);
            });
        });
    }

    public void resendPendingMessages() {
        if (currentSessionId == null) return;
        ThreadUtil.runOnDb(() -> {
            MessageDatabase db = MessageDatabase.getInstance(
                    ConfigManager.getInstance().getAppContext());
            List<Message> pending = db.getPendingMessages(currentSessionId);
            ThreadUtil.runOnMain(() -> {
                for (Message msg : pending) {
                    webSocketClient.send(msg);
                }
            });
        });
    }

    @Override
    public void onConnected(String sessionId) {
        this.currentSessionId = sessionId;
        setConnectionState(ConnectionState.CONNECTED);
        if (callback != null) callback.onSessionStarted(sessionId);
        resendPendingMessages();
    }

    @Override
    public void onMessage(Message message) {
        addMessage(message);
        persistMessage(message);
        unreadCount++;
        if (callback != null) callback.onMessageReceived(message);
    }

    @Override
    public void onSessionClosed(String reason) {
        if (callback != null) callback.onSessionEnded(reason);
    }

    @Override
    public void onDisconnected() {
        setConnectionState(ConnectionState.DISCONNECTED);
    }

    public List<Message> getMessages() {
        return messages;
    }

    public int getUnreadCount() { return unreadCount; }
    public void resetUnreadCount() { unreadCount = 0; }

    public ConnectionState getConnectionState() { return connectionState; }

    private void addMessage(Message message) {
        messages.add(message);
    }

    private void persistMessage(Message message) {
        if (currentSessionId == null) return;
        MessageDatabase db = MessageDatabase.getInstance(
                ConfigManager.getInstance().getAppContext());
        db.insertMessage(message, currentSessionId);
    }

    private void setConnectionState(ConnectionState state) {
        this.connectionState = state;
        if (callback != null) callback.onConnectionStateChanged(state);
    }

    private String buildAuthToken() {
        ConfigManager cm = ConfigManager.getInstance();
        return cm.getConfig().getAppId() + ":" + cm.getConfig().getAppSecret();
    }
}
