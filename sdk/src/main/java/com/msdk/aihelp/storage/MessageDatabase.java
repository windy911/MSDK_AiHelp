package com.msdk.aihelp.storage;

import android.content.Context;

import com.msdk.aihelp.model.Message;
import com.msdk.aihelp.util.ThreadUtil;

import java.util.ArrayList;
import java.util.List;

public class MessageDatabase {

    private static MessageDatabase instance;
    private final MessageDao dao;

    public static synchronized MessageDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new MessageDatabase(context.getApplicationContext());
        }
        return instance;
    }

    private MessageDatabase(Context context) {
        dao = AppDatabase.getInstance(context).messageDao();
    }

    public void insertMessage(Message message, String sessionId) {
        ThreadUtil.runOnDb(() -> dao.insert(toEntity(message, sessionId)));
    }

    public void updateMessageStatus(String clientMsgId, Message.Status status) {
        ThreadUtil.runOnDb(() -> dao.updateStatus(clientMsgId, status.name()));
    }

    public List<Message> getMessages(String sessionId, int limit) {
        List<MessageEntity> entities = dao.getMessages(sessionId, limit);
        return toMessages(entities);
    }

    public List<Message> getPendingMessages(String sessionId) {
        List<MessageEntity> entities = dao.getPendingMessages(sessionId);
        return toMessages(entities);
    }

    private static MessageEntity toEntity(Message message, String sessionId) {
        MessageEntity entity = new MessageEntity();
        entity.clientMsgId = message.getClientMsgId();
        entity.serverMsgId = message.getServerMsgId();
        entity.msgType = message.getMsgType().name();
        entity.direction = message.getDirection().name();
        entity.content = message.getContent();
        entity.sender = message.getSender();
        entity.timestamp = message.getTimestamp();
        entity.status = message.getStatus().name();
        entity.sessionId = sessionId;
        return entity;
    }

    private static List<Message> toMessages(List<MessageEntity> entities) {
        List<Message> messages = new ArrayList<>(entities.size());
        for (MessageEntity e : entities) {
            Message msg = Message.createText("", Message.Direction.SEND);
            msg.setClientMsgId(e.clientMsgId);
            msg.setServerMsgId(e.serverMsgId);
            msg.setMsgType(Message.MsgType.valueOf(e.msgType));
            msg.setDirection(Message.Direction.valueOf(e.direction));
            msg.setContent(e.content);
            msg.setSender(e.sender);
            msg.setTimestamp(e.timestamp);
            msg.setStatus(Message.Status.valueOf(e.status));
            messages.add(msg);
        }
        return messages;
    }
}
