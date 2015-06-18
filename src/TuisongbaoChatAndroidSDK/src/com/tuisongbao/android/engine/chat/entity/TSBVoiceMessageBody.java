package com.tuisongbao.android.engine.chat.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class TSBVoiceMessageBody extends TSBMediaMessageBody {
    public static final String VOICE_INFO = "voice";
    public static final String VOICE_INFO_DURATION = "duration";
    public static final String VOICE_INFO_FORMAT = "format";

    public TSBVoiceMessageBody() {
        super(TSBMessage.TYPE.VOICE);
        file = new JsonObject();
    }

    public void readFromParcel(Parcel in) {
        Gson gson = new Gson();
        setFile(gson.fromJson(in.readString(), JsonObject.class));
    }

    @Override
    public void writeToParcel(Parcel out, int arg1) {
        out.writeString(file.toString());
    }

    public static final Parcelable.Creator<TSBVoiceMessageBody> CREATOR =
            new Parcelable.Creator<TSBVoiceMessageBody>() {
        @Override
        public TSBVoiceMessageBody createFromParcel(Parcel in) {
            return new TSBVoiceMessageBody(in);
        }

        @Override
        public TSBVoiceMessageBody[] newArray(int size) {
            return new TSBVoiceMessageBody[size];
        }
    };

    @Override
    public String toString() {
        return String.format("TSBVoiceMessageBody[file: %s, type: %s]", file.toString(), type.getName());
    }

    private TSBVoiceMessageBody(Parcel in) {
        super(TSBMessage.TYPE.VOICE);
        readFromParcel(in);
    }

    @Override
    public JsonObject getMediaInfo() {
        return file.get(VOICE_INFO).getAsJsonObject();
    }

    @Override
    public void setMediaInfo(JsonObject infoObject) {
        file.add(VOICE_INFO, infoObject);
    }
}
