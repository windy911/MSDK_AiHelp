package com.msdk.aihelp.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.msdk.aihelp.model.Message;

public class MessageProtocol {

    private static final Gson GSON = new Gson();

    public static String encode(Message message) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "send");
        obj.addProperty("msgType", message.getMsgType().name().toLowerCase());
        obj.addProperty("content", message.getContent());
        obj.addProperty("clientMsgId", message.getClientMsgId());
        return GSON.toJson(obj);
    }

    public static String encodeConnect(String sessionId, String token) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "connect");
        obj.addProperty("sessionId", sessionId);
        obj.addProperty("token", token);
        return GSON.toJson(obj);
    }

    public static String encodeHeartbeat() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "heartbeat");
        return GSON.toJson(obj);
    }

    public static Message decode(String json) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        String type = obj.get("type").getAsString();

        if (!"receive".equals(type)) {
            return null;
        }

        String msgTypeStr = obj.get("msgType").getAsString();
        String content = obj.has("content") ? obj.get("content").getAsString() : "";

        Message.MsgType msgType;
        try {
            msgType = Message.MsgType.valueOf(msgTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }

        Message msg;
        if (msgType == Message.MsgType.SYSTEM) {
            msg = Message.createSystem(content);
        } else if (msgType == Message.MsgType.IMAGE) {
            msg = Message.createImage(content, Message.Direction.RECEIVE);
        } else {
            msg = Message.createText(content, Message.Direction.RECEIVE);
        }

        if (obj.has("sender")) {
            msg.setSender(obj.get("sender").getAsString());
        }
        if (obj.has("serverMsgId")) {
            msg.setServerMsgId(obj.get("serverMsgId").getAsString());
        }
        if (obj.has("timestamp")) {
            msg.setTimestamp(obj.get("timestamp").getAsLong());
        }

        return msg;
    }

    public static ControlMessage decodeControl(String json) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        String type = obj.get("type").getAsString();

        if ("close".equals(type) || "connect".equals(type)) {
            ControlMessage ctrl = new ControlMessage();
            ctrl.type = type;
            ctrl.reason = obj.has("reason") ? obj.get("reason").getAsString() : null;
            ctrl.sessionId = obj.has("sessionId") ? obj.get("sessionId").getAsString() : null;
            ctrl.token = obj.has("token") ? obj.get("token").getAsString() : null;
            return ctrl;
        }
        return null;
    }

    public static class ControlMessage {
        public String type;
        public String reason;
        public String sessionId;
        public String token;
    }
}
