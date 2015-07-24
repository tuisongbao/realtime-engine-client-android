package com.tuisongbao.engine.demo.pubsub;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tuisongbao.engine.channel.TSBChannelManager;
import com.tuisongbao.engine.channel.entity.TSBChannel;
import com.tuisongbao.engine.common.TSBEngineBindCallback;
import com.tuisongbao.engine.demo.R;

public class PubSubActivity extends Activity {
    private static String TAG = "com.tuisongbao.android.engine.demo:PubSubActivity";

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
            public void onClick(View arg0) {
                String channelName = mSubscribeChannelEditText.getText().toString();
                String authData = mSubscribeChannelAuthDataEditText.getText().toString();
                if (channelName == null || channelName.trim().length() < 1) {
                    Toast.makeText(PubSubActivity.this, "channel name 必须包含有效字符", Toast.LENGTH_LONG).show();
                    return;
                }
                TSBChannel channel = TSBChannelManager.getInstance().subscribe(channelName, authData);
                channel.bind("engine:subscription_succeeded", new TSBEngineBindCallback() {

                    @Override
                    public void onEvent(String channelName, String eventName, String data) {
                        refreshEventList(channelName, eventName, data);
                    }
                });

                channel.bind("engine:subscription_error", new TSBEngineBindCallback() {

                    @Override
                    public void onEvent(String channelName, String eventName, String data) {
                        refreshEventList(channelName, eventName, data);
                    }
                });

                channel.bind("cool-event", new TSBEngineBindCallback() {

                    @Override
                    public void onEvent(String channelName, String eventName, String data) {
                        refreshEventList(channelName, eventName, data);
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
                TSBChannelManager.getInstance().unSubscribe(channelName);
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
