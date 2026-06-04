package com.msdk.aihelp.chat;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import com.msdk.aihelp.R;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_CHAT_CONFIG = "chat_config_welcome";
    public static final String EXTRA_FAQ_CONTEXT = "faq_context";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aihelp_activity_chat);

        if (savedInstanceState == null) {
            ChatFragment fragment = new ChatFragment();
            fragment.setArguments(getIntent().getExtras());
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, fragment);
            ft.commit();
        }
    }
}
