package com.example.umairali.wyapp;

/**
 * Created by Umair Ali on 11/16/2017.
 */

class Messages {
    private String message, type,messageImage;
    private long  time;
    private boolean seen;
    private String from;

    public Messages() {
    }

    public Messages(String message, String type, String imageUrl, long time, boolean seen, String from) {
        this.message = message;
        this.type = type;
        this.messageImage = imageUrl;
        this.time = time;
        this.seen = seen;
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getMessageImage() {
        return messageImage;
    }

    public void setMessageImage(String messageImage) {
        this.messageImage = messageImage;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
