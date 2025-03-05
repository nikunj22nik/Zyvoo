package com.business.zyvo.fragment.host;

public interface QuickstartConversationsManagerListener {
    void receivedNewMessage();

    void messageSentCallback();

    void reloadMessages();
}
