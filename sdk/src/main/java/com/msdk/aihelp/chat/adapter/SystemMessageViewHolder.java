package com.msdk.aihelp.chat.adapter;

import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.msdk.aihelp.R;
import com.msdk.aihelp.model.Message;

public class SystemMessageViewHolder extends RecyclerView.ViewHolder {

    private final TextView tvContent;

    public SystemMessageViewHolder(View itemView) {
        super(itemView);
        tvContent = itemView.findViewById(R.id.tv_content);
    }

    public void bind(Message message) {
        if (tvContent != null) {
            tvContent.setText(message.getContent());
        }
    }
}
