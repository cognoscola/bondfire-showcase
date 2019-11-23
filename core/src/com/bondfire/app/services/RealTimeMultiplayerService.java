package com.bondfire.app.services;

/**
 * Created by alvaregd on 23/02/16.
 * This service is passed to the game.
 */
public class RealTimeMultiplayerService {

    RealTimeMultiplayerMessageReceiver receiver;
    RealTimeMultiplayerMessageSender sender;

    public static RealTimeMultiplayerService newInstance(){
        return new RealTimeMultiplayerService();
    }

    private RealTimeMultiplayerService() {
        super();
    }

    public void setReceiver(RealTimeMultiplayerMessageReceiver receiver) {
        this.receiver = receiver;
    }
    public RealTimeMultiplayerMessageSender getSender() {return sender;}
    public void setSender(RealTimeMultiplayerMessageSender sender) {
        this.sender = sender;
    }

}
