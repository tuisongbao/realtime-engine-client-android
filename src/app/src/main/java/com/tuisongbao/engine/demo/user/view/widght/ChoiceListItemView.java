package com.tuisongbao.engine.demo.user.view.widght;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.common.utils.NetClient;

/**
 * Created by user on 15-9-23.
 */
public class ChoiceListItemView extends LinearLayout implements Checkable {

    private TextView nameTxt;
    private CheckBox selectBtn;
    private ImageView ivAvatar;

    public ChoiceListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.item_user, this, true);
        nameTxt = (TextView) v.findViewById(R.id.author);
        selectBtn = (CheckBox) v.findViewById(R.id.radio);
        ivAvatar = (ImageView) v.findViewById(R.id.avatar);
    }

    public void setName(String text) {
        nameTxt.setText(text);
        NetClient.showAvatar(ivAvatar, text);
    }

    @Override
    public boolean isChecked() {
        return selectBtn.isChecked();
    }

    @Override
    public void setChecked(boolean checked) {
        selectBtn.setChecked(checked);
        //根据是否选中来选择不同的背景图片
        if (checked) {
            selectBtn.setBackgroundResource(R.drawable.jy_checkbox_on);
        } else {
            selectBtn.setBackgroundResource(R.drawable.jy_checkbox_off);
        }
    }

    @Override
    public void toggle() {
        selectBtn.toggle();
    }

}