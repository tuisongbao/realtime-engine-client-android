package com.tuisongbao.engine.demo.user.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.tuisongbao.engine.demo.Constants;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.common.utils.Utils;
import com.tuisongbao.engine.demo.common.view.activity.BaseActivity;
import com.tuisongbao.engine.demo.user.adapter.UserAdapter;
import com.tuisongbao.engine.demo.user.entity.DemoUser;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;
import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_search_user)
public class SearchUserActivity extends BaseActivity{

    public static final String USERNAMES = "usernames";
    public static final String EXCLUDEUSERS = "excludeUsers";
    public static final String  ALLOWMULTISELECT= "ALLOWMULTISELECT";

    @ViewById
    ListView lvCheckedList;

    @ViewById
    ListView lvSearchList;

    @ViewById
    TextView txtRight;

    @ViewById
    ImageView imgBack;

    @ViewById(R.id.txt_title)
    TextView txt_title;

    private UserAdapter checkedUserAdapter;
    private UserAdapter searchUserAdapter;

    private List<DemoUser> searchUsers;

    private List<DemoUser> checkedUsers;

    List<String> excludeUsers;

    @Extra(ALLOWMULTISELECT)
    boolean allowMultiSelect;

    @AfterViews
    void afterViews(){
        excludeUsers = getIntent().getStringArrayListExtra(EXCLUDEUSERS);
        LogUtils.i(excludeUsers);

        txtRight.setText("确认");
        txt_title.setText("请选择聊天的对象");
        imgBack.setVisibility(View.VISIBLE);

        searchUsers = new ArrayList<>();
        searchUserAdapter = new UserAdapter(this, searchUsers);
        lvSearchList.setAdapter(searchUserAdapter);

        checkedUsers = new ArrayList<>();
        checkedUserAdapter = new UserAdapter(this, checkedUsers);
        lvCheckedList.setAdapter(checkedUserAdapter);
    }

    private void refresh(){
        checkedUserAdapter.refresh(checkedUsers);
        searchUserAdapter.refresh(searchUsers);
    }

    @ItemClick(R.id.lvCheckedList)
    void cancelCheckedUser(int position) {
        DemoUser demoUser = checkedUsers.get(position);

        if(allowMultiSelect){
            searchUsers.add(demoUser);
        }

        checkedUsers.remove(position);
        refresh();
    }

    @ItemClick(R.id.lvSearchList)
    void checkedUser(int position) {
        DemoUser demoUser = searchUsers.get(position);

        if(!allowMultiSelect){
            checkedUsers = new ArrayList<>();
        } else{
            searchUsers.remove(position);
        }

        checkedUsers.add(demoUser);
        refresh();
    }

    @TextChange(R.id.search_user)
    void onTextChangesOnSearchUserTextView(CharSequence text) {
        if ("".equals(text.toString().trim())) {
            searchUsers = new ArrayList<>();
            searchUserAdapter.refresh(searchUsers);
        } else {
            searchUser(text.toString());
        }
    }

    void searchUser(String username) {
        RequestParams params = new RequestParams();
        params.put("username", username);
        netClient.get(Constants.DEMOUSERURL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Gson gson = new Gson();
                final List<DemoUser> demoUserList = gson.fromJson(new String(responseBody), new TypeToken<List<DemoUser>>() {
                }.getType());

                LogUtils.d(demoUserList);

                if (demoUserList != null) {
                    searchUsers = new ArrayList<>();

                    for (DemoUser demoUser : demoUserList){
                        if(excludeUsers != null && excludeUsers.contains(demoUser.getUsername())){
                            continue;
                        }
                        searchUsers.add(demoUser);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            searchUserAdapter.refresh(searchUsers);
                        }
                    });
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Utils.showShortToast(SearchUserActivity.this, "查找用户失败");
            }
        });
    }

    @Click(R.id.imgBack)
    void back() {
        setResult(Activity.RESULT_CANCELED);
        Utils.finish(this);
    }

    @Click(R.id.txtRight)
    void confirm(){
        if(checkedUsers.size() < 1){
            Utils.showShortToast(this, "没有选中用户");
            return;
        }

        Intent it = new Intent();
        LogUtils.i(checkedUsers);

        ArrayList<String> names = new ArrayList<>();
        for (DemoUser demoUser : checkedUsers){
            names.add(demoUser.getUsername());
        }

        it.putStringArrayListExtra(USERNAMES, names);
        setResult(Activity.RESULT_OK, it);
        finish();
    }
}
