package com.tuisongbao.android.engine.chat.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class TSBVideoMessageBody extends TSBMediaMessageBody {
    public static final String VIDEO_INFO_DURATION = "duration";

    public TSBVideoMessageBody() {
        super(TSBMessage.TYPE.VIDEO);
    }

    @Override
    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);

        Gson gson = new Gson();
        setFile(gson.fromJson(in.readString(), JsonObject.class));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int arg1) {
        writeToParcel(out);
        out.writeString(file.toString());
    }

    public static final Parcelable.Creator<TSBVideoMessageBody> CREATOR =
            new Parcelable.Creator<TSBVideoMessageBody>() {
        @Override
        public TSBVideoMessageBody createFromParcel(Parcel in) {
            return new TSBVideoMessageBody(in);
        }

        @Override
        public TSBVideoMessageBody[] newArray(int size) {
            return new TSBVideoMessageBody[size];
        }
    };

    public String getDuration() {
        try {
            return file.get(VIDEO_INFO_DURATION).getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    public void setDuration(String duration) {
        file.addProperty(VIDEO_INFO_DURATION, duration);
    }

    @Override
    public String toString() {
        return String.format("TSBVideoMessageBody[file: %s, type: %s, TSBMessageBody: %s]", file.toString(), type.getName(), super.toString());
    }

    private TSBVideoMessageBody(Parcel in) {
        super(TSBMessage.TYPE.VIDEO);
        readFromParcel(in);
    }
}
