package com.msdk.aihelp.model;

import java.util.UUID;

public class Message {

    public enum MsgType { TEXT, IMAGE, SYSTEM, LOADING }
    public enum Direction { SEND, RECEIVE }
    public enum Status { SENDING, SENT, FAILED }

    private String clientMsgId;
    private String serverMsgId;
    private MsgType msgType;
    private Direction direction;
    private String content;
    private String sender;
    private long timestamp;
    private Status status;

    private Message() {}

    public static Message createText(String content, Direction direction) {
        Message msg = new Message();
        msg.clientMsgId = UUID.randomUUID().toString();
        msg.msgType = MsgType.TEXT;
        msg.direction = direction;
        msg.content = content;
        msg.timestamp = System.currentTimeMillis();
        msg.status = (direction == Direction.SEND) ? Status.SENDING : Status.SENT;
        return msg;
    }

    public static Message createImage(String imageUrl, Direction direction) {
        Message msg = new Message();
        msg.clientMsgId = UUID.randomUUID().toString();
        msg.msgType = MsgType.IMAGE;
        msg.direction = direction;
        msg.content = imageUrl;
        msg.timestamp = System.currentTimeMillis();
        msg.status = (direction == Direction.SEND) ? Status.SENDING : Status.SENT;
        return msg;
    }

    public static Message createSystem(String content) {
        Message msg = new Message();
        msg.clientMsgId = UUID.randomUUID().toString();
        msg.msgType = MsgType.SYSTEM;
        msg.direction = Direction.RECEIVE;
        msg.content = content;
        msg.timestamp = System.currentTimeMillis();
        msg.status = Status.SENT;
        return msg;
    }

    public static Message createLoading() {
        Message msg = new Message();
        msg.clientMsgId = "loading";
        msg.msgType = MsgType.LOADING;
        msg.direction = Direction.RECEIVE;
        msg.content = "";
        msg.timestamp = System.currentTimeMillis();
        msg.status = Status.SENT;
        return msg;
    }

    public String getClientMsgId() { return clientMsgId; }
    public void setClientMsgId(String clientMsgId) { this.clientMsgId = clientMsgId; }
    public String getServerMsgId() { return serverMsgId; }
    public void setServerMsgId(String serverMsgId) { this.serverMsgId = serverMsgId; }
    public MsgType getMsgType() { return msgType; }
    public void setMsgType(MsgType msgType) { this.msgType = msgType; }
    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
