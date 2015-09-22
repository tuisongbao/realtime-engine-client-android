package com.tuisongbao.engine.demo.conversation.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;
import com.github.nkzawa.emitter.Emitter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.chat.conversation.ChatConversation;
import com.tuisongbao.engine.chat.group.ChatGroup;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.Constants;
import com.tuisongbao.engine.demo.GlobeParams;
import com.tuisongbao.engine.demo.MainActivity;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.common.utils.NetClient;
import com.tuisongbao.engine.demo.common.utils.Utils;
import com.tuisongbao.engine.demo.conversation.adapter.ConversationAdapter;
import com.tuisongbao.engine.demo.conversation.view.activity.ChatConversationActivity;
import com.tuisongbao.engine.demo.conversation.view.activity.ChatConversationActivity_;
import com.tuisongbao.engine.demo.group.entity.DemoGroup;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;
import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 15-9-1.
 */
@EFragment(R.layout.fragment_conversations)
public class ConversationsFragment extends Fragment {
    @ViewById(R.id.rl_error_item)
    public RelativeLayout errorItem;

    @ViewById(R.id.tv_connect_errormsg)
    public TextView errorText;

    @ViewById(R.id.listview)
    ListView lvContact;

    @ViewById(R.id.txt_nochat)
    TextView noChat;

    Emitter.Listener mListener;
    private NetClient netClient;

    private ConversationAdapter adapter;
    private List<ChatConversation> conversationList = new ArrayList<ChatConversation>();
    private MainActivity parentActivity;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentActivity = (MainActivity) getActivity();
        mListener = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                parentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshWithLocal();
                    }
                });
            }
        };
        LogUtils.d("onCreateView");
        netClient = new NetClient(parentActivity);
        refreshDemoGroups();
        return null;
    }

    public void refreshDemoGroups() {
        LogUtils.d("begin");
        App.getInstance().getGroupManager().getList(null, new EngineCallback<List<ChatGroup>>() {
            @Override
            public void onSuccess(List<ChatGroup> chatGroups) {
                List<String> ids = new ArrayList<>();
                App.getInstance().setToken(App.getInstance().getToken());

                for (ChatGroup group : chatGroups) {
                    ids.add(group.getGroupId());
                }

                if (!ids.isEmpty()) {
                    final RequestParams requestParams = new RequestParams();
                    requestParams.put("groupIds[]", ids);
                    parentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            netClient.post(Constants.DEMOGROUPURL, requestParams, new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    Gson gson = new Gson();
                                    List<DemoGroup> groupList = gson.fromJson(new String(responseBody), new TypeToken<List<DemoGroup>>() {
                                    }.getType());

                                    if (groupList != null) {
                                        GlobeParams.ListGroupInfo = groupList;
                                        for (DemoGroup group : groupList) {
                                            GlobeParams.GroupInfo.put(group.getGroupId(), group);
                                        }
                                    }
                                    refreshWithLocal();
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                    LogUtils.e(new String(responseBody));
                                    parentActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Utils.showShortToast(parentActivity, "获取群组信息失败");
                                        }
                                    });
                                }
                            }, true);
                        }
                    });
                }
            }

            @Override
            public void onError(ResponseError error) {
                LogUtils.e(error.getMessage());
            }
        });
    }

    @AfterViews
    void afterView() {
        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        App.getInstance().getChatManager().bind(ChatManager.EVENT_MESSAGE_NEW, mListener);
        refreshWithLocal();
    }

    public void onPause() {
        super.onPause();
        App.getInstance().getChatManager().unbind(ChatManager.EVENT_MESSAGE_NEW, mListener);
    }

    private void initViews() {
        if (conversationList == null) {
            conversationList = new ArrayList<>();
        }

        if (adapter == null) {
            adapter = new ConversationAdapter(parentActivity, conversationList);
        }

        adapter.setConversationList(conversationList);

        if (lvContact.getAdapter() == null) {
            lvContact.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }

        if (conversationList == null || conversationList.isEmpty()) {
            refresh();
        } else {
            refreshWithLocal();
        }
    }

    /**
     * 刷新页面
     */
    public void refresh() {
        App.getInstance().getConversationManager().getList(null, null, new EngineCallback<List<ChatConversation>>() {
            public void onSuccess(final List<ChatConversation> t) {
                Activity activity = getActivity();
                if (activity == null) {
                    return;
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (t == null || t.isEmpty()) {
                            noChat.setVisibility(View.VISIBLE);
                        } else {
                            noChat.setVisibility(View.GONE);
                        }
                        conversationList = t;
                        adapter.refresh(conversationList);

                    }
                });
            }

            @Override
            public void onError(ResponseError error) {
                Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                LogUtils.e(error);
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(parentActivity, "获取会话失败，请稍后再试", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public void refreshWithLocal() {
        List<ChatConversation> localList = App.getInstance().getConversationManager().getLocalList();
        if (localList == null || localList.isEmpty()) {
            noChat.setVisibility(View.VISIBLE);
            return;
        } else {
            noChat.setVisibility(View.GONE);
        }
        conversationList = localList;
        adapter.refresh(conversationList);
    }

    @ItemClick(R.id.listview)
    public void gotoConversation(int position) {
        if (conversationList == null || conversationList.isEmpty()) {
            return;
        }
        ChatConversation mClickedChatConversation = conversationList.get(position);
        mClickedChatConversation.resetUnread(null);
        Intent intent = new Intent(this.getActivity(),
                ChatConversationActivity_.class);
        intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TARGET, mClickedChatConversation.getTarget());
        intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TYPE, mClickedChatConversation.getType());

        startActivity(intent);
    }
}
