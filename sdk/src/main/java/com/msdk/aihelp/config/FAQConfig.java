package com.msdk.aihelp.config;

public class FAQConfig {

    private final String sectionId;
    private final boolean showContactUs;

    private FAQConfig(Builder builder) {
        this.sectionId = builder.sectionId;
        this.showContactUs = builder.showContactUs;
    }

    public String getSectionId() { return sectionId; }
    public boolean isShowContactUs() { return showContactUs; }

    public static class Builder {
        private String sectionId;
        private boolean showContactUs = true;

        public Builder setSectionId(String sectionId) {
            this.sectionId = sectionId;
            return this;
        }

        public Builder setShowContactUs(boolean showContactUs) {
            this.showContactUs = showContactUs;
            return this;
        }

        public FAQConfig build() {
            return new FAQConfig(this);
        }
    }
}
