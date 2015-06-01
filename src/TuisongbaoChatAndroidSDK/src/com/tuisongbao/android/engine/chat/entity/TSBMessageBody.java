package com.tuisongbao.android.engine.chat.entity;

import android.os.Parcelable;

import com.tuisongbao.android.engine.chat.entity.TSBMessage.TYPE;

public abstract class TSBMessageBody implements Parcelable {

    public TSBMessageBody(TYPE type) {
        this.type = type;
    }

    public TSBMessageBody() {
    }

    protected TYPE type;

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public static TSBMessageBody createMessage(TYPE type) {
        TSBMessageBody body = null;
        if (type == null) {
            body = new TSBTextMessageBody();
        }
        if (type == TYPE.TEXT) {
            body = new TSBTextMessageBody();
        } else if (type == TYPE.IMAGE) {
            body = new TSBImageMessageBody();
            body.setType(TYPE.IMAGE);
        } else if (type == TYPE.VOICE) {
            body = new TSBVoiceMessageBody();
            body.setType(TYPE.VOICE);
        }
        return body;
    }
}
