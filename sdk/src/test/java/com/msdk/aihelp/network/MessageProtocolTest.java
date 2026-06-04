package com.msdk.aihelp.network;

import com.msdk.aihelp.model.Message;

import org.junit.Test;
import static org.junit.Assert.*;

public class MessageProtocolTest {

    @Test
    public void encodeTextMessage_producesCorrectJson() {
        Message msg = Message.createText("Hello", Message.Direction.SEND);
        msg.setClientMsgId("test-id-123");

        String json = MessageProtocol.encode(msg);

        assertTrue(json.contains("\"type\":\"send\""));
        assertTrue(json.contains("\"msgType\":\"text\""));
        assertTrue(json.contains("\"content\":\"Hello\""));
        assertTrue(json.contains("\"clientMsgId\":\"test-id-123\""));
    }

    @Test
    public void encodeImageMessage_producesCorrectJson() {
        Message msg = Message.createImage("https://img.com/1.jpg", Message.Direction.SEND);

        String json = MessageProtocol.encode(msg);

        assertTrue(json.contains("\"msgType\":\"image\""));
        assertTrue(json.contains("\"content\":\"https://img.com/1.jpg\""));
    }

    @Test
    public void decodeTextMessage_parsesCorrectly() {
        String json = "{\"type\":\"receive\",\"msgType\":\"text\",\"content\":\"Hi there\",\"sender\":\"agent\",\"serverMsgId\":\"srv_001\",\"timestamp\":1700000000000}";

        Message msg = MessageProtocol.decode(json);

        assertNotNull(msg);
        assertEquals(Message.MsgType.TEXT, msg.getMsgType());
        assertEquals(Message.Direction.RECEIVE, msg.getDirection());
        assertEquals("Hi there", msg.getContent());
        assertEquals("agent", msg.getSender());
        assertEquals("srv_001", msg.getServerMsgId());
    }

    @Test
    public void decodeSystemMessage_parsesCorrectly() {
        String json = "{\"type\":\"receive\",\"msgType\":\"system\",\"content\":\"正在转接人工客服...\"}";

        Message msg = MessageProtocol.decode(json);

        assertNotNull(msg);
        assertEquals(Message.MsgType.SYSTEM, msg.getMsgType());
        assertEquals("正在转接人工客服...", msg.getContent());
    }

    @Test
    public void encodeConnectMessage_producesCorrectJson() {
        String json = MessageProtocol.encodeConnect("session_abc", "token_xyz");

        assertTrue(json.contains("\"type\":\"connect\""));
        assertTrue(json.contains("\"sessionId\":\"session_abc\""));
        assertTrue(json.contains("\"token\":\"token_xyz\""));
    }

    @Test
    public void encodeHeartbeat_producesCorrectJson() {
        String json = MessageProtocol.encodeHeartbeat();

        assertTrue(json.contains("\"type\":\"heartbeat\""));
    }

    @Test
    public void decodeCloseMessage_parsesCorrectly() {
        String json = "{\"type\":\"close\",\"reason\":\"session_end\"}";

        MessageProtocol.ControlMessage ctrl = MessageProtocol.decodeControl(json);

        assertNotNull(ctrl);
        assertEquals("close", ctrl.type);
        assertEquals("session_end", ctrl.reason);
    }

    @Test
    public void decode_unknownType_returnsNull() {
        String json = "{\"type\":\"unknown\",\"data\":\"test\"}";

        Message msg = MessageProtocol.decode(json);
        assertNull(msg);
    }
}
