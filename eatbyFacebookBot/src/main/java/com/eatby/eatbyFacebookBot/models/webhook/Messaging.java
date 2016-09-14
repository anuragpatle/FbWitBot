package com.eatby.eatbyFacebookBot.models.webhook;

import com.eatby.eatbyFacebookBot.models.common.Recipient;

public class Messaging {
    public Sender sender;
    public Recipient recipient;
    public String timeStamp;
    public Message message;
    public Postback postback;
    public Delivery delivery;
}
