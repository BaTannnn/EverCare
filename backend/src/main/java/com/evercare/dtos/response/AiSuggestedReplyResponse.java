package com.evercare.dtos.response;

public class AiSuggestedReplyResponse {

    private String suggestedReply;
    private String replyType;
    private String warning;
    private String disclaimer;

    public String getSuggestedReply() {
        return suggestedReply;
    }

    public void setSuggestedReply(String suggestedReply) {
        this.suggestedReply = suggestedReply;
    }

    public String getReplyType() {
        return replyType;
    }

    public void setReplyType(String replyType) {
        this.replyType = replyType;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
