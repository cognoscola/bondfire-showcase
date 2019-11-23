package com.bondfire.app.services;

/**
 * Created by alvaregd on 28/02/16.
 */
public class GameParticipant {

    public final static int STATUS_PLAY = 1; //everything is normal
    public final static int STATUS_BUSY = 2; // the player is in the game, is ready to receive data, but
                                             // the app is not in the foreground,

    private String participantName;  //the name of the user
    private String participantId;    //the participant ID
    private int playerStatus = 0;
    private int watchDogCounter = 3; //if this reaches 0, it means the player is disconnected

    public int getPlayerStatus() {
        return playerStatus;
    }
    public void setPlayerStatus(int playerStatus) {
        this.playerStatus = playerStatus;
    }
    public String getParticipantName() {
        return participantName;
    }
    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }
    public String getParticipantId() {
        return participantId;
    }
    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    public boolean isWatchDogExpired() {
        watchDogCounter--;
        return watchDogCounter <= 0 ;
    }
    public void resetWatchDogCounter(){
        watchDogCounter = 3;
    }


}
