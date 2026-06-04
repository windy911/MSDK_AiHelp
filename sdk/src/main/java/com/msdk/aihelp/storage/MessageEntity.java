package com.msdk.aihelp.storage;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages",
        indices = {@Index(value = {"session_id", "timestamp"})})
public class MessageEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "client_msg_id")
    public String clientMsgId;

    @ColumnInfo(name = "server_msg_id")
    public String serverMsgId;

    @NonNull
    @ColumnInfo(name = "msg_type")
    public String msgType;

    @NonNull
    @ColumnInfo(name = "direction")
    public String direction;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "sender")
    public String sender;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @NonNull
    @ColumnInfo(name = "status")
    public String status;

    @ColumnInfo(name = "session_id")
    public String sessionId;
}
