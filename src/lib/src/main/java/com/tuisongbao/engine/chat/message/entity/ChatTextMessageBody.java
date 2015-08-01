package com.tuisongbao.engine.chat.message.entity;

import android.os.Parcel;
import android.os.Parcelable;


public class ChatTextMessageBody extends ChatMessageBody {

    private String text;

    public ChatTextMessageBody() {
        super(ChatMessage.TYPE.TEXT);
    }

    public ChatTextMessageBody(String text) {
        super(ChatMessage.TYPE.TEXT);
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

    public static final Parcelable.Creator<ChatTextMessageBody> CREATOR =
            new Parcelable.Creator<ChatTextMessageBody>() {
        @Override
        public ChatTextMessageBody createFromParcel(Parcel in) {
            return new ChatTextMessageBody(in);
        }

        @Override
        public ChatTextMessageBody[] newArray(int size) {
            return new ChatTextMessageBody[size];
        }
    };

    @Override
    public String toString() {
        return String.format("ChatTextMessageBody[text: %s, type: %s, ChatMessageBody: %s", text, type.getName(), super.toString());
    }

    private ChatTextMessageBody(Parcel in) {
        super(ChatMessage.TYPE.TEXT);
        readFromParcel(in);
    }
}
