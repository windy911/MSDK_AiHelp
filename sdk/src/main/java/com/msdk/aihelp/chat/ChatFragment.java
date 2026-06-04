package com.msdk.aihelp.chat;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.msdk.aihelp.R;
import com.msdk.aihelp.chat.adapter.MessageAdapter;
import com.msdk.aihelp.model.Message;
import com.msdk.aihelp.ui.ImagePickerUtil;
import com.msdk.aihelp.ui.ImageViewerActivity;
import com.msdk.aihelp.ui.theme.ThemeManager;

import java.io.File;

public class ChatFragment extends Fragment implements ChatManager.ChatCallback {

    private RecyclerView recyclerMessages;
    private EditText etInput;
    private TextView btnSend;
    private ImageView btnImage;
    private View toolbar;
    private MessageAdapter adapter;
    private ChatManager chatManager;
    private File cameraFile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.aihelp_fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        applyTheme();
        initChat();
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        recyclerMessages = view.findViewById(R.id.recycler_messages);
        etInput = view.findViewById(R.id.et_input);
        btnSend = view.findViewById(R.id.btn_send);
        btnImage = view.findViewById(R.id.btn_image);
        ImageView btnBack = view.findViewById(R.id.btn_back);

        adapter = new MessageAdapter();
        adapter.setOnImageClickListener(this::openImageViewer);
        recyclerMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerMessages.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendTextMessage());
        btnImage.setOnClickListener(v -> ImagePickerUtil.openGallery(this));
        btnBack.setOnClickListener(v -> getActivity().finish());
    }

    private void applyTheme() {
        int primaryColor = ThemeManager.getPrimaryColor();
        toolbar.setBackgroundColor(primaryColor);
        btnSend.setBackgroundColor(primaryColor);
    }

    private void initChat() {
        chatManager = ChatManager.getInstance();
        chatManager.setCallback(this);
        chatManager.connect();

        adapter.setMessages(chatManager.getMessages());
        scrollToBottom();
    }

    private void sendTextMessage() {
        String content = etInput.getText().toString().trim();
        if (TextUtils.isEmpty(content)) return;

        chatManager.sendTextMessage(content);
        etInput.setText("");
        adapter.addMessage(chatManager.getMessages().get(chatManager.getMessages().size() - 1));
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            recyclerMessages.scrollToPosition(adapter.getItemCount() - 1);
        }
    }

    private void openImageViewer(String imageUrl) {
        ImageViewerActivity.start(getContext(), imageUrl);
    }

    @Override
    public void onMessageReceived(Message message) {
        adapter.removeLoading();
        adapter.addMessage(message);
        scrollToBottom();
    }

    @Override
    public void onMessageStatusChanged(String clientMsgId, Message.Status status) {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onConnectionStateChanged(ChatManager.ConnectionState state) {}

    @Override
    public void onSessionStarted(String sessionId) {
        chatManager.loadHistory();
    }

    @Override
    public void onSessionEnded(String reason) {
        adapter.addMessage(Message.createSystem(getString(R.string.aihelp_session_ended)));
        scrollToBottom();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == ImagePickerUtil.REQUEST_IMAGE_PICK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                File file = ImagePickerUtil.uriToFile(getContext(), uri);
                if (file != null) {
                    chatManager.sendImageMessage(file);
                }
            }
        } else if (requestCode == ImagePickerUtil.REQUEST_IMAGE_CAPTURE && cameraFile != null) {
            chatManager.sendImageMessage(cameraFile);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        chatManager.setCallback(null);
        chatManager.disconnect();
    }
}
