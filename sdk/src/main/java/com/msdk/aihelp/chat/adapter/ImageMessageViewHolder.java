package com.msdk.aihelp.chat.adapter;

import android.view.View;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.msdk.aihelp.R;
import com.msdk.aihelp.model.Message;

public class ImageMessageViewHolder extends RecyclerView.ViewHolder {

    private final ImageView ivImage;

    public ImageMessageViewHolder(View itemView) {
        super(itemView);
        ivImage = itemView.findViewById(R.id.iv_image);
    }

    public void bind(Message message, MessageAdapter.OnImageClickListener listener) {
        Glide.with(itemView.getContext())
                .load(message.getContent())
                .centerCrop()
                .into(ivImage);

        if (listener != null) {
            ivImage.setOnClickListener(v -> listener.onImageClick(message.getContent()));
        }
    }
}
