package com.tuisongbao.engine.demo.chat.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.tuisongbao.engine.chat.conversation.entity.ChatConversation;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.user.ChatType;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.demo.DemoApplication;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.chat.ChatConversationActivity;
import com.tuisongbao.engine.demo.chat.adapter.ChatConversationsAdapter;
import com.tuisongbao.engine.demo.chat.entity.ConversationWrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ChatConversationsFragment extends Fragment {

    private static ChatConversationsFragment mConversationsFragment;
    private static final String TAG = ChatConversationsFragment.class.getSimpleName();

    private View mRootView;
    private ListView mConversationsListView;
    private List<ConversationWrapper> mConversationList;
    private ChatConversationsAdapter mConversationsAdapter;
    private HashMap<String, ConversationWrapper> mConversationHashMap = new HashMap<String, ConversationWrapper>();
    private ConversationWrapper mClickedConversationWrapper;

    public static ChatConversationsFragment getInstance() {
        if (null == mConversationsFragment) {
            mConversationsFragment = new ChatConversationsFragment();
        }
        return mConversationsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // inflater: 打气筒， inflate：充气，very vivid.
        mRootView = inflater.inflate(R.layout.fragment_conversations, container,
                false);
        mConversationsListView = (ListView) mRootView
                .findViewById(R.id.fragment_conversations_listview);

        mConversationList = new ArrayList<ConversationWrapper>();

        mConversationsAdapter = new ChatConversationsAdapter(mConversationList, getActivity());
        mConversationsListView.setAdapter(mConversationsAdapter);
        mConversationsListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                mClickedConversationWrapper = mConversationList.get(arg2);
                mClickedConversationWrapper.localUnreadCount = 0;
                ChatConversation conversation = mClickedConversationWrapper.getConversation();
                resetUnread(conversation);

                mConversationsAdapter.refresh(mConversationList);

                Intent intent = new Intent(getActivity(),
                        ChatConversationActivity.class);
                intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION, conversation);
                startActivity(intent);
            }
        });

        mConversationsListView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                    final int arg2, long arg3) {
                new AlertDialog.Builder(getActivity())
                    .setTitle("确定删除该会话吗？")
                    .setPositiveButton("确定", new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                int which) {
                            deleteConversation(mConversationList.get(arg2).getConversation());
                        }
                    })
                    .setNegativeButton("取消", new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                int which) {
                            // empty

                        }
                    }).show();
                return true;
            }
        });
        request();

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mClickedConversationWrapper = null;
    }

    public void onMessageSent(ChatMessage message) {
        updateLatestMessageOfConversation(message, message.getRecipient());
        refreshConversationList();
    }

    public void onMessageReceived(ChatMessage message) {
        String target = "";
        if (message.getChatType() == ChatType.SingleChat) {
            target = message.getFrom();
        } else {
            target = message.getRecipient();
        }

        ConversationWrapper wrapper = updateLatestMessageOfConversation(message, target);
        if (wrapper != mClickedConversationWrapper) {
            wrapper.localUnreadCount++;
        }
        refreshConversationList();
    }

    private ConversationWrapper updateLatestMessageOfConversation(ChatMessage message, String target) {
        String key = message.getChatType().getName() + target;
        ConversationWrapper wrapper = mConversationHashMap.get(key);
        // No local conversation, create a new one.
        if (wrapper == null) {
            wrapper = new ConversationWrapper();
            mConversationHashMap.put(key, wrapper);

            ChatConversation conversation = new ChatConversation(DemoApplication.engine.chatManager.conversationManager);
            conversation.setType(message.getChatType());
            conversation.setTarget(target);
            wrapper.setConversation(conversation);
        }
        wrapper.setLatestMessage(message);

        return wrapper;
    }

    private void request() {
        DemoApplication.engine.chatManager.conversationManager.getList(null, null, new TSBEngineCallback<List<ChatConversation>>() {

            @Override
            public void onSuccess(final List<ChatConversation> t) {
                Log.d(TAG, "Get " + t.size() + " conversations");

                refreshConversationHashMap(t);
                Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        refreshConversationList();
                    }
                });
            }

            @Override
            public void onError(int code, String message) {
                Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "获取会话失败，请稍后再试", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void deleteConversation(ChatConversation conversation) {
        conversation.delete(new TSBEngineCallback<String>() {

            @Override
            public void onSuccess(String t) {
                Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "删除会话成功", Toast.LENGTH_LONG).show();
                        request();
                    }
                });
            }

            @Override
            public void onError(int code, String message) {
                Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "删除会话失败，请稍后再试", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void resetUnread(ChatConversation conversation) {
        conversation.setUnreadMessageCount(0);
        conversation.resetUnread(new TSBEngineCallback<String>() {
            @Override
            public void onSuccess(String t) {
                Toast.makeText(getActivity(), "重置未读消息成功", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(int code, String message) {
                Toast.makeText(getActivity(), "重置未读消息失败，请稍后再试", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void refreshConversationHashMap(List<ChatConversation> conversations) {
        HashMap<String, ConversationWrapper> newConversations = new HashMap<String, ConversationWrapper>();
        for (ChatConversation conversation : conversations) {
            String keyString = getKeyString(conversation);
            ConversationWrapper wrapper = mConversationHashMap.get(keyString);
            if (wrapper == null) {
                wrapper = new ConversationWrapper();
            }
            wrapper.setConversation(conversation);
            wrapper.loadLatestMessage(null);
            newConversations.put(getKeyString(conversation), wrapper);
        }
        mConversationHashMap = newConversations;
    }

    private String getKeyString(ChatConversation conversation) {
        return conversation.getType().getName() + conversation.getTarget();
    }

    private void refreshConversationList() {
        Collection<ConversationWrapper> collection = mConversationHashMap.values();
        mConversationList = new ArrayList<ConversationWrapper>();
        mConversationList.addAll(collection);
        mConversationsAdapter.refresh(mConversationList);
    }
}
