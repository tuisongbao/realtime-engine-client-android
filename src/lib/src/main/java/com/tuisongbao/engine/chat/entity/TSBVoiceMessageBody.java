package com.tuisongbao.engine.chat.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class TSBVoiceMessageBody extends TSBMediaMessageBody {
    public static final String VOICE_INFO_DURATION = "duration";

    public TSBVoiceMessageBody() {
        super(TSBMessage.TYPE.VOICE);
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

    public static final Parcelable.Creator<TSBVoiceMessageBody> CREATOR = new Parcelable.Creator<TSBVoiceMessageBody>() {
        @Override
        public TSBVoiceMessageBody createFromParcel(Parcel in) {
            return new TSBVoiceMessageBody(in);
        }

        @Override
        public TSBVoiceMessageBody[] newArray(int size) {
            return new TSBVoiceMessageBody[size];
        }
    };

    public String getDuration() {
        try {
            return file.get(VOICE_INFO_DURATION).getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    public void setDuration(String duration) {
        file.addProperty(VOICE_INFO_DURATION, duration);
    }

    @Override
    public String toString() {
        return String.format(
                "TSBVoiceMessageBody[file: %s, type: %s, TSBMessageBody: %s]",
                file.toString(), type.getName(), super.toString());
    }

    private TSBVoiceMessageBody(Parcel in) {
        super(TSBMessage.TYPE.VOICE);
        readFromParcel(in);
    }
}
