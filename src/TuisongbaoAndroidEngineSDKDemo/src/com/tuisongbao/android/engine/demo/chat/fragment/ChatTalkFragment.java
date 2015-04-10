package com.tuisongbao.android.engine.demo.chat.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.tuisongbao.android.engine.chat.TSBConversationManager;
import com.tuisongbao.android.engine.chat.entity.TSBChatConversation;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.demo.R;
import com.tuisongbao.android.engine.demo.chat.ChatGroupDetailActivity;
import com.tuisongbao.android.engine.demo.chat.adapter.ChatTalkAdapter;

public class ChatTalkFragment extends Fragment {

    private static ChatTalkFragment mChatTalkFragment;

    private View mRootView;
    private ListView mListViewTalk;
    private List<TSBChatConversation> mListConversation;
    private ChatTalkAdapter mAdapterChatTalk;

    public static ChatTalkFragment getInstance() {
        if (null == mChatTalkFragment) {
            mChatTalkFragment = new ChatTalkFragment();
        }
        return mChatTalkFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_chat_talk, container,
                false);
        mListViewTalk = (ListView) mRootView
                .findViewById(R.id.fragment_chat_talk_listview);

        mListConversation = new ArrayList<TSBChatConversation>();

        mAdapterChatTalk = new ChatTalkAdapter(mListConversation, getActivity());
        mListViewTalk.setAdapter(mAdapterChatTalk);
        mListViewTalk.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                Intent intent = new Intent(getActivity(),
                        ChatGroupDetailActivity.class);
                intent.putExtra(ChatGroupDetailActivity.EXTRA_CODE_CHAT_TYPE, mListConversation.get(arg2).getType().getName());
                intent.putExtra(ChatGroupDetailActivity.EXTRA_CODE_TARGET, mListConversation.get(arg2).getTarget());
                startActivity(intent);
                resetUnread(mListConversation.get(arg2));
            }
        });
        mListViewTalk.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                    final int arg2, long arg3) {
                new AlertDialog.Builder(getActivity())
                    .setTitle("确定删除该会话吗？")
                    .setPositiveButton("确定", new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                int which) {
                            deleteConversation(mListConversation.get(arg2));
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
        request();
    }

    private void request() {
        TSBConversationManager.getInstance().getList(null, null, new TSBEngineCallback<List<TSBChatConversation>>() {

            @Override
            public void onSuccess(final List<TSBChatConversation> t) {
                mListConversation = t;
                Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mAdapterChatTalk.refresh(mListConversation);
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

    private void deleteConversation(TSBChatConversation conversation) {
        TSBConversationManager.getInstance().delete(conversation.getType(), conversation.getTarget(), new TSBEngineCallback<String>() {

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

    private void resetUnread(TSBChatConversation conversation) {
        TSBConversationManager.getInstance().resetUnread(conversation.getType(), conversation.getTarget(), new TSBEngineCallback<String>() {
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
}
