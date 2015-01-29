package com.tuisongbao.android.engine.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.channel.TSBChannelManager;
import com.tuisongbao.android.engine.chat.TSBChatManager;
import com.tuisongbao.android.engine.chat.entity.TSBChatUser;
import com.tuisongbao.android.engine.common.TSBEngineBindCallback;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.connection.entity.TSBConnection;
import com.tuisongbao.android.engine.engineio.sink.TSBListenerSink;
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
        TSBEngine.connection.bind(TSBEngineConstants.TSBENGINE_BIND_NAME_CONNECTION_CONNECTED, new TSBEngineCallback<TSBConnection>() {
            
            @Override
            public void onSuccess(TSBConnection t) {
                runOnUiThread(new Runnable() {
                    
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,
                                "Connected",
                                Toast.LENGTH_LONG).show();
                        mContentTextView.setText("Connected");
                    }
                });
            }
            
            @Override
            public void onError(int code, String message) {
                runOnUiThread(new Runnable() {
                    
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,
                                "disConnected",
                                Toast.LENGTH_LONG).show();
                        mContentTextView.setText("disConnected");
                    }
                });
            }
        });
        mSendButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
//                mChannel = StrUtil.creatUUID();
//                TSBChannelManager.getInstance().subscribe(mChannel, data);
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
                    TSBChannelManager.getInstance().bind(mChannel, mTSBEngineBindCallback);
                    mBindButton.setText(getString(R.string.unbind));
                } else {
                    TSBChannelManager.getInstance().unbind(mChannel, mTSBEngineBindCallback);
                    mBindContentTextView.setText("");
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
        mBindButton.postDelayed(new Runnable() {
            
            @Override
            public void run() {
                TSBChatManager.getInstance().login(null, new TSBEngineCallback<TSBChatUser>() {
                    
                    @Override
                    public void onSuccess(TSBChatUser t) {
                        runOnUiThread(new Runnable() {
                            
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,
                                        "login success",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    
                    @Override
                    public void onError(int code, String message) {
                        runOnUiThread(new Runnable() {
                            
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,
                                        "login failed",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }
        }, 5 * 1000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    private TSBEngineBindCallback mTSBEngineBindCallback = new TSBEngineBindCallback() {
        
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
    };

}
