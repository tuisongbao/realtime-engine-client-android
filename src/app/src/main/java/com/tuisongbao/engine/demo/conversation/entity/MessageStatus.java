package com.tuisongbao.engine.demo.conversation.entity;

/**
 * Created by user on 15-9-2.
 */
public enum MessageStatus {
    SUCCESS(0, "SUCCESS"),
    FAIL(1, "FAIL"),
    INPROGRESS(2, "INPROGRESS");


    private final String name;
    private int value;

    MessageStatus(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static MessageStatus getMessageStatus(int value) {
        MessageStatus[] t = MessageStatus.values();
        for (MessageStatus s : t) {
            if (s.value == value) {
                return s;
            }
        }
        return INPROGRESS;
    }

    public int getValue() {
        return value;
    }
}
