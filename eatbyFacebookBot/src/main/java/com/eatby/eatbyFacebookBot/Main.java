package com.eatby.eatbyFacebookBot;

import com.google.gson.Gson;
import com.eatby.eatbyFacebookBot.models.common.Action;
import com.eatby.eatbyFacebookBot.models.send.Button;
import com.eatby.eatbyFacebookBot.models.send.Element;
import com.eatby.eatbyFacebookBot.models.send.Message;
import com.eatby.eatbyFacebookBot.models.webhook.Messaging;
import com.eatby.eatbyFacebookBot.models.webhook.ReceivedMessage;
import okhttp3.*;

import java.util.List;
import java.util.Random;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.SparkBase.port;

public class Main {
    public static  String sAccessToken;
    private static String sValidationToken;
    public static final String END_POINT;
    public static final MediaType JSON;
    private static final Random sRandom;
    private static final Gson GSON;

    static {
        JSON = MediaType.parse("application/json; charset=utf-8");
        END_POINT = "https://graph.facebook.com/v2.6/me/messages";
        GSON = new Gson();
        sRandom = new Random();
        //sAccessToken = System.getenv("ACCESS_TOKEN");
        //sValidationToken = System.getenv("VALIDATION_TOKEN");
        
        //BotJava Page
        sAccessToken = "EAAZAtvtWmvKABAMImsBWZBs7QGdFss4CdtJ3NIahIfO86KRDIa16Tiz1ZAn58Idq0sKZBq9ZBQKUYzcPGycweLz7zabVnEgJbtZCzlFJKYNGBKIPqmtZC4ZC55vGK8nUt9LfGGAszJVsZCaouUbeNNs719CyXYvWIye823RQBSBh30AZDZD";
        //sValidationToken = System.getenv("VALIDATION_TOKEN");
        
        //JavaBot3
        sValidationToken = "VT";
    }

    public static void main(String[] args) {

        //port(Integer.valueOf(System.getenv("PORT")));
    	port(2000);
        get("/webhook", (request, response) -> {
            if (request.queryMap("hub.verify_token").value().equals(sValidationToken)) {
                return request.queryMap("hub.challenge").value();
            }
            return "Error, wrong validation token";
        });

        post("/webhook", (request, response) -> {
            ReceivedMessage receivedMessage = GSON.fromJson(request.body(), ReceivedMessage.class);
            List<Messaging> messagings = receivedMessage.entry.get(0).messaging;
            for (Messaging messaging : messagings) {
                String senderId = messaging.sender.id;
                if (messaging.message !=null) {
                    // Receiving text message
                    switch (sRandom.nextInt(4)){
                        case 0:
                            if (messaging.message.text != null)
                                Message.Text(messaging.message.text).sendTo(senderId);
                            else
                                sendSampleGenericMessage(senderId);
                            break;
                        case 1:
                            Message.Image("https://unsplash.it/764/400?image=200").sendTo(senderId);
                            break;
                        case 2:
                            sendSampleGenericMessage(senderId);
                            break;
                        default:
                            sendSamplePostBackMessage(senderId);
                            break;
                    }

                } else if (messaging.postback != null) {
                    // Receiving postback message
                    if (messaging.postback.payload == Action.ACTION_A) {
                        Message.Text("Action A").sendTo(senderId);
                    }else {
                        Message.Text("Action B").sendTo(senderId);
                    }
                } else if (messaging.delivery != null) {
                    // when the message is delivered, this webhook will be triggered.
                } else {
                    // sticker may not be supported for now.
                    System.out.println(request.body());
                }
            }
            return "";
        });
    }

    static private void sendSamplePostBackMessage(String senderId) throws Exception {
        Message message = Message.Button("This is a postback message; please choose the action below");
        message.addButton(Button.Postback("action A", Action.ACTION_A));
        message.addButton(Button.Postback("action B", Action.ACTION_B));
        message.addButton(Button.Url("open Google", "https://google.com"));
        message.sendTo(senderId);
    }

    static private void sendSampleGenericMessage(String senderId) throws Exception {
        Message message = Message.Generic();
        Element element = new Element("Generic Message Sample", "https://unsplash.it/764/400?image=400", "subtitle");
        message.addElement(element);
        element = new Element("Yay Yay", "https://unsplash.it/764/400?image=500", "subtitle");
        element.addButton(Button.Postback("action A", Action.ACTION_A));
        element.addButton(Button.Url("jump to FB", "https://facebook.com/"));
        message.addElement(element);
        message.sendTo(senderId);
    }
}
