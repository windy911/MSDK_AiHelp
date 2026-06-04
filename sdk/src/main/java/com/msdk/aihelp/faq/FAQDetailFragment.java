package com.msdk.aihelp.faq;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.msdk.aihelp.R;
import com.msdk.aihelp.model.FAQItem;
import com.msdk.aihelp.network.ApiCallback;
import com.msdk.aihelp.ui.theme.ThemeManager;

public class FAQDetailFragment extends Fragment {

    private static final String ARG_FAQ_ID = "faq_id";
    private static final String ARG_QUESTION = "question";
    private static final String ARG_SHOW_CONTACT = "show_contact";

    private WebView webView;
    private TextView btnContact;
    private View toolbar;
    private String faqId;

    public static FAQDetailFragment newInstance(String faqId, String question, boolean showContact) {
        FAQDetailFragment fragment = new FAQDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FAQ_ID, faqId);
        args.putString(ARG_QUESTION, question);
        args.putBoolean(ARG_SHOW_CONTACT, showContact);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.aihelp_fragment_faq_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar = view.findViewById(R.id.toolbar);
        webView = view.findViewById(R.id.webview_content);
        btnContact = view.findViewById(R.id.btn_contact);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        ImageView btnBack = view.findViewById(R.id.btn_back);
        TextView btnHelpful = view.findViewById(R.id.btn_helpful);
        TextView btnNotHelpful = view.findViewById(R.id.btn_not_helpful);

        faqId = getArguments().getString(ARG_FAQ_ID);
        String question = getArguments().getString(ARG_QUESTION, "");
        boolean showContact = getArguments().getBoolean(ARG_SHOW_CONTACT, true);

        tvTitle.setText(question);
        btnContact.setVisibility(showContact ? View.VISIBLE : View.GONE);

        applyTheme();
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnContact.setOnClickListener(v ->
                ((FAQActivity) getActivity()).openChat("FAQ: " + question));
        btnHelpful.setOnClickListener(v -> submitFeedback(true));
        btnNotHelpful.setOnClickListener(v -> submitFeedback(false));

        loadDetail();
    }

    private void applyTheme() {
        int primaryColor = ThemeManager.getPrimaryColor();
        toolbar.setBackgroundColor(primaryColor);
        btnContact.setBackgroundColor(primaryColor);
    }

    private void loadDetail() {
        FAQManager.getInstance().getDetail(faqId, new ApiCallback<FAQItem>() {
            @Override
            public void onSuccess(FAQItem result) {
                String html = "<html><head>"
                        + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                        + "<style>body{padding:16px;font-size:15px;line-height:1.6;color:#212121;}"
                        + "img{max-width:100%;height:auto;}</style>"
                        + "</head><body>" + result.getAnswer() + "</body></html>";
                webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
            }

            @Override
            public void onError(int code, String message) {}
        });
    }

    private void submitFeedback(boolean helpful) {
        FAQManager.getInstance().submitFeedback(faqId, helpful);
        View feedbackLayout = getView().findViewById(R.id.layout_feedback);
        if (feedbackLayout != null) {
            feedbackLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webView != null) {
            webView.destroy();
        }
    }
}
