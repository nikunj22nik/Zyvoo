package com.business.zyvo.activity;

import android.app.Activity;
import android.content.Context;
import android.util.Log;


import androidx.annotation.Nullable;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.twilio.conversations.Attributes;
import com.twilio.conversations.CallbackListener;
import com.twilio.conversations.Conversation;
import com.twilio.conversations.ConversationListener;
import com.twilio.conversations.ConversationsClient;
import com.twilio.conversations.ConversationsClientListener;
import com.twilio.conversations.ErrorInfo;
import com.twilio.conversations.Message;
import com.twilio.conversations.Participant;
import com.twilio.conversations.ProgressListener;
import com.twilio.conversations.StatusListener;
import com.twilio.conversations.User;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.Channel;
import java.util.ArrayList;
import java.util.List;


import kotlin.io.path.OnErrorResult;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

interface QuickstartConversationsManagerListener {
    void receivedNewMessage();

    void messageSentCallback();

    void reloadMessages();
}

public class QuickstartConversationsManager {

    //    private final static String DEFAULT_CONVERSATION_NAME = "anotherchanz_43_70";
    private String DEFAULT_CONVERSATION_NAME = "general";
    public final static String TAG = "TwilioConversations";
    private String identity;

    private String userid;
    private String typeApiValue;
    private String status = "0";
    String conversationSid = "";

    final private ArrayList<Message> messages = new ArrayList<>();

    private ConversationsClient conversationsClient;

    private Conversation conversation;
    private QuickstartConversationsManagerListener conversationsManagerListener;


    private String tokenURL = "";

    private class TokenResponse {
        String token;
    }



    void initializeWithAccessToken(final Context context, final String token, String DEFAULT_CONVERSATION_NAME, String identity, String userid, String typeApiValue) {
        this.DEFAULT_CONVERSATION_NAME = DEFAULT_CONVERSATION_NAME;
        this.identity = identity;
        this.userid = userid;
        this.typeApiValue = typeApiValue;
        ConversationsClient.Properties props = ConversationsClient.Properties.newBuilder().createProperties();
        ConversationsClient.create(context, token, props, mConversationsClientCallback);
    }


    void sendMessage(String messageBody) {
        if (conversation != null) {
           Conversation.NotificationLevel notificationLevel =  conversation.getNotificationLevel();
            Message.Options options = Message.options().withBody(messageBody);
            options.withAttributes(conversation.getAttributes());
//            Log.d(ChatActivity.TAG,"Message created");

            conversation.sendMessage(options, new CallbackListener<Message>() {
                @Override
                public void onSuccess(Message message) {
                    if (conversationsManagerListener != null) {
                        conversationsManagerListener.messageSentCallback();
                    }
                }
            });
        }
    }


    void sendMessageImage(String messageBody, File file) throws FileNotFoundException {
        if (conversation != null) {
            Message.Options options = Message.options().withMedia(new FileInputStream(messageBody),
                            "jpg/png")
                    .withMediaFileName(file.getName())
                    .withMediaProgressListener(new ProgressListener() {
                        @Override
                        public void onStarted() {

                        }

                        @Override
                        public void onProgress(long bytes) {

                        }

                        @Override
                        public void onCompleted(String mediaSid) {

                        }
                    });
            conversation.sendMessage(options, new CallbackListener<Message>() {
                @Override
                public void onSuccess(Message message) {
                    if (conversationsManagerListener != null) {
                        conversationsManagerListener.messageSentCallback();
                    }
                }
            });
        }


    }


    void sendMessagefile(String messageBody, File file) throws FileNotFoundException {
        if (conversation != null) {
            Message.Options options = Message.options().withMedia(new FileInputStream(messageBody),
                            "application/pdf")
                    .withMediaFileName(file.getName())
                    .withMediaProgressListener(new ProgressListener() {
                        @Override
                        public void onStarted() {

                        }

                        @Override
                        public void onProgress(long bytes) {

                        }

                        @Override
                        public void onCompleted(String mediaSid) {

                        }
                    });
            conversation.sendMessage(options, new CallbackListener<Message>() {
                @Override
                public void onSuccess(Message message) {
                    if (conversationsManagerListener != null) {
                        conversationsManagerListener.messageSentCallback();
                    }
                }
            });
        }


    }


    private void loadChannels() {
        boolean check = false;
        if (conversationsClient == null || conversationsClient.getMyConversations() == null) {
            return;
        }

        conversationsClient.getConversation(DEFAULT_CONVERSATION_NAME, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                if (conversation != null) {
                    Log.d(TAG,conversation.getSid());
                    if (conversation.getStatus() == Conversation.ConversationStatus.JOINED
                        /*|| conversation.getStatus() == Conversation.ConversationStatus.NOT_PARTICIPATING*/) {
                        Log.d(TAG, "Already Exists in Conversation: " + DEFAULT_CONVERSATION_NAME);
                        QuickstartConversationsManager.this.conversation = conversation;
                        QuickstartConversationsManager.this.conversation.addListener(mDefaultConversationListener);
                        QuickstartConversationsManager.this.loadPreviousMessages(conversation);
                    } else {
                        Log.d(TAG, "Joining Conversation: " + DEFAULT_CONVERSATION_NAME);
                        joinConversation(conversation);
                    }

                }
            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                Log.e(TAG, "Error retrieving conversation: " + errorInfo.getMessage());
                createConversation();
            }
        });
    }

    private void join(final Conversation conversation) {
        conversation.join(new StatusListener() {
            @Override
            public void onSuccess() {
                QuickstartConversationsManager.this.conversation = conversation;
                Log.d(TAG, "Joined default conversation");
                QuickstartConversationsManager.this.conversation.addListener(mDefaultConversationListener);
                QuickstartConversationsManager.this.loadPreviousMessages(conversation);
            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                Log.e(TAG, "Error joining conversation: " + errorInfo.getMessage());
            }
        });
    }

    private void createConversation() {
        Log.d(TAG, "Creating Conversation: " + DEFAULT_CONVERSATION_NAME);
        conversationsClient.conversationBuilder().withUniqueName(DEFAULT_CONVERSATION_NAME).build(new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation result) {
                if (result != null) {
                    Log.d(TAG, "Joining Conversation: " + DEFAULT_CONVERSATION_NAME);
                    String conversationSid = result.getSid();


                    Attributes attributes = result.getAttributes();
                    result.addParticipantByIdentity(identity, attributes, new StatusListener() {
                        @Override
                        public void onSuccess() {
                            Log.d("join add :-", "add");
                            joinConversation(result);
                        }

                        @Override
                        public void onError(ErrorInfo errorInfo) {
                            Log.d("join error :-", "error  .." + errorInfo.getMessage());
                            joinConversation(result);
                        }
                    });
                }
            }
        });
    }

    private void joinConversation(final Conversation conversation) {
        Log.d(TAG, "Joining Conversation: " + conversation.getUniqueName());
        if (conversation.getStatus() == Conversation.ConversationStatus.JOINED) {
            QuickstartConversationsManager.this.conversation = conversation;
            Log.d(TAG, "Already joined default conversation");
            QuickstartConversationsManager.this.conversation.addListener(mDefaultConversationListener);
            return;
        }

        conversation.join(new StatusListener() {
            @Override
            public void onSuccess() {
                QuickstartConversationsManager.this.conversation = conversation;
                Log.d(TAG, "Joined default conversation");
                QuickstartConversationsManager.this.conversation.addListener(mDefaultConversationListener);
                QuickstartConversationsManager.this.loadPreviousMessages(conversation);

            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                Log.e(TAG, "Error joining conversation: " + errorInfo.getMessage());
            }
        });


    }

    private void loadPreviousMessages(final Conversation conversation) {
        if (conversation.getLastMessageIndex() != null) {
            conversation.getLastMessages(conversation.getLastMessageIndex().intValue(), new CallbackListener<List<Message>>() {
                @Override
                public void onSuccess(List<Message> result) {
                    messages.addAll(result);
                    conversation.setAllMessagesRead(new CallbackListener<Long>() {
                        @Override
                        public void onSuccess(Long result) {
                            if (conversationsManagerListener != null) {
                                conversationsManagerListener.reloadMessages();
                            }
                        }
                    });
                }
            });
        } else {
            if (conversationsManagerListener != null) {
                conversationsManagerListener.reloadMessages();
            }
        }
    }

    private final ConversationsClientListener mConversationsClientListener = new ConversationsClientListener() {

        @Override
        public void onConversationAdded(Conversation conversation) {
            //   loadChannels();
        }

        @Override
        public void onConversationUpdated(Conversation conversation, Conversation.UpdateReason updateReason) {

        }

        @Override
        public void onConversationDeleted(Conversation conversation) {

        }

        @Override
        public void onConversationSynchronizationChange(Conversation conversation) {

        }

        @Override
        public void onError(ErrorInfo errorInfo) {

        }

        @Override
        public void onUserUpdated(User user, User.UpdateReason updateReason) {

        }

        @Override
        public void onUserSubscribed(User user) {

        }

        @Override
        public void onUserUnsubscribed(User user) {

        }

        @Override
        public void onClientSynchronization(ConversationsClient.SynchronizationStatus synchronizationStatus) {
            if (synchronizationStatus == ConversationsClient.SynchronizationStatus.COMPLETED) {
                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                Log.e(TAG, "Fetching FCM token failed", task.getException());
                                return;
                            }
                            String newToken = task.getResult();
                            Log.d(TAG, "Twilio Identity: " + conversationsClient.getMyUser().getIdentity());
                            Log.d(TAG, "New FCM Token: " + newToken);

                            conversationsClient.registerFCMToken(new ConversationsClient.FCMToken(newToken),
                                    new StatusListener() {
                                        @Override
                                        public void onSuccess() {
                                            Log.d(TAG,"registerFCMToken");
                                        }

                                        @Override
                                        public void onError(ErrorInfo errorInfo) {
                                            StatusListener.super.onError(errorInfo);
                                            Log.d(TAG,errorInfo.getMessage());
                                        }
                                    });
                        });
                loadChannels();
            }
        }

        @Override
        public void onNewMessageNotification(String s, String s1, long l) {

        }

        @Override
        public void onAddedToConversationNotification(String s) {

        }

        @Override
        public void onRemovedFromConversationNotification(String s) {

        }

        @Override
        public void onNotificationSubscribed() {

        }

        @Override
        public void onNotificationFailed(ErrorInfo errorInfo) {

        }

        @Override
        public void onConnectionStateChange(ConversationsClient.ConnectionState connectionState) {

        }

        @Override
        public void onTokenExpired() {

        }

        @Override
        public void onTokenAboutToExpire() {

        }
    };

    private final CallbackListener<ConversationsClient> mConversationsClientCallback
            = new CallbackListener<ConversationsClient>() {
        @Override
        public void onSuccess(ConversationsClient conversationsClient) {
            QuickstartConversationsManager.this.conversationsClient = conversationsClient;

            conversationsClient.addListener(QuickstartConversationsManager.this.mConversationsClientListener);
            Log.d(TAG, "Success creating Twilio Conversations Client");

        }

        @Override
        public void onError(ErrorInfo errorInfo) {
            Log.e(TAG, "Error creating Twilio Conversations Client: " + errorInfo.getMessage());
        }
    };


    public void UpdateStatus() {
        if (conversation != null) {
            conversation.typing();
        }
    }

    private final ConversationListener mDefaultConversationListener = new ConversationListener() {
        @Override
        public void onMessageAdded(final Message message) {
            Log.d(TAG, "Message added");
            messages.add(message);
            if (message.getAuthor().equals("muairspaprovider_" + userid)) {
                conversation.setAllMessagesRead(new CallbackListener<Long>() {
                    @Override
                    public void onSuccess(Long result) {

                    }
                });
            }
            if (conversationsManagerListener != null) {
                conversationsManagerListener.receivedNewMessage();
            }
        }

        @Override
        public void onMessageUpdated(Message message, Message.UpdateReason updateReason) {
            Log.d(TAG, "Message updated: " + message.getMessageBody());
            conversation.setAllMessagesRead(new CallbackListener<Long>() {
                @Override
                public void onSuccess(Long result) {

                }
            });
        }

        @Override
        public void onMessageDeleted(Message message) {
            Log.d(TAG, "Message deleted");
        }

        @Override
        public void onParticipantAdded(Participant participant) {
            Log.d(TAG, "Participant added: " + participant.getIdentity());
        }

        @Override
        public void onParticipantUpdated(Participant participant, Participant.UpdateReason updateReason) {
            Log.d(TAG, "Participant updated: using live chat " + participant.getIdentity() + " " + updateReason.toString());
        }

        @Override
        public void onParticipantDeleted(Participant participant) {
            Log.d(TAG, "Participant deleted: " + participant.getIdentity());
        }

        @Override
        public void onTypingStarted(Conversation conversation, Participant participant) {
//            checkTyping();

            Log.d(TAG, "Started Typing: " + participant.getIdentity());
        }

        @Override
        public void onTypingEnded(Conversation conversation, Participant participant) {
            Log.d(TAG, "Ended Typing: " + participant.getIdentity());
        }

        @Override
        public void onSynchronizationChanged(Conversation conversation) {

        }
    };

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setListener(QuickstartConversationsManagerListener listener) {
        this.conversationsManagerListener = listener;
    }

    public Conversation CheckLastMassageRead() {
        return conversation;
    }

    public void deleteConversation() {
        if (conversation != null) {


        }
    }

}


//    conversation.getParticipantByIdentity("").getSid()
//            conversation.setAllMessagesRead(new CallbackListener<Long>() {
//                @Override
//                public void onSuccess(Long result) {
//
//                }
//            });


