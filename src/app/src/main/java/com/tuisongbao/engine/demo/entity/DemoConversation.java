package com.tuisongbao.engine.demo.entity;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by user on 15-8-19.
 */
public class DemoConversation implements Parcelable {
    private String name;
    private int type;
    private String target;
    private int unreadMessageCount;
    private String lastActiveAt;
    private String lastMessage;
    private String avatarUrl;
    private Drawable avatar;

    protected DemoConversation(Parcel in) {
        name = in.readString();
        target = in.readString();
        type = in.readInt();
        unreadMessageCount = in.readInt();
        lastActiveAt = in.readString();
        avatarUrl = in.readString();
        lastMessage = in.readString();
        ClassLoader Drawable = ClassLoader.getSystemClassLoader();
        avatar = (android.graphics.drawable.Drawable) in.readValue(Drawable);
    }

    public static final Creator<DemoConversation> CREATOR = new Creator<DemoConversation>() {
        @Override
        public DemoConversation createFromParcel(Parcel in) {
            DemoConversation conversation = new DemoConversation(in);
            return conversation;
        }

        @Override
        public DemoConversation[] newArray(int size) {
            return new DemoConversation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(target);
        dest.writeInt(unreadMessageCount);
        dest.writeString(lastActiveAt);
        dest.writeString(avatarUrl);
        dest.writeString(lastMessage);
        dest.writeInt(type);
    }
}
