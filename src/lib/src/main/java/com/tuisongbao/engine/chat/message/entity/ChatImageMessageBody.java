package com.tuisongbao.engine.chat.message.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ChatImageMessageBody extends ChatMediaMessageBody {
    public static final String IMAGE_INFO_WIDTH = "width";
    public static final String IMAGE_INFO_HEIGHT = "height";

    public ChatImageMessageBody() {
        super(ChatMessage.TYPE.IMAGE);
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

    public static final Parcelable.Creator<ChatImageMessageBody> CREATOR =
            new Parcelable.Creator<ChatImageMessageBody>() {
        @Override
        public ChatImageMessageBody createFromParcel(Parcel in) {
            return new ChatImageMessageBody(in);
        }

        @Override
        public ChatImageMessageBody[] newArray(int size) {
            return new ChatImageMessageBody[size];
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
        return String.format("ChatImageMessageBody[file: %s, type: %s, ChatMessageBody: %s]", file.toString(), type.getName(), super.toString());
    }

    private ChatImageMessageBody(Parcel in) {
        super(ChatMessage.TYPE.IMAGE);
        readFromParcel(in);
    }
}
