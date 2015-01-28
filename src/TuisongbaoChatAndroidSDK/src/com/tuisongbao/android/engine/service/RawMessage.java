package com.tuisongbao.android.engine.service;

import android.os.Parcel;
import android.os.Parcelable;

import com.tuisongbao.android.engine.engineio.EngineConstants;
import com.tuisongbao.android.engine.util.StrUtil;

public class RawMessage implements Parcelable {

    /**
     * 消息唯一标志符,用于发送的标示消息类型
     */
    private String mUUID;
    /**
     * 应用id
     */
    private String mAppId;
    /**
     * 应用Secret
     */
    private String mAppKey;
    /**
     * 消息名
     */
    private String mName;
    /**
     * 消息数据
     */
    private String mData;
    /**
     * 渠道号
     */
    private String mChannel;
    /**
     * 返回的状态码
     */
    private int mCode = EngineConstants.CONNECTION_CODE_SUCCESS;
    /**
     * 返回的状态错误信息
     */
    private String mErrorMessge;
    /**
     * 绑定事件名称
     */
    private String mBindName;
    /**
     * 绑定事件名称
     */
    private boolean mIsUnbind = false;
    /**
     * 请求id, 发送请求时使用
     */
    private long mRequestId;
    /**
     * 签名用的string
     */
    private String mSignStr;
    private long mTimestamp;
    
    public RawMessage(String appId, String appKey, String name, String data) {
        this();
        mAppKey = appKey;
        mAppId = appId;
        mName = name;
        mData = data;
    }

    public String getSignStr() {
        return mSignStr;
    }

    public void setSignStr(String signStr) {
        this.mSignStr = signStr;
    }

    public String getAppKey() {
        return mAppKey;
    }

    public void setAppKey(String appKey) {
        this.mAppKey = appKey;
    }

    public boolean isUnbind() {
        return mIsUnbind;
    }

    public void setIsUnbind(boolean isUnbind) {
        this.mIsUnbind = isUnbind;
    }

    public String getBindName() {
        return mBindName;
    }

    public void setBindName(String bindName) {
        this.mBindName = bindName;
    }

    public int getCode() {
        return mCode;
    }

    public void setCode(int code) {
        this.mCode = code;
    }

    public String getErrorMessge() {
        return mErrorMessge;
    }

    public void setErrorMessge(String errorMessage) {
        this.mErrorMessge = errorMessage;
    }

    public String getChannel() {
        return mChannel;
    }

    public void setChannel(String channel) {
        this.mChannel = channel;
    }

    public long getRequestId() {
        return mRequestId;
    }

    public void setRequestId(long requestId) {
        this.mRequestId = requestId;
    }

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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(StrUtil.strNotNull(mUUID));
        dest.writeString(StrUtil.strNotNull(mAppId));
        dest.writeString(StrUtil.strNotNull(mAppKey));
        dest.writeString(StrUtil.strNotNull(mName));
        dest.writeString(StrUtil.strNotNull(mData));
        dest.writeString(StrUtil.strNotNull(mChannel));
        dest.writeString(StrUtil.strNotNull(mErrorMessge));
        dest.writeString(StrUtil.strNotNull(mBindName));
        dest.writeString(StrUtil.strNotNull(mSignStr));
        dest.writeInt(mCode);
        dest.writeBooleanArray(new boolean[] {mIsUnbind});
    }

    public void readFromParcel(Parcel in) {
        mUUID = in.readString();
        mAppId = in.readString();
        mAppKey = in.readString();
        mName = in.readString();
        mData = in.readString();
        mChannel = in.readString();
        mErrorMessge = in.readString();
        mBindName = in.readString();
        mSignStr = in.readString();
        mCode = in.readInt();

        boolean[] bools = new boolean[1];
        in.readBooleanArray(bools);
        mIsUnbind = bools[0];
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
