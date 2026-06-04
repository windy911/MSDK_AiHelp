package com.msdk.aihelp.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.msdk.aihelp.model.Message;
import com.msdk.aihelp.util.ThreadUtil;

import java.util.ArrayList;
import java.util.List;

public class MessageDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "msdk_aihelp_messages.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_MESSAGES = "messages";
    private static final String COL_CLIENT_MSG_ID = "client_msg_id";
    private static final String COL_SERVER_MSG_ID = "server_msg_id";
    private static final String COL_MSG_TYPE = "msg_type";
    private static final String COL_DIRECTION = "direction";
    private static final String COL_CONTENT = "content";
    private static final String COL_SENDER = "sender";
    private static final String COL_TIMESTAMP = "timestamp";
    private static final String COL_STATUS = "status";
    private static final String COL_SESSION_ID = "session_id";

    private static MessageDatabase instance;

    public static synchronized MessageDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new MessageDatabase(context.getApplicationContext());
        }
        return instance;
    }

    private MessageDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_MESSAGES + " ("
                + COL_CLIENT_MSG_ID + " TEXT PRIMARY KEY, "
                + COL_SERVER_MSG_ID + " TEXT, "
                + COL_MSG_TYPE + " TEXT NOT NULL, "
                + COL_DIRECTION + " TEXT NOT NULL, "
                + COL_CONTENT + " TEXT, "
                + COL_SENDER + " TEXT, "
                + COL_TIMESTAMP + " INTEGER NOT NULL, "
                + COL_STATUS + " TEXT NOT NULL, "
                + COL_SESSION_ID + " TEXT"
                + ")");
        db.execSQL("CREATE INDEX idx_session_time ON " + TABLE_MESSAGES
                + " (" + COL_SESSION_ID + ", " + COL_TIMESTAMP + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    public void insertMessage(Message message, String sessionId) {
        ThreadUtil.runOnDb(() -> {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_CLIENT_MSG_ID, message.getClientMsgId());
            cv.put(COL_SERVER_MSG_ID, message.getServerMsgId());
            cv.put(COL_MSG_TYPE, message.getMsgType().name());
            cv.put(COL_DIRECTION, message.getDirection().name());
            cv.put(COL_CONTENT, message.getContent());
            cv.put(COL_SENDER, message.getSender());
            cv.put(COL_TIMESTAMP, message.getTimestamp());
            cv.put(COL_STATUS, message.getStatus().name());
            cv.put(COL_SESSION_ID, sessionId);
            db.insertWithOnConflict(TABLE_MESSAGES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        });
    }

    public void updateMessageStatus(String clientMsgId, Message.Status status) {
        ThreadUtil.runOnDb(() -> {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_STATUS, status.name());
            db.update(TABLE_MESSAGES, cv,
                    COL_CLIENT_MSG_ID + " = ?", new String[]{clientMsgId});
        });
    }

    public List<Message> getMessages(String sessionId, int limit) {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGES, null,
                COL_SESSION_ID + " = ?", new String[]{sessionId},
                null, null, COL_TIMESTAMP + " ASC",
                String.valueOf(limit));

        while (cursor.moveToNext()) {
            messages.add(cursorToMessage(cursor));
        }
        cursor.close();
        return messages;
    }

    public List<Message> getPendingMessages(String sessionId) {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGES, null,
                COL_SESSION_ID + " = ? AND " + COL_STATUS + " = ?",
                new String[]{sessionId, Message.Status.SENDING.name()},
                null, null, COL_TIMESTAMP + " ASC", null);

        while (cursor.moveToNext()) {
            messages.add(cursorToMessage(cursor));
        }
        cursor.close();
        return messages;
    }

    private Message cursorToMessage(Cursor cursor) {
        Message msg = Message.createText("", Message.Direction.SEND);
        msg.setClientMsgId(cursor.getString(cursor.getColumnIndexOrThrow(COL_CLIENT_MSG_ID)));
        msg.setServerMsgId(cursor.getString(cursor.getColumnIndexOrThrow(COL_SERVER_MSG_ID)));
        msg.setMsgType(Message.MsgType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COL_MSG_TYPE))));
        msg.setDirection(Message.Direction.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COL_DIRECTION))));
        msg.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT)));
        msg.setSender(cursor.getString(cursor.getColumnIndexOrThrow(COL_SENDER)));
        msg.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP)));
        msg.setStatus(Message.Status.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS))));
        return msg;
    }
}
