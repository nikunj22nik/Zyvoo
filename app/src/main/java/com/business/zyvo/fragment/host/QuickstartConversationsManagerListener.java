package com.business.zyvo.fragment.host;

public interface QuickstartConversationsManagerListener {
    void receivedNewMessage();

    void messageSentCallback();

    void reloadMessages();
    void showError(String message);
    void notInit();
}
