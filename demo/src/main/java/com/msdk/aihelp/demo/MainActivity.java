package com.msdk.aihelp.demo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.msdk.aihelp.MSDKAiHelp;
import com.msdk.aihelp.model.UserInfo;

public class MainActivity extends AppCompatActivity {

    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tv_status);
        Button btnChat = findViewById(R.id.btn_chat);
        Button btnFaq = findViewById(R.id.btn_faq);
        Button btnUnread = findViewById(R.id.btn_unread);
        Button btnSetUser = findViewById(R.id.btn_set_user);

        btnChat.setOnClickListener(v -> MSDKAiHelp.openChat());

        btnFaq.setOnClickListener(v -> MSDKAiHelp.openFAQ());

        btnUnread.setOnClickListener(v -> {
            MSDKAiHelp.getUnreadCount(count -> {
                tvStatus.setText("未读消息: " + count);
            });
        });

        btnSetUser.setOnClickListener(v -> {
            UserInfo user = new UserInfo.Builder()
                    .setUserId("player_12345")
                    .setUserName("TestPlayer")
                    .setServerId("server_01")
                    .addCustomData("level", "50")
                    .addCustomData("vip", "3")
                    .build();
            MSDKAiHelp.setUser(user);
            tvStatus.setText("状态: 用户已设置");
            Toast.makeText(this, "用户信息已设置", Toast.LENGTH_SHORT).show();
        });
    }
}
