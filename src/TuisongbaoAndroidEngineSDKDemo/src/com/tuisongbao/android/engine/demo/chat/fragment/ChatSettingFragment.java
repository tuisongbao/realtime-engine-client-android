package com.tuisongbao.android.engine.demo.chat.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.tuisongbao.android.engine.chat.TSBChatManager;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.demo.R;
import com.tuisongbao.android.engine.demo.chat.LoginActivity;

public class ChatSettingFragment extends Fragment {

    private static ChatSettingFragment mChatSettingFragment;
    private Button mLogoutButton;
    private View mRootView;

    public static ChatSettingFragment getInstance() {
        if (null == mChatSettingFragment) {
            mChatSettingFragment = new ChatSettingFragment();
        }
        return mChatSettingFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_chat_setting, container,
                false);
        mLogoutButton = (Button)mRootView.findViewById(R.id.chat_setting_logout);
        mLogoutButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                TSBChatManager.getInstance().logout(new TSBEngineCallback<String>() {
                    
                    @Override
                    public void onSuccess(String t) {
                        getActivity().runOnUiThread(new Runnable() {
                            
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "登出成功", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            }
                        });
                    }
                    
                    @Override
                    public void onError(int code, String message) {
                        getActivity().runOnUiThread(new Runnable() {
                            
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "登出失败，请稍后再试", Toast.LENGTH_LONG).show();
                            }
                        });
                        
                    }
                });
            }
        });

        return mRootView;
    }
}
