package com.tuisongbao.android.engine.chat.entity;

import android.os.Parcelable;

import com.tuisongbao.android.engine.chat.entity.TSBMessage.TYPE;

public abstract class TSBMessageBody implements Parcelable {

    public TSBMessageBody(TYPE type) {
        
    }

    private TYPE type;
    private String text;

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
