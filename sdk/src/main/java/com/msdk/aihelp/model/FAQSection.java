package com.msdk.aihelp.model;

import java.util.List;

public class FAQSection {

    private String sectionId;
    private String title;
    private int sortOrder;
    private List<FAQItem> items;

    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public List<FAQItem> getItems() { return items; }
    public void setItems(List<FAQItem> items) { this.items = items; }
}
