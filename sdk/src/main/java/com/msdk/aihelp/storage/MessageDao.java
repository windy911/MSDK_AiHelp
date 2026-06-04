package com.msdk.aihelp.storage;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MessageEntity entity);

    @Query("UPDATE messages SET status = :status WHERE client_msg_id = :clientMsgId")
    void updateStatus(String clientMsgId, String status);

    @Query("SELECT * FROM messages WHERE session_id = :sessionId ORDER BY timestamp ASC LIMIT :limit")
    List<MessageEntity> getMessages(String sessionId, int limit);

    @Query("SELECT * FROM messages WHERE session_id = :sessionId AND status = 'SENDING' ORDER BY timestamp ASC")
    List<MessageEntity> getPendingMessages(String sessionId);
}
