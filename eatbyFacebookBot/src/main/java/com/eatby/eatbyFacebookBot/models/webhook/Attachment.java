package com.eatby.eatbyFacebookBot.models.webhook;

import com.google.gson.annotations.SerializedName;

public class Attachment {
    public enum Type {
        @SerializedName("audio")
        AUDIO,
        @SerializedName("image")
        IMAGE,
        @SerializedName("video")
        VIDEO
    }

    public Type type;
    public Payload payload;
}
