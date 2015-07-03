package com.tuisongbao.android.engine.chat.entity;

import android.os.Parcel;
import android.os.Parcelable;


public class TSBTextMessageBody extends TSBMessageBody {

    private String text;

    public TSBTextMessageBody() {
        super(TSBMessage.TYPE.TEXT);
    }

    public TSBTextMessageBody(String text) {
        super(TSBMessage.TYPE.TEXT);
        setText(text);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        writeToParcel(out);
        out.writeString(getText());
    }

    @Override
    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        setText(in.readString());
    }

    public static final Parcelable.Creator<TSBTextMessageBody> CREATOR =
            new Parcelable.Creator<TSBTextMessageBody>() {
        @Override
        public TSBTextMessageBody createFromParcel(Parcel in) {
            return new TSBTextMessageBody(in);
        }

        @Override
        public TSBTextMessageBody[] newArray(int size) {
            return new TSBTextMessageBody[size];
        }
    };

    @Override
    public String toString() {
        return String.format("TSBTextMessageBody[text: %s, type: %s, TSBMessageBody: %s", text, type.getName(), super.toString());
    }

    private TSBTextMessageBody(Parcel in) {
        super(TSBMessage.TYPE.TEXT);
        readFromParcel(in);
    }
}
