package com.msdk.aihelp.model;

public class FAQItem {

    private String faqId;
    private String question;
    private String answer;
    private int sortOrder;

    public String getFaqId() { return faqId; }
    public void setFaqId(String faqId) { this.faqId = faqId; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
