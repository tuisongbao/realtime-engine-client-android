package com.tuisongbao.android.engine.chat.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class TSBImageMessageBody extends TSBMediaMessageBody {
    public static final String IMAGE_INFO_WIDTH = "width";
    public static final String IMAGE_INFO_HEIGHT = "height";

    public TSBImageMessageBody() {
        super(TSBMessage.TYPE.IMAGE);
        file = new JsonObject();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flag) {
        out.writeString(file.toString());
    }

    public void readFromParcel(Parcel in) {
        Gson gson = new Gson();
        setFile(gson.fromJson(in.readString(), JsonObject.class));
    }

    public static final Parcelable.Creator<TSBImageMessageBody> CREATOR =
            new Parcelable.Creator<TSBImageMessageBody>() {
        @Override
        public TSBImageMessageBody createFromParcel(Parcel in) {
            return new TSBImageMessageBody(in);
        }

        @Override
        public TSBImageMessageBody[] newArray(int size) {
            return new TSBImageMessageBody[size];
        }
    };

    @Override
    public int getWidth() {
        return file.get(IMAGE_INFO_WIDTH).getAsInt();
    }

    @Override
    public int getHeight() {
        return file.get(IMAGE_INFO_HEIGHT).getAsInt();
    }

    @Override
    public void setWidth(int width) {
        file.addProperty(IMAGE_INFO_WIDTH, width);
    }

    @Override
    public void setHeight(int height) {
        file.addProperty(IMAGE_INFO_HEIGHT, height);
    }

    @Override
    public String toString() {
        return String.format("TSBImageMessage[file: %s, type: %s]", file.toString(), type.getName());
    }

    private TSBImageMessageBody(Parcel in) {
        super(TSBMessage.TYPE.IMAGE);
        readFromParcel(in);
    }
}
