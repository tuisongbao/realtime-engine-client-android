package com.tuisongbao.android.engine.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tuisongbao.android.engine.TSBEngine.TSBEngineListener;
import com.tuisongbao.android.engine.channel.TSBChannelManager;
import com.tuisongbao.android.engine.service.RawMessage;
import com.tuisongbao.android.engine.util.StrUtil;

public class MainActivity extends Activity {

    private Button mSendButton;
    private TextView mContentTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContentTextView = (TextView)findViewById(R.id.content);
        mSendButton = (Button)findViewById(R.id.send);
        mSendButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                TSBChannelManager.getInstance().subscribe(StrUtil.creatUUID(), new TSBEngineListener() {
                    
                    @Override
                    public void call(final RawMessage msg) {
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this,
                                                msg.getData(),
                                                Toast.LENGTH_LONG).show();
                                        mContentTextView.setText(msg.getData());
                                    }
                                });
                    }
                });
//                RawMessage message = new RawMessage("", "", "2");
//                TSBEngine.send("ping", "2", new TSBEngineListener() {
//                    
//                    @Override
//                    public void call(final RawMessage msg) {
//                        runOnUiThread(new Runnable() {
//                            
//                            @Override
//                            public void run() {
//                                Toast.makeText(MainActivity.this, msg.getData(), Toast.LENGTH_LONG).show();
//                            }
//                        });
//                        
//                    }
//                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
