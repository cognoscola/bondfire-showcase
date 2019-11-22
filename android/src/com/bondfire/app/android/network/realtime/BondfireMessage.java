package com.bondfire.app.android.network.realtime;

/**
 * Created by alvaregd on 25/02/16.
 * Describes the structure of messages passed between bondfire clients
 */
public class BondfireMessage {

    //Types of Message calls
    public final static int TYPE_HOST_UPDATE = 0; //sets the sender id as the host
    public final static int TYPE_CHAT_UPDATE = 1; //updates the chat
    public final static int TYPE_GAME_INVITE = 2; //prompts everyone to join host in a game
    public final static int TYPE_STATUS_UPDATE = 3; //updates everyone on client status
    public final static int TYPE_GAME_READY = 4; //to let everyone know that they are ready to receive game data
    public final static int TYPE_GAME_LEAVE = 5;
    public final static int TYPE_GAME_DATA = 6; //contains game information
    public final static int TYPE_GAME_SYNC = 7; //sync message
    public final static int TYPE_ROUND_WIN = 8;

    //Types of STATUS Updates
    public final static int STATUS_LOBBY = 0; //player is not playing a game, but is in the lobby
    public final static int STATUS_BUSY  = 2; //player is outside the app
    public final static int STATUS_GAME  = 1; //player is a play a game different from the host

    public int type;
    public int status_type;
    public String data;
    public String data2;


}
