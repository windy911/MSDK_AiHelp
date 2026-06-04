package com.msdk.aihelp.chat.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.msdk.aihelp.R;
import com.msdk.aihelp.model.Message;

public class TextMessageViewHolder extends RecyclerView.ViewHolder {

    private final TextView tvContent;
    private final ImageView ivStatus;

    public TextMessageViewHolder(View itemView) {
        super(itemView);
        tvContent = itemView.findViewById(R.id.tv_content);
        ivStatus = itemView.findViewById(R.id.iv_status);
    }

    public void bind(Message message) {
        tvContent.setText(message.getContent());
        if (ivStatus != null && message.getDirection() == Message.Direction.SEND) {
            if (message.getStatus() == Message.Status.FAILED) {
                ivStatus.setVisibility(View.VISIBLE);
                ivStatus.setImageResource(android.R.drawable.ic_dialog_alert);
            } else {
                ivStatus.setVisibility(View.GONE);
            }
        }
    }
}
