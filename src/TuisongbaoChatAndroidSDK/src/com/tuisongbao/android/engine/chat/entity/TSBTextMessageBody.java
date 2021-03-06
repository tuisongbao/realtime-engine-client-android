package com.tuisongbao.android.engine.chat.entity;

import android.os.Parcel;
import android.os.Parcelable;


public class TSBTextMessageBody extends TSBMessageBody {

    public TSBTextMessageBody() {
        super(TSBMessage.TYPE.TEXT);
    }

    public TSBTextMessageBody(String text) {
        super(TSBMessage.TYPE.TEXT);
        setText(text);
    }

    public void setText(String text) {
        super.setText(text);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getText());
    }

    public void readFromParcel(Parcel in) {
        setText(in.readString());
    }

    public static final Parcelable.Creator<TSBTextMessageBody> CREATOR =
            new Parcelable.Creator<TSBTextMessageBody>() {
        public TSBTextMessageBody createFromParcel(Parcel in) {
            return new TSBTextMessageBody(in);
        }

        public TSBTextMessageBody[] newArray(int size) {
            return new TSBTextMessageBody[size];
        }
    };

    private TSBTextMessageBody(Parcel in) {
        super(TSBMessage.TYPE.TEXT);
        readFromParcel(in);
    }

}
