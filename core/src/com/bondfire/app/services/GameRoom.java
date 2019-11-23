package com.bondfire.app.services;

import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

/**
 * Created by alvaregd on 23/02/16.
 * Is the room that games inside bondfire look at to decide its game logic with participants
 */
public class GameRoom {

    private boolean isConnected;       //Lets the game know that the  client is connected to a room
    private String gameHostId;         //Id of the game Host
    private String clientId;           //the ID of this client
    private boolean isConnectionReady; //lets the client know that the game is ready to receive data
    private Array<GameParticipant> participants; //the participants in the current game


    /** get/set*/
    public String getClientId() {
        return clientId;
    }
    public String getGameHostId() {
        return gameHostId;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    public void setGameHostId(String gameHostId) {
        this.gameHostId = gameHostId;
    }
    public boolean isConnectionReady() {
        return isConnectionReady;
    }
    public void setIsConnectionReady(boolean isConnectionReady) {
        this.isConnectionReady = isConnectionReady;
    }
    public boolean isConnected() {return isConnected;}
    public void setConnected(boolean connected) {isConnected = connected;}
    public Array<GameParticipant> getParticipants() {
        return participants;
    }

    public GameRoom() {
        super();
        participants = new Array<GameParticipant>();
    }

    public void removeParticipant(String participantId) {

        Iterator<GameParticipant> it = participants.iterator();
        while (it.hasNext()) {
            GameParticipant participant = it.next();
            if (participant.getParticipantId().equals(participantId)) {
                it.remove();
                break;
            }
        }
    }

    public boolean isHost() {
        if (clientId != null && gameHostId != null) {
            return clientId.equals(gameHostId) && !clientId.isEmpty() && !gameHostId.isEmpty();
        }else {
            return false;
        }
    }
}
