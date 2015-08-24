package com.tuisongbao.engine.demo.fragment;

import android.support.v4.app.Fragment;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tuisongbao.engine.demo.Constants;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.app.App;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by user on 15-8-14.
 */
@EFragment(R.layout.fragment_settings)
public class SettingsFragment extends Fragment {

    @ViewById
    ImageView logo;

    @ViewById
    TextView userName;

    @AfterViews
    void calledAfterViewInjection() {
        String target = App.getContext().getChatManager().getChatUser().getUserId();
        ImageLoader.getInstance().displayImage(Constants.USERAVATARURL + target, logo);
        userName.setText(target);

    }

}
