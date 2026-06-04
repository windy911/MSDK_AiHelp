package com.msdk.aihelp.network;

import com.msdk.aihelp.config.ConfigManager;
import com.msdk.aihelp.model.Message;
import com.msdk.aihelp.util.Logger;
import com.msdk.aihelp.util.ThreadUtil;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketClient {

    public interface Listener {
        void onConnected(String sessionId);
        void onMessage(Message message);
        void onSessionClosed(String reason);
        void onDisconnected();
    }

    private static final int NORMAL_CLOSE_CODE = 1000;
    private static final long MAX_RECONNECT_DELAY_MS = 30000;
    private static final long HEARTBEAT_INTERVAL_MS = 30000;

    private final OkHttpClient okHttpClient;
    private WebSocket webSocket;
    private Listener listener;
    private String sessionId;
    private String token;
    private boolean intentionallyClosed;
    private int reconnectAttempt;

    public WebSocketClient() {
        this.okHttpClient = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void connect(String token) {
        this.token = token;
        this.intentionallyClosed = false;
        this.reconnectAttempt = 0;
        doConnect();
    }

    private void doConnect() {
        ConfigManager cm = ConfigManager.getInstance();
        String domain = cm.getConfig().getDomain();
        String wsUrl = domain.replace("https://", "wss://").replace("http://", "ws://")
                + "/ws/chat";

        Request request = new Request.Builder()
                .url(wsUrl)
                .addHeader("X-App-Id", cm.getConfig().getAppId())
                .addHeader("X-App-Secret", cm.getConfig().getAppSecret())
                .addHeader("X-Token", token)
                .build();

        webSocket = okHttpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                Logger.d("WebSocket connected");
                reconnectAttempt = 0;
                if (sessionId != null) {
                    ws.send(MessageProtocol.encodeConnect(sessionId, token));
                }
                startHeartbeat();
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                handleMessage(text);
            }

            @Override
            public void onClosing(WebSocket ws, int code, String reason) {
                ws.close(NORMAL_CLOSE_CODE, null);
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                Logger.d("WebSocket closed: " + reason);
                stopHeartbeat();
                if (!intentionallyClosed) {
                    scheduleReconnect();
                }
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                Logger.e("WebSocket failure", t);
                stopHeartbeat();
                if (!intentionallyClosed) {
                    scheduleReconnect();
                }
                ThreadUtil.runOnMain(() -> {
                    if (listener != null) listener.onDisconnected();
                });
            }
        });
    }

    private void handleMessage(String text) {
        MessageProtocol.ControlMessage ctrl = MessageProtocol.decodeControl(text);
        if (ctrl != null) {
            if ("connect".equals(ctrl.type)) {
                sessionId = ctrl.sessionId;
                ThreadUtil.runOnMain(() -> {
                    if (listener != null) listener.onConnected(sessionId);
                });
            } else if ("close".equals(ctrl.type)) {
                ThreadUtil.runOnMain(() -> {
                    if (listener != null) listener.onSessionClosed(ctrl.reason);
                });
            }
            return;
        }

        Message msg = MessageProtocol.decode(text);
        if (msg != null) {
            ThreadUtil.runOnMain(() -> {
                if (listener != null) listener.onMessage(msg);
            });
        }
    }

    public void send(Message message) {
        if (webSocket != null) {
            String json = MessageProtocol.encode(message);
            webSocket.send(json);
        }
    }

    public void disconnect() {
        intentionallyClosed = true;
        stopHeartbeat();
        if (webSocket != null) {
            webSocket.close(NORMAL_CLOSE_CODE, "user_close");
            webSocket = null;
        }
    }

    private void scheduleReconnect() {
        reconnectAttempt++;
        long delay = Math.min(
                (long) Math.pow(2, reconnectAttempt) * 1000,
                MAX_RECONNECT_DELAY_MS);
        Logger.d("Reconnecting in " + delay + "ms (attempt " + reconnectAttempt + ")");
        ThreadUtil.getMainHandler().postDelayed(this::doConnect, delay);
    }

    private Runnable heartbeatRunnable;

    private void startHeartbeat() {
        heartbeatRunnable = new Runnable() {
            @Override
            public void run() {
                if (webSocket != null && !intentionallyClosed) {
                    webSocket.send(MessageProtocol.encodeHeartbeat());
                    ThreadUtil.getMainHandler().postDelayed(this, HEARTBEAT_INTERVAL_MS);
                }
            }
        };
        ThreadUtil.getMainHandler().postDelayed(heartbeatRunnable, HEARTBEAT_INTERVAL_MS);
    }

    private void stopHeartbeat() {
        if (heartbeatRunnable != null) {
            ThreadUtil.getMainHandler().removeCallbacks(heartbeatRunnable);
            heartbeatRunnable = null;
        }
    }

    public String getSessionId() { return sessionId; }
}
