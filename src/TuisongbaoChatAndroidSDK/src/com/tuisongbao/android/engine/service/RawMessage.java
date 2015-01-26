package com.tuisongbao.android.engine.service;

import com.tuisongbao.android.engine.util.StrUtil;

import android.os.Parcel;
import android.os.Parcelable;

public class RawMessage implements Parcelable {

    /**
     * 消息唯一标志符,用于发送的标示消息类型
     */
    public String mUUID;
    /**
     * 应用id
     */
    public String mAppId;
    /**
     * 消息名
     */
    public String mName;
    /**
     * 消息数据
     */
    public String mData;
    public long mTimestamp;

    public String getUUID() {
        return mUUID;
    }

    public String getAppId() {
        return mAppId;
    }

    public void setAppId(String appId) {
        this.mAppId = appId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getData() {
        return mData;
    }

    public void setData(String data) {
        this.mData = data;
    }

    @Override
    public int describeContents() {
        return 0;
    }
    
    public RawMessage(String appId, String name, String data) {
        this();
        mAppId = appId;
        mName = name;
        mData = data;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUUID);
        dest.writeString(mAppId);
        dest.writeString(mName);
        dest.writeString(mData);
    }

    public void readFromParcel(Parcel in) {
        mUUID = in.readString();
        mAppId = in.readString();
        mName = in.readString();
        mData = in.readString();
    }

    public static final Parcelable.Creator<RawMessage> CREATOR =
            new Parcelable.Creator<RawMessage>() {
        public RawMessage createFromParcel(Parcel in) {
            return new RawMessage(in);
        }

        public RawMessage[] newArray(int size) {
            return new RawMessage[size];
        }
    };

    private RawMessage(Parcel in) {
        readFromParcel(in);
        timestamp();
    }

    private RawMessage() {
        mUUID = StrUtil.creatUUID();
        timestamp();
    }

    private void timestamp() {
        mTimestamp = System.currentTimeMillis();
    }
}
