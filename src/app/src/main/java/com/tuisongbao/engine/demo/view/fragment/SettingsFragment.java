package com.tuisongbao.engine.demo.view.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.Constants;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.common.Utils;
import com.tuisongbao.engine.demo.net.NetClient;
import com.tuisongbao.engine.demo.view.activity.ChangePasswordActivity_;
import com.tuisongbao.engine.demo.view.activity.LoginActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.apache.http.Header;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by user on 15-9-1.
 */
@EFragment(R.layout.fragment_setting)
public class SettingsFragment extends Fragment {
    public static final int REQUEST_CODE_LOCAL = 0;
    @ViewById(R.id.head)
    ImageView head;

    @ViewById(R.id.tvname)
    TextView tvname;

    @AfterViews
    void afterViews() {
        if(App.getInstance2().getChatUser() == null){
            return;
        }
        String username = App.getInstance2().getChatUser().getUserId();
        tvname.setText(username);
        NetClient.getIconBitmap(head, Constants.USERAVATARURL + username);
    }

    @Click(R.id.view_user)
    void updateHead() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");

        } else {
            intent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, REQUEST_CODE_LOCAL);
    }

    @OnActivityResult(REQUEST_CODE_LOCAL)
    void onResultLocalImage(int resultCode, Intent data) {
        Log.d("update", data.getData().toString());
        if (data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                uploadPic(selectedImage);
            }
        }
    }

    void uploadPic(Uri uri) {
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null,
                null, null);
        String picturePath;
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex("_data");
            picturePath = cursor.getString(columnIndex);
            cursor.close();
        } else {
            File file = new File(uri.getPath());
            if (!file.exists()) {
                return;

            }
            picturePath = file.getAbsolutePath();
        }

        if (picturePath == null || picturePath.equals("null")) {
            return;
        }

        //获取上传文件的路径
        final String path = picturePath;

        Log.d("update", "-00000000" + path);
        //异步的客户端对象

        //指定url路径
        final String url = Constants.uploadChatUserAvatar;

        //根据路径创建文件
        final File file = new File(path);

        if (!file.exists()) {
            Log.e("file not found", "sdffdf");
        }

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        try {
            params.put("avatar", file);
            params.put("token", App.getInstance2().getToken());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        LogUtils.i("params", params + "");
        client.post(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, final Header[] headers, byte[] responseBody) {
                Bitmap bmp = BitmapFactory.decodeFile(path);
                String url = Constants.USERAVATARURL + App.getInstance2().getChatUser().getUserId();
                LogUtils.i("url------------", url);
                NetClient.updateImage(url, bmp, head);
                Toast.makeText(getActivity(), "成功", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] responseBody, Throwable error) {
                Toast.makeText(getActivity(), "失败", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Click(R.id.btn_logout)
    void logout() {
        App.getInstance2().getChatManager().logout(new EngineCallback<String>() {
            @Override
            public void onSuccess(String s) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "登出成功", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(ResponseError error) {

            }
        });

        Utils.RemoveValue(getActivity(), Constants.LoginState);
        Utils.RemoveValue(getActivity(), Constants.UserInfo);
        Intent intent = new Intent(getActivity(), LoginActivity_.class);
        startActivity(intent);
        getActivity().finish();
    }

    @Click(R.id.btn_logout_clean)
    void logoutAndClean() {
        App.getInstance2().getChatManager().clearCache();
        logout();

    }

    @Click(R.id.txt_setting)
    void gotoChangePassword(){
        Utils.start_Activity(getActivity(), ChangePasswordActivity_.class);
    }
}
