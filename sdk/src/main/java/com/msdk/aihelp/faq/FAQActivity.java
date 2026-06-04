package com.msdk.aihelp.faq;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import com.msdk.aihelp.R;
import com.msdk.aihelp.chat.ChatActivity;

public class FAQActivity extends AppCompatActivity {

    public static final String EXTRA_SECTION_ID = "section_id";
    public static final String EXTRA_SHOW_CONTACT = "show_contact";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aihelp_activity_faq);

        if (savedInstanceState == null) {
            String sectionId = getIntent().getStringExtra(EXTRA_SECTION_ID);
            boolean showContact = getIntent().getBooleanExtra(EXTRA_SHOW_CONTACT, true);

            FAQListFragment fragment = FAQListFragment.newInstance(sectionId, showContact);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, fragment);
            ft.commit();
        }
    }

    public void openDetail(String faqId, String question, boolean showContact) {
        FAQDetailFragment fragment = FAQDetailFragment.newInstance(faqId, question, showContact);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void openChat(String faqContext) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_FAQ_CONTEXT, faqContext);
        startActivity(intent);
    }
}
