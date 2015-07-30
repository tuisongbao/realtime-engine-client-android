package com.tuisongbao.engine.service;

import android.os.Parcel;
import android.os.Parcelable;

import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.StrUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class RawMessage implements Parcelable {
    private static final String TAG = RawMessage.class.getSimpleName();
    /**
     * 消息唯一标志符,用于发送的标示消息类型
     */
    private String mUUID;
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
    private int mCode = Protocol.ENGINE_CODE_SUCCESS;
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
     * 服务端请求id, 服务端返回
     */
    private long mServerRequestId;

    public RawMessage(String name, String data) {
        this();
        mName = name;
        mData = data;
    }

    public long getServerRequestId() {
        return mServerRequestId;
    }

    public void setServerRequestId(long serverRequestId) {
        this.mServerRequestId = serverRequestId;
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

    public void setErrorMessage(String errorMessage) {
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

    public String serialize() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Protocol.REQUEST_KEY_ID, getRequestId());
        json.put(Protocol.REQUEST_KEY_NAME, getName());
        json.put(Protocol.REQUEST_KEY_DATA, getData());
        return json.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(StrUtil.strNotNull(mUUID));
        dest.writeString(StrUtil.strNotNull(mName));
        dest.writeString(StrUtil.strNotNull(mData));
        dest.writeString(StrUtil.strNotNull(mChannel));
        dest.writeString(StrUtil.strNotNull(mErrorMessge));
        dest.writeString(StrUtil.strNotNull(mBindName));
        dest.writeInt(mCode);
        dest.writeBooleanArray(new boolean[] {mIsUnbind});
        dest.writeLong(mServerRequestId);
    }

    public void readFromParcel(Parcel in) {
        mUUID = in.readString();
        mName = in.readString();
        mData = in.readString();
        mChannel = in.readString();
        mErrorMessge = in.readString();
        mBindName = in.readString();
        mCode = in.readInt();

        boolean[] bools = new boolean[1];
        in.readBooleanArray(bools);
        mIsUnbind = bools[0];
        mServerRequestId = in.readLong();
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
    }

    private RawMessage() {
        mUUID = StrUtil.creatUUID();
    }
}
