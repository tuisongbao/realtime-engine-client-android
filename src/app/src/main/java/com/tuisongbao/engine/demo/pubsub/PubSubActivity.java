package com.tuisongbao.engine.demo.pubsub;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.tuisongbao.engine.channel.Channel;
import com.tuisongbao.engine.channel.PresenceChannel;
import com.tuisongbao.engine.channel.entity.User;
import com.tuisongbao.engine.demo.DemoApplication;
import com.tuisongbao.engine.demo.R;

import java.util.ArrayList;
import java.util.List;

public class PubSubActivity extends Activity {
    private static String TAG = "TSB" + "com.tuisongbao.android.engine.demo:PubSubActivity";

    private Button mSubscribeChannelButton;
    private Button mUnsubscribeChannelButton;
    private EditText mSubscribeChannelEditText;
    private EditText mSubscribeChannelAuthDataEditText;
    private EditText mUnsubscribeChannelEditText;
    private ListView mEventsListView;
    private List<String> mEventsList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pubsub);

        TextView noteView = (TextView) findViewById(R.id.pubsub_textview_note);
        noteView.setText("执行下面这个命令触发event：\n"
                + "curl http://testapi.tuisongbao.com:80/v2/open/engine/events \n"
                + "-H 'Content-Type: application/json' \n"
                + "-u 'ab3d5241778158b2864c0852:16477b81ad80b3af008f9601' \n"
                + "-d '{\"name\":\"cool-event\",\"data\":\"Hello World!\",\"channels\":[\"你输入的channel名字\"]}'");
        noteView.setText("");

        mSubscribeChannelButton = (Button) findViewById(R.id.pubsub_button_channel_subscribe);
        mSubscribeChannelEditText = (EditText) findViewById(R.id.pubsub_edittext_channel_name);
        mSubscribeChannelAuthDataEditText = (EditText) findViewById(R.id.pubsub_edittext_channel_authData);
        mSubscribeChannelButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View arg0) {
                String channelName = mSubscribeChannelEditText.getText().toString();
                String authData = mSubscribeChannelAuthDataEditText.getText().toString();
                if (channelName == null || channelName.trim().length() < 1) {
                    Toast.makeText(PubSubActivity.this, "channel name 必须包含有效字符", Toast.LENGTH_LONG).show();
                    return;
                }
                Channel channel = DemoApplication.getChannelManager().subscribe(channelName, authData);
                channel.bind(Channel.EVENT_SUBSCRIPTION_SUCCESS, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        String data = args[0].toString();
                        Log.i(TAG, "Get " + data);
                    }
                });
                channel.bind(Channel.EVENT_SUBSCRIPTION_ERROR, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        String message = args[0].toString();
                        Log.i(TAG, "Subscribe failed with error " + message);
                    }
                });

                Emitter.Listener listener = new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        String data = args[0].toString();
                        Log.i(TAG, "Get " + data);
                    }
                };
                channel.bind("cool-event", listener);
                channel.unbind("cool-event", listener);

                Channel presenceChannel = DemoApplication.getChannelManager().subscribe("presence-" + channelName, authData);
                presenceChannel.bind(PresenceChannel.EVENT_USER_ADDED, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        User user = (User)args[0];
                        Log.i(TAG, user.getId() + " add, " + user.getInfo());
                    }
                });
                presenceChannel.bind(PresenceChannel.EVENT_USER_REMOVED, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        User user = (User)args[0];
                        Log.i(TAG, user.getId() + " removed, " + user.getInfo());
                    }
                });
            }
        });

        mUnsubscribeChannelEditText = (EditText) findViewById(R.id.pubsub_edittext_channel_unsubscribe);
        mUnsubscribeChannelButton = (Button) findViewById(R.id.pubsub_button_channel_unsubscribe);
        mUnsubscribeChannelButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String channelName = mUnsubscribeChannelEditText.getText().toString();
                if (channelName == null || channelName.trim().length() < 1) {
                    Toast.makeText(PubSubActivity.this, "channel name 必须包含有效字符", Toast.LENGTH_LONG).show();
                    return;
                }
                DemoApplication.getChannelManager().unsubscribe(channelName);
            }
        });

        mEventsListView = (ListView) findViewById(R.id.pubsub_listview_events);
        mEventsListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mEventsList));
    }

    private void refreshEventList(String channelName, String eventName, String data) {
        mEventsList.add(String.format("channelName: %s;eventName: %s;data: %s", channelName,eventName, data));
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                ArrayAdapter<String> adapter  = (ArrayAdapter)mEventsListView.getAdapter();
                adapter.notifyDataSetChanged();
            }
        });
    }
}
