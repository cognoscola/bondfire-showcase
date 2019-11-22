package com.bondfire.app.android.network.realtime;

import com.google.android.gms.games.multiplayer.Participant;

/**
 * Created by alvaregd on 28/02/16.
 * This class exists to carry additional participant information
 */
public class BondfireParticipant  {

    private boolean isHost;       // of the room?
    private int clientStatus;     //Lobby/Busy/In-game
    private String gameId;        //holds the game Id of this participant
    private Participant participant; //the participant information
    private int roundScore = 0;
    private boolean isReadyToReceiveGameData;   //This is used to determine who is to be Host of a game
    private boolean pendingInvitation = false;

    public boolean isPendingInvitation() {
        return pendingInvitation;
    }
    public void setPendingInvitation(boolean pendingInvitation) {
        this.pendingInvitation = pendingInvitation;
    }

    public long getReadyTime() {
        return readyTime;
    }

    public void setReadyTime(long readyTime) {
        this.readyTime = readyTime;
    }

    //if we see that someone is gameReady and their gameID matches ours,
                                  //it means they are first to arrive at the game and so they are host
    private long readyTime; //In case 2 or more players become game ready while the Ready message in transit
                            //we will attach a  time, and see who became ready first
                            //make the first person to become ready the host

    public int getRoundScore() {
        return roundScore;
    }
    public void incrementScore(){
        this.roundScore++;
    }

    public boolean isReadyToReceiveGameData() {
        return isReadyToReceiveGameData;
    }
    public void setIsReadyToReceiveGameData(boolean isReadyToReceiveGameData) {this.isReadyToReceiveGameData = isReadyToReceiveGameData;}
    public Participant getParticipant() {
        return participant;
    }
    public void setParticipant(Participant participant) {
        this.participant = participant;
    }
    public int getClientStatus() {
        return clientStatus;
    }
    public void setClientStatus(int clientStatus) {
        this.clientStatus = clientStatus;
    }
    public boolean isHost() {
        return isHost;
    }
    public void setIsHost(boolean isHost) {
        this.isHost = isHost;
    }
    public String getGameId() {
        return gameId;
    }
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }



}
