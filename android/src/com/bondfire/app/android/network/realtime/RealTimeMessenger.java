package com.bondfire.app.android.network.realtime;

import android.util.Base64;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.gson.Gson;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by alvaregd on 25/02/16.
 * Handles all of the different messages to send out.
 */
public class RealTimeMessenger {

    private static final String TAG = RealTimeMessenger.class.getName();
    private final static boolean d_sendHostGreetingMessage = true;
    private final static boolean d_sendHostChangeMessage =true;
    private final static boolean d_sendChatMessage = true;
    private final static boolean d_sendGamePrompt = true;
    private final static boolean d_broadcastClientStatus = true;
    private final static boolean d_sendGameReadyState = true;
    private final static boolean d_sendLeftGameMessage = true;
    private final static boolean d_sendGameMessage = false;

    private static Gson gson;
    private static BondfireMessage message;
    private static GoogleApiClient client;
    private static String jsonMessage;
    private static byte[] UDPbuf;
    private static byte[] TCPbuf;

    public final static String Encoding = "UTF-8";

    /**
     * Must call this function before being able to send anything
     */
    public static void prepare(Gson inGson, GoogleApiClient inClient )
    {
        client = inClient;
        gson = inGson;
        message = new BondfireMessage();
    }

    /**
     * Call this function when you are finished with it.
     */
    public static void destroy(){
        gson =null;
        message = null;
    }

    /**
     * called when current host changed the Host of the room
     * to let others know of new host
     * @param list the participant Id of the peers in the room
     * @param roomId the room id to which the peer is connect to
     * @param newHostId the participantId of the new Host
     * @param clientId the client participant Id
     */
    public static void sendHostChangeMessage(List<BondfireParticipant> list,String roomId, String newHostId, String clientId) {
        if(d_sendHostChangeMessage) Log.i(TAG, "sendHostChangeMessage() ");
        
        message.type = BondfireMessage.TYPE_HOST_UPDATE;
        message.status_type = -1;   
        message.data = newHostId;
        convertToEncodedBytes(message, true);
        broadCastToEveryOneExceptMe(list, roomId, clientId);
    }

    /**
     * When a new peer joins the room, the host must send a message to the peer
     * to let them know that they are the host.
     * @param list the participant Id of the newly connected peers
     * @param roomId the room id to which the peer is connect to
     * @param newHostId the participantId of the new Host
     */
    public static void sendHostGreetingMessage(List<String> list,String roomId, String newHostId) {
        if(d_sendHostGreetingMessage) Log.i(TAG, "sendHostGreetingMessage() ");

        message.type = BondfireMessage.TYPE_HOST_UPDATE;
        message.status_type = -1;
        message.data = newHostId;
        convertToEncodedBytes(message, true);

        for (String participantId : list){

            if (TCPbuf.length < Multiplayer.MAX_RELIABLE_MESSAGE_LEN) {
                Games.RealTimeMultiplayer.sendReliableMessage(
                        client, null, TCPbuf, roomId, participantId);
            }
        }
    }

    /**
     * call when the client sends a chat message to the room
     * @param participants the list of receiving client roomParticipants
     * @param roomId the id of room to which peers are connected to
     * @param data the message
     * @param clientId the id of the client sending the message
     */
    public static void sendChatMessage(
            List<BondfireParticipant> participants, String roomId, String data, String clientId) {
        if(d_sendChatMessage) Log.i(TAG, "sendChatMessage() ");
        
        message.type = BondfireMessage.TYPE_CHAT_UPDATE;
        message.data = data;
        message.status_type = -1;
        convertToEncodedBytes(message,true);
        broadCastToEveryOneExceptMe(participants, roomId, clientId);
    }

    /**
     * call when the host client needs to tell everyone that they are about to start
     * playing a round of a certain game
     * @param participant the target roomParticipants in the room
     * @param roomId the id of the room to which peers are connected to
     * @param data the ID of the game indicated by the host
     * @param clientId the id of the client
     */
    public static void sendGameInvite(
            Participant participant, String roomId, String data, String clientId) {
        if(d_sendGamePrompt) Log.i(TAG, "sendGameInvite() ");

        message.type = BondfireMessage.TYPE_GAME_INVITE;
        message.status_type = -1;
        message.data = data;
        convertToEncodedBytes(message,true);

        if (participant.getParticipantId().equals(clientId))return;
        if (participant.getStatus() != Participant.STATUS_JOINED)return;
        if (TCPbuf.length < Multiplayer.MAX_RELIABLE_MESSAGE_LEN) {
            Games.RealTimeMultiplayer.sendReliableMessage(
                    client, null, TCPbuf, roomId, participant.getParticipantId());
        }
    }

    /**
     * Called when a client needs to tell the other clients what they are doing
     * @param participants the list of roomParticipants
     * @param roomId the id of the room to which peers are connected to
     * @param status one of STATUS_READY, STATUS_LOBBY, STATUS_BUSY, STATUS_GAME
     * @param clientId the client participant Id
     */
    public static void broadcastClientStatus(
            List<BondfireParticipant> participants, String roomId, int status, String gameId, String clientId) {
        if(d_broadcastClientStatus) Log.i(TAG, "broadcastClientStatus() ");

        if (!isValidStatus(status)) {
            Log.e(TAG, "broadcastClientStatus: Illegal Status: " + status);
           throw new IllegalArgumentException();
        }

        message.type = BondfireMessage.TYPE_STATUS_UPDATE;
        message.data = gameId;
        message.status_type = status;
        convertToEncodedBytes(message,true);
        broadCastToEveryOneExceptMe(participants, roomId, clientId);
    }

    /**
     * Called when a client needs to tell the other clients what they won a round
     * @param participants the list of roomParticipants
     * @param roomId the id of the room to which peers are connected to
     * @param clientId the client participant Id
     */
    public static void broadcastRoundWin(
            List<BondfireParticipant> participants, String roomId, String clientId) {

        message.type = BondfireMessage.TYPE_ROUND_WIN;
        message.data = "";
        message.status_type = -1;
        convertToEncodedBytes(message,true);
        broadCastToEveryOneExceptMe(participants, roomId, clientId);
    }



    /**
     * called by client whenever it wishes to let someone else know that they are ready to start
     * receiving game data
     * @param participant recipient participant
     * @param roomId the id of the room that the client is connected to
     * @param gameId the id of the game the client is currently running
     * @param clientId the id of the client
     */
    public static void sendGameReadyState(Participant participant, String roomId,String gameId, String readyTime, String clientId) {
        if(d_sendGameReadyState) Log.i(TAG, "sendGameReadyState() ");

        message.type = BondfireMessage.TYPE_GAME_READY;
        message.status_type = -1;
        message.data = gameId;
        message.data2 = readyTime;
        convertToEncodedBytes(message,true );

        if (participant.getParticipantId().equals(clientId))return;
        if (participant.getStatus() != Participant.STATUS_JOINED)return;
        if (TCPbuf.length < Multiplayer.MAX_RELIABLE_MESSAGE_LEN) {
            Games.RealTimeMultiplayer.sendReliableMessage(
                    client, null, TCPbuf, roomId, participant.getParticipantId());
        }
    }

    /**
     * This is a sync message. We send it out every few seconds to let a target client know
     * we are still connected with them.
     * @param participant the recipient participant
     * @param roomId the id of the room
     */
    public static void sendGameSyncMessage(Participant participant, String roomId) {

        message.type = BondfireMessage.TYPE_GAME_SYNC;
        message.status_type = -1;
        message.data = null;
        message.data2 = null;
        convertToEncodedBytes(message, true);
        if (TCPbuf.length < Multiplayer.MAX_RELIABLE_MESSAGE_LEN) {
            Games.RealTimeMultiplayer.sendReliableMessage(
                    client, null, TCPbuf, roomId, participant.getParticipantId());
        }
    }


    /**
     * called by client whenever it wishes to let people in the game mesh that they are no longer
     * available to receiving game data
     * @param participant recipient participant
     * @param roomId the id of the room that the client is connected to
     * @param gameId the id of the game the client is currently running
     * @param clientId the id of the client
     */
    public static void sendLeftGameMessage(Participant participant, String roomId,String gameId, String data2,String clientId) {
        if(d_sendLeftGameMessage) Log.i(TAG, "sendLeftGameMessage() ");

        message.type = BondfireMessage.TYPE_GAME_LEAVE;
        message.status_type = -1;
        message.data = gameId;
        message.data2 = data2;
        convertToEncodedBytes(message,true);

        if (participant.getParticipantId().equals(clientId))return;
        if (participant.getStatus() != Participant.STATUS_JOINED)return;
        if (TCPbuf.length < Multiplayer.MAX_RELIABLE_MESSAGE_LEN) {
            Games.RealTimeMultiplayer.sendReliableMessage(
                    client, null, TCPbuf, roomId, participant.getParticipantId());
        }
    }

    /**
     * called by client whenever it wishes to let people in the game mesh that they are no longer
     * available to receiving game data
     * @param participant recipient participant
     * @param roomId the id of the room that the client is connected to
     * @param gameData the id of the game the client is currently running
     * @param clientId the id of the client
     */
    public static void sendGameMessage(
            Participant participant, String roomId,String gameData, String clientId, boolean isReliable) {
        if(d_sendGameMessage) Log.i(TAG, "sendGameMessage() " + gameData);

        message.type = BondfireMessage.TYPE_GAME_DATA;
        message.status_type = -1;
        message.data = gameData;
        convertToEncodedBytes(message, isReliable);

        if (participant.getParticipantId().equals(clientId))return;
        if (participant.getStatus() != Participant.STATUS_JOINED)return;

        try {
            if (isReliable) {

                if (TCPbuf.length < Multiplayer.MAX_RELIABLE_MESSAGE_LEN) {
                    Games.RealTimeMultiplayer.sendReliableMessage(
                            client, null, TCPbuf, roomId, participant.getParticipantId());
                }

            }else {
                if (UDPbuf.length < Multiplayer.MAX_UNRELIABLE_MESSAGE_LEN) {
                    Games.RealTimeMultiplayer.sendUnreliableMessage(
                            client, UDPbuf, roomId, participant.getParticipantId());
                }
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "sendGameMessage: Tried to send message", e);
        }
    }



    /**
     * Call when a message needs to be transmitted to everyone except the client
     * @param participants
     * @param roomId
     * @param clientId
     */
    private static void broadCastToEveryOneExceptMe(
            List<BondfireParticipant> participants,String roomId, String clientId){


        for (BondfireParticipant p : participants) {
            if (p.getParticipant().getParticipantId().equals(clientId))continue;
            if (p.getParticipant().getStatus() != Participant.STATUS_JOINED)continue;
            if (TCPbuf.length < Multiplayer.MAX_RELIABLE_MESSAGE_LEN) {
                Games.RealTimeMultiplayer.sendReliableMessage(
                        client, null, TCPbuf, roomId, p.getParticipant().getParticipantId());
            }
        }
    }

    /**
     * Converts a bondfire message into a format that the network can handle
     * @param message the object to convert
     */
    private static void convertToEncodedBytes(BondfireMessage message, boolean isReliable) {
        try {
            jsonMessage = gson.toJson(message);
            if (isReliable) {
                TCPbuf = Base64.encode(jsonMessage.getBytes(Encoding), Base64.DEFAULT);
            }else{
                UDPbuf = Base64.encode(jsonMessage.getBytes(Encoding), Base64.DEFAULT);
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "sendHostGreetingMessage: Something was null!", e);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "sendHostGreetingMessage: Unsupported encoding", e);
        }
    }

    private static boolean isValidType (int type) {
        return  (type == BondfireMessage.TYPE_CHAT_UPDATE ||
                type == BondfireMessage.TYPE_GAME_INVITE ||
                type == BondfireMessage.TYPE_HOST_UPDATE ||
                type == BondfireMessage.TYPE_STATUS_UPDATE);
    }


    private static boolean isValidStatus (int status) {

        return  (status == BondfireMessage.STATUS_BUSY ||
                status == BondfireMessage.STATUS_LOBBY ||
                status == BondfireMessage.STATUS_GAME);
    }
}
