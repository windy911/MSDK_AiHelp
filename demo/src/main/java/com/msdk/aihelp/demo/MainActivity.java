package com.msdk.aihelp.demo;

import android.os.Bundle;
import android.view.View;
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

        setupThemeColors();
    }

    private void setupThemeColors() {
        View colorBlue = findViewById(R.id.color_blue);
        View colorRed = findViewById(R.id.color_red);
        View colorGreen = findViewById(R.id.color_green);
        View colorPurple = findViewById(R.id.color_purple);

        colorBlue.setOnClickListener(v -> applyTheme(0xFF1A73E8, "蓝色"));
        colorRed.setOnClickListener(v -> applyTheme(0xFFE53935, "红色"));
        colorGreen.setOnClickListener(v -> applyTheme(0xFF43A047, "绿色"));
        colorPurple.setOnClickListener(v -> applyTheme(0xFF8E24AA, "紫色"));
    }

    private void applyTheme(int color, String name) {
        MSDKAiHelp.setThemeColor(color);
        tvStatus.setText("主题色: " + name);
        Toast.makeText(this, "已切换为" + name + "主题，打开聊天或FAQ查看效果", Toast.LENGTH_SHORT).show();
    }
}
