package com.tuisongbao.android.engine.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tuisongbao.android.engine.channel.TSBChannelManager;
import com.tuisongbao.android.engine.common.TSBEngineBindCallback;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.entity.TSBEngineConstants;
import com.tuisongbao.android.engine.util.StrUtil;

public class MainActivity extends Activity {

    private Button mSendButton;
    private TextView mContentTextView;
    private Button mBindButton;
    private TextView mBindContentTextView;
    private String mChannel;
    private boolean isBind = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContentTextView = (TextView)findViewById(R.id.content);
        mSendButton = (Button)findViewById(R.id.send);
        mChannel = TSBEngineConstants.TSBENGINE_CHANNEL_PREFIX_PRIVATE + StrUtil.creatUUID();
        mSendButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                TSBChannelManager.getInstance().subscribe(mChannel);
//                TSBChannelManager.getInstance().subscribe(mChannel, new TSBEngineCallback<String>() {
//                    
//                    @Override
//                    public void onSuccess(final String t) {
//                        runOnUiThread(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                Toast.makeText(MainActivity.this,
//                                        "Channel: " + t,
//                                        Toast.LENGTH_LONG).show();
//                                mContentTextView.setText(t);
//                            }
//                        });
//                    }
//                    
//                    @Override
//                    public void onError(final int code, final String message) {
//                        runOnUiThread(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                String content = "Error [code=" + code + ";message=" + message;
//                                Toast.makeText(MainActivity.this,content,
//                                        Toast.LENGTH_LONG).show();
//                                mContentTextView.setText(content);
//                            }
//                        });
//                    }
//                });
            }
        });
        mBindContentTextView = (TextView)findViewById(R.id.bind_content);
        mBindButton = (Button)findViewById(R.id.bind);
        mBindButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (!isBind) {
                    TSBChannelManager.getInstance().bind(mChannel, new TSBEngineBindCallback() {
                        
                        @Override
                        public void onEvent(final String eventName, final String name, final String data) {
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    String content = "Bind message [eventName=" + eventName + ";name=" + name + ";data" + data;
                                    mBindContentTextView.setText(content);
                                }
                            });
                            
                        }
                    });
                    mBindButton.setText(getString(R.string.unbind));
                } else {
                    TSBChannelManager.getInstance().unbind(mChannel);
//                    TSBChannelManager.getInstance().unbind(mChannel, new TSBEngineBindCallback() {
//                        
//                        @Override
//                        public void onEvent(final String eventName, final String name, final String data) {
//                            runOnUiThread(new Runnable() {
//
//                                @Override
//                                public void run() {
//                                    String content = "Bind message [eventName=" + eventName + ";name=" + name + ";data" + data;
//                                    mBindContentTextView.setText(content);
//                                }
//                            });
//                            
//                        }
//                    });
                    mBindButton.setText(getString(R.string.bind));
                }
                isBind = !isBind;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
