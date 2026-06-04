package com.msdk.aihelp.chat.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.msdk.aihelp.R;
import com.msdk.aihelp.model.Message;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_TEXT_SEND = 0;
    private static final int TYPE_TEXT_RECEIVE = 1;
    private static final int TYPE_IMAGE_SEND = 2;
    private static final int TYPE_IMAGE_RECEIVE = 3;
    private static final int TYPE_SYSTEM = 4;
    private static final int TYPE_LOADING = 5;

    public interface OnImageClickListener {
        void onImageClick(String imageUrl);
    }

    private final List<Message> messages = new ArrayList<>();
    private OnImageClickListener imageClickListener;

    public void setOnImageClickListener(OnImageClickListener listener) {
        this.imageClickListener = listener;
    }

    public void setMessages(List<Message> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void removeLoading() {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).getMsgType() == Message.MsgType.LOADING) {
                messages.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messages.get(position);
        switch (msg.getMsgType()) {
            case TEXT:
                return msg.getDirection() == Message.Direction.SEND ? TYPE_TEXT_SEND : TYPE_TEXT_RECEIVE;
            case IMAGE:
                return msg.getDirection() == Message.Direction.SEND ? TYPE_IMAGE_SEND : TYPE_IMAGE_RECEIVE;
            case SYSTEM:
                return TYPE_SYSTEM;
            case LOADING:
                return TYPE_LOADING;
            default:
                return TYPE_TEXT_RECEIVE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_TEXT_SEND:
                return new TextMessageViewHolder(
                        inflater.inflate(R.layout.aihelp_item_message_text_send, parent, false));
            case TYPE_TEXT_RECEIVE:
                return new TextMessageViewHolder(
                        inflater.inflate(R.layout.aihelp_item_message_text_receive, parent, false));
            case TYPE_IMAGE_SEND:
                return new ImageMessageViewHolder(
                        inflater.inflate(R.layout.aihelp_item_message_image_send, parent, false));
            case TYPE_IMAGE_RECEIVE:
                return new ImageMessageViewHolder(
                        inflater.inflate(R.layout.aihelp_item_message_image_receive, parent, false));
            case TYPE_SYSTEM:
                return new SystemMessageViewHolder(
                        inflater.inflate(R.layout.aihelp_item_message_system, parent, false));
            case TYPE_LOADING:
                return new SystemMessageViewHolder(
                        inflater.inflate(R.layout.aihelp_item_message_loading, parent, false));
            default:
                return new TextMessageViewHolder(
                        inflater.inflate(R.layout.aihelp_item_message_text_receive, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messages.get(position);
        if (holder instanceof TextMessageViewHolder) {
            ((TextMessageViewHolder) holder).bind(msg);
        } else if (holder instanceof ImageMessageViewHolder) {
            ((ImageMessageViewHolder) holder).bind(msg, imageClickListener);
        } else if (holder instanceof SystemMessageViewHolder) {
            ((SystemMessageViewHolder) holder).bind(msg);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
