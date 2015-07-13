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
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flag) {
        writeToParcel(out);
        out.writeString(file.toString());
    }

    @Override
    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);

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

    public int getWidth() {
        return file.get(IMAGE_INFO_WIDTH).getAsInt();
    }

    public int getHeight() {
        return file.get(IMAGE_INFO_HEIGHT).getAsInt();
    }

    public void setWidth(int width) {
        file.addProperty(IMAGE_INFO_WIDTH, width);
    }

    public void setHeight(int height) {
        file.addProperty(IMAGE_INFO_HEIGHT, height);
    }

    @Override
    public String toString() {
        return String.format("TSBImageMessage[file: %s, type: %s, TSBMessageBody: %s]", file.toString(), type.getName(), super.toString());
    }

    private TSBImageMessageBody(Parcel in) {
        super(TSBMessage.TYPE.IMAGE);
        readFromParcel(in);
    }
}
