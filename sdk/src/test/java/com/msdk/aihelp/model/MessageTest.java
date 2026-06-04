package com.msdk.aihelp.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class MessageTest {

    @Test
    public void createTextMessage_setsFieldsCorrectly() {
        Message msg = Message.createText("Hello", Message.Direction.SEND);

        assertEquals("Hello", msg.getContent());
        assertEquals(Message.MsgType.TEXT, msg.getMsgType());
        assertEquals(Message.Direction.SEND, msg.getDirection());
        assertEquals(Message.Status.SENDING, msg.getStatus());
        assertNotNull(msg.getClientMsgId());
        assertTrue(msg.getTimestamp() > 0);
    }

    @Test
    public void createImageMessage_setsFieldsCorrectly() {
        Message msg = Message.createImage("https://img.com/1.jpg", Message.Direction.SEND);

        assertEquals("https://img.com/1.jpg", msg.getContent());
        assertEquals(Message.MsgType.IMAGE, msg.getMsgType());
        assertEquals(Message.Direction.SEND, msg.getDirection());
    }

    @Test
    public void createSystemMessage_directionIsReceive() {
        Message msg = Message.createSystem("排队中...");

        assertEquals(Message.Direction.RECEIVE, msg.getDirection());
        assertEquals(Message.MsgType.SYSTEM, msg.getMsgType());
    }

    @Test
    public void markSent_updatesStatus() {
        Message msg = Message.createText("Hi", Message.Direction.SEND);
        msg.setStatus(Message.Status.SENT);

        assertEquals(Message.Status.SENT, msg.getStatus());
    }
}
