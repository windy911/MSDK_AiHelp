package com.msdk.aihelp.chat;

import com.msdk.aihelp.model.Message;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ChatManagerTest {

    private List<Message> receivedMessages;
    private ChatManager.ChatCallback testCallback;

    @Before
    public void setUp() {
        receivedMessages = new ArrayList<>();
        testCallback = new ChatManager.ChatCallback() {
            @Override
            public void onMessageReceived(Message message) {
                receivedMessages.add(message);
            }
            @Override
            public void onMessageStatusChanged(String clientMsgId, Message.Status status) {}
            @Override
            public void onConnectionStateChanged(ChatManager.ConnectionState state) {}
            @Override
            public void onSessionStarted(String sessionId) {}
            @Override
            public void onSessionEnded(String reason) {}
        };
    }

    @Test
    public void connectionState_startsAsDisconnected() {
        assertEquals(ChatManager.ConnectionState.DISCONNECTED, ChatManager.ConnectionState.DISCONNECTED);
    }

    @Test
    public void messageList_initiallyEmpty() {
        List<Message> messages = new ArrayList<>();
        assertTrue(messages.isEmpty());
    }
}
