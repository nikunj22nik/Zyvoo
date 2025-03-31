package com.business.zyvo.fragment.host;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.twilio.conversations.CallbackListener;
import com.twilio.conversations.Conversation;
import com.twilio.conversations.ConversationListener;
import com.twilio.conversations.ConversationsClient;
import com.twilio.conversations.ConversationsClientListener;
import com.twilio.conversations.ErrorInfo;
import com.twilio.conversations.Message;
import com.twilio.conversations.Participant;
import com.twilio.conversations.StatusListener;
import com.twilio.conversations.User;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

interface TokenResponseListener {
    void receivedTokenResponse(boolean success, @Nullable Exception exception);
}


interface AccessTokenListenerOne {
    void receivedAccessToken(@Nullable String token, @Nullable Exception exception);
}


public class QuickstartConversationsManager {

    //    private final static String DEFAULT_CONVERSATION_NAME = "anotherchanz_43_70";
    private String DEFAULT_CONVERSATION_NAME = "general";
    public final static String TAG = "TwilioConversations";


    private String userid;
    private String status = "0";

    final private ArrayList<Message> messages = new ArrayList<>();
    final private ArrayList<Message> updatelistmessages = new ArrayList<>();

    private ConversationsClient conversationsClient;

    private Conversation conversation;
    private QuickstartConversationsManagerListener conversationsManagerListener;


    private String tokenURL = "";

    private class TokenResponse {
        String token;
    }

    void retrieveAccessTokenFromServer(final Context context, String identity, final TokenResponseListener listener) {

        // Set the chat token URL in your strings.xml file
//        String chatTokenURL = context.getString(R.string.chat_token_url);
        String chatTokenURL = "";

        if ("https://anotherchanz.yesitlabs.co/public/chat/get_access_token".equals(chatTokenURL)) {
            listener.receivedTokenResponse(true, new Exception("You need to replace the chat token URL in strings.xml"));
            return;
        }

        tokenURL = chatTokenURL + "?identity=" + identity;

        new Thread(new Runnable() {
            @Override
            public void run() {
                retrieveToken(new AccessTokenListenerOne() {
                    @Override
                    public void receivedAccessToken(@Nullable String token, @Nullable Exception exception) {
                        if (token != null) {
                            ConversationsClient.Properties props = ConversationsClient.Properties.newBuilder().createProperties();
                            ConversationsClient.create(context, token, props, mConversationsClientCallback);
                            listener.receivedTokenResponse(true, null);
                        } else {
                            listener.receivedTokenResponse(false, exception);
                        }
                    }
                });
            }
        }).start();

    }

   public void initializeWithAccessToken(final Context context, final String token, String DEFAULT_CONVERSATION_NAME, String userid) {
        this.DEFAULT_CONVERSATION_NAME = DEFAULT_CONVERSATION_NAME;
        this.userid = userid;
        ConversationsClient.Properties props = ConversationsClient.Properties.newBuilder().createProperties();
        ConversationsClient.create(context, token, props, mConversationsClientCallback);
    }


    private void retrieveToken(AccessTokenListenerOne listener) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(tokenURL)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String responseBody = "";
            if (response != null && response.body() != null) {
                responseBody = response.body().string();
            }
            Log.d(TAG, "Response from server: " + responseBody);
            Gson gson = new Gson();
            TokenResponse tokenResponse = gson.fromJson(responseBody, TokenResponse.class);
            String accessToken = tokenResponse.token;
            Log.d(TAG, "Retrieved access token from server: " + accessToken);
            listener.receivedAccessToken(accessToken, null);
        } catch (IOException ex) {
            Log.e(TAG, ex.getLocalizedMessage(), ex);
            listener.receivedAccessToken(null, ex);
        }
    }

    public void loadChatList(){
        if (conversationsClient!=null) {
            loadAllChannel(conversationsClient);
        }else {
            conversationsManagerListener.notInit();
            Log.d("******", "conversationsClient = null");
        }

    }


    private void loadChannels() {
        boolean check = false;
        if (conversationsClient == null || conversationsClient.getMyConversations() == null) {
            return;
        }

        loadAllChannel(conversationsClient);


//        conversationsClient.getConversation(DEFAULT_CONVERSATION_NAME, new CallbackListener<Conversation>() {
//            @Override
//            public void onSuccess(Conversation conversation) {
//                if (conversation != null) {
//                    if (conversation.getStatus() == Conversation.ConversationStatus.JOINED /*|| conversation.getStatus() == Conversation.ConversationStatus.NOT_PARTICIPATING*/) {
//                        Log.d(TAG, "Already Exists in Conversation: " + DEFAULT_CONVERSATION_NAME);
//                        QuickstartConversationsManager.this.conversation = conversation;
//                        QuickstartConversationsManager.this.conversation.addListener(mDefaultConversationListener);
//                        QuickstartConversationsManager.this.loadPreviousMessages(conversation);
//                    } else {
//                        Log.d(TAG, "Joining Conversation: " + DEFAULT_CONVERSATION_NAME);
//                       joinConversation(conversation);
//                    }
//                }
//            }
//
//            @Override
//            public void onError(ErrorInfo errorInfo) {
//                Log.e(TAG, "Error retrieving conversation: " + errorInfo.getMessage());
//                createConversation();
//            }
//        });
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
                if (conversationsManagerListener!=null) {
                    conversationsManagerListener.showError(errorInfo.getMessage());
                }
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
                    joinConversation(result);
                }
            }
        });
    }

    private void joinConversation(final Conversation conversation) {
        Log.d(TAG, "Joining Conversation: " + conversation.getUniqueName());
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
        loadAllChannel(conversationsClient);
    }


    public  ConversationsClient getConversationsClient(){
        return conversationsClient;
    }

    public List<Conversation> loadAllChannel(ConversationsClient conversationsClient) {
        if (messages != null) {
            messages.clear();
        }
        for (Conversation data : conversationsClient.getMyConversations()) {
            Log.d("******", "list" + data.getUniqueName().toString());
            try {
                if (data.getLastMessageIndex()!=null){
                    data.getLastMessages(0, result -> {
                        messages.addAll(result);
                        if (conversationsManagerListener != null) {
                            conversationsManagerListener.reloadMessages();
                        }
                    });
                }else {
                    if (conversationsManagerListener != null) {
                        conversationsManagerListener.reloadMessages();
                    }
                }

            } catch (Exception e) {
                if (conversationsManagerListener != null) {
                    conversationsManagerListener.reloadMessages();
                }
                Log.d("error", "msg" + e.getMessage().toString());
            }
        }
        return conversationsClient.getMyConversations();
    }

    private final ConversationsClientListener mConversationsClientListener = new ConversationsClientListener() {

        @Override
        public void onConversationAdded(Conversation conversation) {

        }

        @Override
        public void onConversationUpdated(Conversation conversation, Conversation.UpdateReason updateReason) {
            messages.clear();
            if (conversation.getLastMessageIndex() != null) {
                conversation.getMessageByIndex(conversation.getLastMessageIndex().intValue(), new CallbackListener<Message>() {
                    @Override
                    public void onSuccess(Message result) {
                        Log.d("*******","onConversationUpdated");
                        messages.add(result);
                        if (conversationsManagerListener != null) {
                            conversationsManagerListener.receivedNewMessage();
                        }
                    }
                });
            }

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
            retrieveToken(new AccessTokenListenerOne() {
                @Override
                public void receivedAccessToken(@Nullable String token, @Nullable Exception exception) {
                    if (token != null) {
                        conversationsClient.updateToken(token, new StatusListener() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "Refreshed access token.");
                            }
                        });
                    }
                }
            });
        }
    };

    private final CallbackListener<ConversationsClient> mConversationsClientCallback = new CallbackListener<ConversationsClient>() {
        @Override
        public void onSuccess(ConversationsClient conversationsClient) {
            QuickstartConversationsManager.this.conversationsClient = conversationsClient;
            conversationsClient.addListener(QuickstartConversationsManager.this.mConversationsClientListener);
            Log.d(TAG, "Success creating Twilio Conversations Client");
           /* conversationsClient.registerFCMToken(new ConversationsClient.FCMToken("dUv0FWUvTeyYOOPthFSZaj:APA91bHEPxtsx5rkxsDuZN3EhVXcCa2nDrbPu-8dSxF3mpbZaL2lG6WruM4KXWWvTFkX0-IN6sKEL94I_opMUZxu0sQuQoDBYouiKTH8wg3nTWyuJ4Bw6NM"), new StatusListener() {
                @Override
                public void onSuccess() {
                Log.d(TAG,"registerFCMToken");
                }

                @Override
                public void onError(ErrorInfo errorInfo) {
                    StatusListener.super.onError(errorInfo);
                    Log.d(TAG,errorInfo.getMessage());
                }
            });*/
//            loadAllChannel(conversationsClient);
        }

        @Override
        public void onError(ErrorInfo errorInfo) {
            Log.e(TAG, "Error creating Twilio Conversations Client: " + errorInfo.getMessage());
            if (conversationsManagerListener!=null) {
                conversationsManagerListener.showError(errorInfo.getMessage());
            }
        }
    };

    public void UpdateStatus() {
        if (conversation != null) {
            conversation.getSynchronizationStatus();
        }

//        if (conversation!=null){
//            conversation.typing();
//        }
    }

    private final ConversationListener mDefaultConversationListener = new ConversationListener() {
        @Override
        public void onMessageAdded(final Message message) {
//            loadAllChannel(conversationsClient);
            /*Log.d(TAG, "Message added");
            messages.clear();
            messages.add(message);
            if (message.getAuthor().equals("muairspa"+userid)){
                conversation.setAllMessagesRead(new CallbackListener<Long>() {
                    @Override
                    public void onSuccess(Long result) {

                    }
                });
            }
            if (conversationsManagerListener != null) {
                conversationsManagerListener.receivedNewMessage();
            }*/
        }

        @Override
        public void onMessageUpdated(Message message, Message.UpdateReason updateReason) {
            Log.d(TAG, "Message updated: " + message.getMessageBody());
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

            Log.d(TAG, "Participant updated: " + participant.getIdentity() + " " + updateReason.toString());
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

    public ArrayList<Message> getnewMessages() {
        return updatelistmessages;
    }

    public void setListener(QuickstartConversationsManagerListener listener) {
        this.conversationsManagerListener = listener;
    }


    public Conversation CheckLastMassageRead() {


        return conversation;
    }


    public void deletedata() {
        conversation.removeParticipantByIdentity("13", new StatusListener() {
            @Override
            public void onSuccess() {
                Log.d("delete ", "user");
            }
        });
    }
      public  void deleteConversation(String channelName, String userId){
                  conversationsClient.getConversation(channelName, new CallbackListener<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
//              conversation.removeParticipantByIdentity(userId, new StatusListener() {
//                  @Override
//                  public void onSuccess() {
//                     Log.d("TESTING","Delete Chat here");
//                  }
//              });


              conversation.destroy(new StatusListener() {
                  @Override
                  public void onSuccess() {
                      Log.d("TESTING","Delete Chat here");
                  }
              });

            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                Log.e(TAG, "Error retrieving conversation: " + errorInfo.getMessage());
            }
        });
      }

    /**
     * Fetch user online status and return the result via a callback
     */
    public void subscribeToUserStatus(ConversationsClient conversationsClient, String identity, UserStatusCallback callback) {
        conversationsClient.getAndSubscribeUser(identity, new CallbackListener<User>() {
            @Override
            public void onSuccess(User result) {
                if (result != null) {
                    boolean isOnline = result.isOnline();
                    Log.d(TAG, result.getIdentity() + " is now " + (isOnline ? "Online" : "Offline"));
                    callback.onUserStatusReceived(isOnline); // Return the result
                }
            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                Log.e(TAG, "Error fetching user status: " + (errorInfo != null ? errorInfo.getMessage() : "Unknown error"));
                callback.onUserStatusReceived(false); // Default to false if there's an error
            }
        });
    }

    /**
     * Callback interface for user status
     */
    public interface UserStatusCallback {
        void onUserStatusReceived(boolean isOnline);
    }


    public void getUnreadConversations(ConversationsClient conversationsClient,
                                       UnreadConversationsCallback callback) {
        List<Conversation> unreadConversations = new ArrayList<>();

        if (conversationsClient.getMyConversations() != null) {
            List<Conversation> conversations = conversationsClient.getMyConversations();
            AtomicInteger processed = new AtomicInteger(0);

            for (Conversation conversation : conversations) {
                conversation.getUnreadMessagesCount(new CallbackListener<Long>() {
                    @Override
                    public void onSuccess(Long result) {
                        if (result != null && result > 0) {
                            unreadConversations.add(conversation); // Add if unread messages exist
                        }
                        if (processed.incrementAndGet() == conversations.size()) {
                            callback.onResult(unreadConversations); // Return final list
                        }
                    }

                    @Override
                    public void onError(ErrorInfo errorInfo) {
                        Log.e("Twilio", "Error checking unread messages: " + errorInfo.getMessage());
                        if (processed.incrementAndGet() == conversations.size()) {
                            callback.onResult(unreadConversations);
                        }
                    }
                });
            }
        } else {
            callback.onResult(new ArrayList<>()); // Return empty list if no conversations exist
        }
    }

    public interface UnreadConversationsCallback {
        void onResult(List<Conversation> unreadConversations);
    }

}

