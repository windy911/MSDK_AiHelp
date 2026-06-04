package com.msdk.aihelp.faq;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.msdk.aihelp.model.FAQItem;
import com.msdk.aihelp.model.FAQSection;
import com.msdk.aihelp.network.ApiCallback;
import com.msdk.aihelp.ui.theme.ThemeManager;
import java.util.List;

public class FAQListFragment extends Fragment {

    private static final String ARG_SECTION_ID = "section_id";
    private static final String ARG_SHOW_CONTACT = "show_contact";
    private static final long SEARCH_DEBOUNCE_MS = 300;

    private RecyclerView recyclerView;
    private EditText etSearch;
    private TextView btnContact;
    private View toolbar;
    private FAQManager faqManager;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    public static FAQListFragment newInstance(String sectionId, boolean showContact) {
        FAQListFragment fragment = new FAQListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SECTION_ID, sectionId);
        args.putBoolean(ARG_SHOW_CONTACT, showContact);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.aihelp_fragment_faq_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar = view.findViewById(R.id.toolbar);
        recyclerView = view.findViewById(R.id.recycler_faq);
        etSearch = view.findViewById(R.id.et_search);
        btnContact = view.findViewById(R.id.btn_contact);
        ImageView btnBack = view.findViewById(R.id.btn_back);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        faqManager = FAQManager.getInstance();

        boolean showContact = getArguments() != null && getArguments().getBoolean(ARG_SHOW_CONTACT, true);
        btnContact.setVisibility(showContact ? View.VISIBLE : View.GONE);

        applyTheme();
        btnBack.setOnClickListener(v -> getActivity().finish());
        btnContact.setOnClickListener(v -> ((FAQActivity) getActivity()).openChat(null));

        setupSearch();
        loadSections();
    }

    private void applyTheme() {
        int primaryColor = ThemeManager.getPrimaryColor();
        toolbar.setBackgroundColor(primaryColor);
        btnContact.setBackgroundColor(primaryColor);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                String query = s.toString().trim();
                if (TextUtils.isEmpty(query)) {
                    loadSections();
                    return;
                }
                searchRunnable = () -> performSearch(query);
                searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_MS);
            }
        });
    }

    private void loadSections() {
        String sectionId = getArguments() != null ? getArguments().getString(ARG_SECTION_ID) : null;

        if (!TextUtils.isEmpty(sectionId)) {
            faqManager.getItems(sectionId, new ApiCallback<List<FAQItem>>() {
                @Override
                public void onSuccess(List<FAQItem> result) {
                    showItems(result);
                }
                @Override
                public void onError(int code, String message) {}
            });
        } else {
            faqManager.getSections(new ApiCallback<List<FAQSection>>() {
                @Override
                public void onSuccess(List<FAQSection> result) {
                    showSections(result);
                }
                @Override
                public void onError(int code, String message) {}
            });
        }
    }

    private void performSearch(String keyword) {
        faqManager.search(keyword, new ApiCallback<List<FAQItem>>() {
            @Override
            public void onSuccess(List<FAQItem> result) {
                showItems(result);
            }
            @Override
            public void onError(int code, String message) {}
        });
    }

    private void showSections(List<FAQSection> sections) {
        recyclerView.setAdapter(new SectionAdapter(sections, section -> {
            faqManager.getItems(section.getSectionId(), new ApiCallback<List<FAQItem>>() {
                @Override
                public void onSuccess(List<FAQItem> result) {
                    showItems(result);
                }
                @Override
                public void onError(int code, String message) {}
            });
        }));
    }

    private void showItems(List<FAQItem> items) {
        boolean showContact = getArguments() != null && getArguments().getBoolean(ARG_SHOW_CONTACT, true);
        recyclerView.setAdapter(new ItemAdapter(items, item -> {
            ((FAQActivity) getActivity()).openDetail(item.getFaqId(), item.getQuestion(), showContact);
        }));
    }

    private static class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.VH> {
        interface OnClick { void onClick(FAQSection s); }
        private final List<FAQSection> data;
        private final OnClick listener;

        SectionAdapter(List<FAQSection> data, OnClick listener) {
            this.data = data;
            this.listener = listener;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.aihelp_item_faq_section, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            FAQSection s = data.get(position);
            holder.tv.setText(s.getTitle());
            holder.itemView.setOnClickListener(v -> listener.onClick(s));
        }

        @Override public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tv;
            VH(View v) { super(v); tv = v.findViewById(R.id.tv_title); }
        }
    }

    private static class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.VH> {
        interface OnClick { void onClick(FAQItem item); }
        private final List<FAQItem> data;
        private final OnClick listener;

        ItemAdapter(List<FAQItem> data, OnClick listener) {
            this.data = data;
            this.listener = listener;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.aihelp_item_faq_item, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            FAQItem item = data.get(position);
            holder.tv.setText(item.getQuestion());
            holder.itemView.setOnClickListener(v -> listener.onClick(item));
        }

        @Override public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tv;
            VH(View v) { super(v); tv = v.findViewById(R.id.tv_question); }
        }
    }
}
