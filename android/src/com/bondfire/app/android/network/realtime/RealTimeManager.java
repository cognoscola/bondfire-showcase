package com.bondfire.app.android.network.realtime;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;

import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentTransaction;

import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.bondfire.app.R;
import com.bondfire.app.android.activity.GamePlayServiceActivity;
import com.bondfire.app.android.activity.MainActivity;
import com.bondfire.app.android.adapter.RealTimeChatAdapter;
import com.bondfire.app.android.adapter.RealTimeParticipantAdapter;
import com.bondfire.app.android.fragment.GameGridViewFragment;
import com.bondfire.app.android.fragment.InviteDialogFragment;
import com.bondfire.app.android.services.NetworkService;
import com.bondfire.app.android.view.TabViewBase;
import com.bondfire.app.services.GameParticipant;
import com.bondfire.app.services.RealTimeMultiplayerMessageReceiver;
import com.bondfire.app.services.RealTimeMultiplayerMessageSender;
import com.bondfire.app.services.GameRoom;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by alvaregd on 15/02/16.
 * Manage all network logic that has to do with Real-time communication EXCEPT
 * logic that is needed to handshake with GooglePlay services.
 */
public class RealTimeManager implements
        RealTimeMessageReceivedListener,
        RoomStatusUpdateListener,
        RoomUpdateListener,
        RealTimeMultiplayerMessageSender {

    /***
     * DEBUG
     **/
    private final static String TAG = RealTimeManager.class.getName();
    private final static boolean d_game_invite = true;
    private final static boolean d_run = false;
    private final static boolean d_onDestroy = true;
    private static final boolean d_acceptInviteRoom = false;
    private static final boolean d_onP2PDisconnected = false;
    private static final boolean d_onRoomConnecting = false;
    private static final boolean d_onRoomAutoMatching = false;
    private static final boolean d_onPeerInvitedToRoom = false;
    private static final boolean d_onPeerDeclined = false;
    private static final boolean d_onPeerJoined = true;
    private static final boolean d_onPeerLeft = true;
    private static final boolean d_onConnectedToRoom = true;
    private static final boolean d_onDisconnectedFromRoom = true;
    private static final boolean d_onPeersConnected = true;
    private static final boolean d_onPeersDisconnected = true;
    private static final boolean d_onP2PConnected = true;
    private static final boolean d_onRoomCreated = true;
    private static final boolean d_onJoinedRoom = true;
    private static final boolean d_onLeftRoom = true;
    private static final boolean d_onRoomConnected = true;
    private static final boolean d_handleInvitePlayersToRoom = false;
    private static final boolean d_leaveRoom = false;
    private final static boolean d_updateRoom = true;
    private final static boolean d_bindReceiver = false;
    private final static boolean d_onRealTimeMessageReceived = false;
    private final static boolean d_onJoinPressed = false;
    private final static boolean d_getStatusAndBroadcast = false;
    private final static boolean d_broadcastClientStatus = false;
    private final static boolean d_updateGameRoomState = true;
    private final static boolean d_setGameConnectionReady = true;
    private final static boolean d_DestroyGameConnection = false;
    private final static boolean d_checkAndResolveGameHostConflict = true;

    private final static boolean d_GAME_READY = true;
    private final static boolean d_GAME_LEAVE = true;

    /**
     * Request Codes
     */
    public final static int RC_SELECT_PLAYERS = 10000; //result code;
    public final static int RC_INVITATION_INBOX = 10001; //result code;

    /*this variable is monitorerd by the real time room fragment to decide which view state it
    * should be in (CHAT OR PARTICIPANT)
    */
    private boolean isBoundToService = true;

    private static String HOST = "HOST";
    private static String GUEST = "GUEST";

    private int viewState = 0;

    //client used to interact with Google APIS
    private GoogleApiClient googleApiClient;

    //the ID of the current coom
    private static Room inRoom; //the lastest received room object

    /*this is the object that is given to the game, its hold players that are going to participate
    * in the client'sgame */
    private static GameRoom gameRoom;

    //The Participants in the currectly active game
    private static HashMap<String, BondfireParticipant> roomParticipants = null;

    private static String clientId = "";


    //when receiving a new message we place the sender's name here
    private static String senderParticipantId;

    private Context context;

    private RealTimeParticipantAdapter participantAdapter;
    private RealTimeChatAdapter chatAdapter;
    private RealTimeMultiplayerMessageReceiver gameReceiver;

    private static BondfireMessage incomingMessage;
    private static Gson gson;
    private static byte[] inBuffer; //a place to store the incoming message as bytes;
    private static String inStringBuffer; //message as a string;

    //this is called by a game which uses multiplayer services to know if it should
    //skip to its Multiplayer menu when the game is started
    private boolean skipToMultiplayer = false;

    /* A reference to the service so that we can let it know to kill itself when we leave the room
    *  while outside of the app
    */
    private NetworkService service;

    Random random;

    //intent action
    public final static String ACTION_JOIN = "JOIN_GAME";
    public final static String ACTION_DECLINE = "DECLINE_GAME";
    public final static String EXTRA_GAMEID = "EXTRA_GAME_ID";

    private static final long vibratePatternGameInvite[] = {0, 300, 100, 100, 100, 300};
    private static final long vibratePatternRoomConnect[] = {0, 100, 100, 300};

    public boolean canRecievePopup = true; //prevents the user from being spammed with dialogInvites
    //A user can receive one Dialog every 5 seconds

    /**
     * Analytics stuff
     **/
    private static int roomCount; //holds the number of participants in a joined room


    public static RealTimeManager newInstance(Context context) {
        return new RealTimeManager(context);
    }

    public RealTimeManager(Context context) {
        this.context = context;
        gson = new Gson();
        roomParticipants = new HashMap<>();
        random = new Random();
        watchDogHandler = new Handler();
    }

    public void setNewContext(Context context) {
        this.context = context;
    }


    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
        RealTimeMessenger.prepare(gson, googleApiClient);
    }

    /**
     * get/set
     **/
    public RealTimeChatAdapter getChatAdapter() {
        return chatAdapter;
    }

    public RealTimeParticipantAdapter getParticipantAdapter() {
        return participantAdapter;
    }

    public void setParticipantAdapter(RealTimeParticipantAdapter participantAdapter) {
        this.participantAdapter = participantAdapter;
    }

    public void setChatAdapter(RealTimeChatAdapter adapter) {
        this.chatAdapter = adapter;
    }

    public void setIsBoundToService(boolean isBoundToService) {
        this.isBoundToService = isBoundToService;
    }

    public void setService(NetworkService service) {
        this.service = service;
    }

    public int getViewState() {
        return viewState;
    }

    public void saveViewState(int viewState) {
        this.viewState = viewState;
    }

    /**
     * Message Received Listener
     * Handle all incoming messages from bondfire clients
     **/
    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {

        senderParticipantId = realTimeMessage.getSenderParticipantId();

        //check if the message come from someone we know
        BondfireParticipant participant = roomParticipants.get(senderParticipantId);
        if (participant == null) {
            Log.e(TAG, "onRealTimeMessageReceived: Unknown Sender!");
            return;
        }

        // extract the message
        try {
            inBuffer = Base64.decode(realTimeMessage.getMessageData(), Base64.DEFAULT);
            inStringBuffer = new String(inBuffer, RealTimeMessenger.Encoding);
            incomingMessage = gson.fromJson(inStringBuffer, BondfireMessage.class);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "onRealTimeMessageReceived: Error While trying to decode", e);
        }

        //see what type of message it is
        switch (incomingMessage.type) {
            case BondfireMessage.TYPE_HOST_UPDATE:
                if (d_onRealTimeMessageReceived)
                    Log.i(TAG, "onRealTimeMessageReceived() HOST UPDATE");

                //received a new host update from a game host , first, check if this person is in the same game as us
                if (isParticipantInSameGameAsMe(senderParticipantId)) {
                    //cool is this client the host?
                    if (gameRoom.getGameHostId().equals(senderParticipantId)) {
                        //the game host is telling us to declare a new leader, lets make leader
                        //whoever they say is the leader
                        gameRoom.setGameHostId(incomingMessage.data);
                    }
                }
                updateRoomFragment(inRoom);
                updateGameRoomState();
                break;

            case BondfireMessage.TYPE_GAME_INVITE:
                if (d_game_invite) Log.i(TAG, "onRealTimeMessageReceived() GAME INVITE");

                final int gameId = Integer.parseInt(incomingMessage.data);
                try {

                    if (isAppInForeground()) {

                        //are we already participanting in a multiplayer game?
                        if (!roomParticipants.get(clientId).isReadyToReceiveGameData()) {
                            String senderName = getParticipantName(senderParticipantId);
                            try {
                                if (canRecievePopup) {
                                    InviteDialogFragment inviteDialogFragment = InviteDialogFragment.newInstance(1, new InviteDialogFragment.InviteDialogListener() {
                                        @Override
                                        public void onDeclinePressed(Invitation inviteResult) {

                                        }

                                        @Override
                                        public void onJoinPressed(Invitation inviteResult) {
                                            skipToMultiplayer = true;
                                            ((MainActivity) context).runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    //if we're already in the same game, but not in the multiplayer menu,
                                                    //go to multiplayer section
                                                    if (isParticipantInSameGameAsMe(senderParticipantId)) {
                                                        gameReceiver.onGoToMultiplayerModeCommandReceived();
                                                    }

                                                    //else launch the game
                                                    if (d_onJoinPressed)
                                                        Log.i(TAG, "onJoinPressed() Launch GAME");
                                                    ((MainActivity) context).configureGameManager(gameId);
                                                }
                                            });
//                                    }
                                        }
                                    }, GameGridViewFragment.GAME_TITLES[Integer.parseInt(incomingMessage.data)], senderName);

                                    FragmentTransaction ft = ((MainActivity) context).getSupportFragmentManager().beginTransaction();
                                    ft.add(inviteDialogFragment, null);
                                    ft.commitAllowingStateLoss();

                                    ((GamePlayServiceActivity) context).submitEvent(
                                            GamePlayServiceActivity.decryptString(context.getResources()
                                                    .getString(R.string.event_client_received_game_invite_dialog))
                                            , 1);

                                    canRecievePopup = false;
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            canRecievePopup = true;
                                        }
                                    }, 5000);
                                }
                            } catch (IllegalStateException e) {
                                Log.e(TAG, "onRealTimeMessageReceived: Invite Error", e);
                            }
                        }
                    } else {

                        /*
                        Create a notification that takes the user into the app and into the game
                        when pressed
                         */
                        Intent resultIntent = new Intent(context, MainActivity.class);
                        PendingIntent resultPendingIntent =
                                PendingIntent.getActivity(
                                        context,
                                        0,
                                        resultIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );

                        Intent acceptItent = new Intent(context, MainActivity.class);
                        acceptItent.setAction(ACTION_JOIN);
                        acceptItent.putExtra(EXTRA_GAMEID, gameId);
                        PendingIntent acceptPIntent =
                                PendingIntent.getActivity(
                                        context,
                                        0,
                                        acceptItent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );
                        Intent declineIntent = new Intent(service, NetworkService.class);
                        declineIntent.setAction(ACTION_DECLINE);
                        PendingIntent declinePIntent =
                                PendingIntent.getService(service, 0, declineIntent, 0);

                        //create a notification that when pressed takes you inside the app and
                        // inside the game
                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(context)
                                        .setSmallIcon(R.drawable.bf_game_icon)
                                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), GameGridViewFragment.GAME_ICONS[gameId]))
                                        .setContentTitle("Play " + GameGridViewFragment.GAME_TITLES[gameId] + "?")
                                        .setContentText("Invited by " + roomParticipants.get(senderParticipantId).getParticipant().getDisplayName())
                                        .setTicker("Game Invite!")
                                        .setLights(Color.MAGENTA, 1000, 3000)
                                        .setVibrate(vibratePatternGameInvite)
                                        .setAutoCancel(true)
                                        .addAction(0, "Decline", declinePIntent)
                                        .addAction(0, "Accept", acceptPIntent);

                        mBuilder.setContentIntent(resultPendingIntent);

                        NotificationManager mNotifyMgr =
                                (NotificationManager) (context).getSystemService(Context.NOTIFICATION_SERVICE);

                        mNotifyMgr.notify(1, mBuilder.build());

                        //Record the event
                        ((GamePlayServiceActivity) context).submitEvent(
                                GamePlayServiceActivity.decryptString(context.getResources()
                                        .getString(R.string.event_client_received_game_invite_notification)), 1);

                    }
                } catch (NullPointerException e) {
                    Log.e(TAG, "onRealTimeMessageReceived: Exception while receiving game update!", e);
                }

                break;
            case BondfireMessage.TYPE_CHAT_UPDATE: {
                if (d_onRealTimeMessageReceived)
                    Log.i(TAG, "onRealTimeMessageReceived() CHAT UPDATE");
                String senderName = getParticipantName(senderParticipantId);
                if (senderName.isEmpty()) {
                    Log.e(TAG, "onRealTimeMessageReceived: Error, received message from unknown sender");
                    return;
                }
                chatAdapter.add(senderName + ": " + incomingMessage.data);
                chatAdapter.setNotificationVisible();
                ((MainActivity) context).setSocialNotificationOnTabs(TabViewBase.TAB_SOCIAL);
            }
            break;
            case BondfireMessage.TYPE_STATUS_UPDATE:
                if (d_onRealTimeMessageReceived)
                    Log.i(TAG, "onRealTimeMessageReceived() STATUS UPDATE");

                //get the sender Id,
                BondfireParticipant part = roomParticipants.get(senderParticipantId);
                part.setClientStatus(incomingMessage.status_type);
                part.setGameId(incomingMessage.data);
                updateRoomFragment(inRoom);

                //Its possible for player to busy while being in the game, so pass the
                //status to the game so it can react accordingly
                if (incomingMessage.status_type != BondfireMessage.STATUS_LOBBY) {
                    if (gameRoom.isConnected() && gameRoom.getParticipants() != null) {
                        for (int i = 0; i < gameRoom.getParticipants().size; i++) {
                            GameParticipant gameParticipant = gameRoom.getParticipants().get(i);
                            if (gameParticipant.getParticipantId().equals(senderParticipantId)) {
                                gameParticipant.setPlayerStatus(incomingMessage.status_type);
                                break;
                            }
                        }

                        if (isParticipantInSameGameAsMe(senderParticipantId)
                                && gameRoom.isConnectionReady()) {
                            sendReadyMessage(roomParticipants.get(senderParticipantId));
                        }
                        updateGameRoomState();
                    }
                }
                break;

            case BondfireMessage.TYPE_GAME_READY:
                if (d_GAME_READY)

                    Log.i(TAG, "onRealTimeMessageReceived() GAME_READY");

                roomParticipants.get(senderParticipantId).setIsReadyToReceiveGameData(true);
                roomParticipants.get(senderParticipantId).setReadyTime(Long.valueOf(incomingMessage.data2));

                //just another precheck to make sure we are actually in the correct game with the sender
                if (isParticipantInSameGameAsMe(senderParticipantId)) {

                    //mark this participant as capable of receiving game data

                    //Check if we are ready to receive data
                    if (gameRoom.isConnectionReady()) {

                        //adding this person for the first time
                        if (!checkGameParticipantExists(senderParticipantId)) {

                            //Add this person to our game Room
                            if (d_GAME_READY)
                                Log.i(TAG, "onRealTimeMessageReceived() Added " + roomParticipants.get(senderParticipantId).getParticipant().getDisplayName() + "to game Room");
                            GameParticipant gameParticipant = new GameParticipant();
                            gameParticipant.setParticipantId(senderParticipantId);
                            gameParticipant.setParticipantName(getParticipantName(senderParticipantId));
                            gameRoom.getParticipants().add(gameParticipant);

                            checkAndResolveGameHostConflict(incomingMessage, senderParticipantId);

                            //Also let sender know that we are also ready
                            sendReadyMessage(roomParticipants.get(senderParticipantId));
                            //update our game room state
                            updateGameRoomState();
                        } else {
                            checkAndResolveGameHostConflict(incomingMessage, senderParticipantId);
                        }
                    }
                }
                updateRoomFragment(inRoom);

                break;
            case BondfireMessage.TYPE_GAME_LEAVE:

                if (d_GAME_LEAVE) Log.i(TAG, "onRealTimeMessageReceived() GAME_LEAVE");
                try {
                    if (incomingMessage.data.equals(roomParticipants.get(clientId).getGameId())) {

                        if (d_GAME_LEAVE)
                            Log.i(TAG, "onRealTimeMessageReceived() REMOVE " + roomParticipants.get(senderParticipantId).getParticipant().getDisplayName());
                        gameRoom.removeParticipant(senderParticipantId);
                        roomParticipants.get(senderParticipantId).setIsReadyToReceiveGameData(false);
                        roomParticipants.get(senderParticipantId).setReadyTime(0);

                        //If the client was the leader of the game, we must assign a new gameLeader
                        //Was this person host?
                        if (gameRoom.getGameHostId().equals(senderParticipantId)) {
                            if (d_GAME_LEAVE)
                                Log.i(TAG, "onRealTimeMessageReceived() Person that left was host!");
                            assignNewGameHost();
                        }

                        //we are the only one in here, so make us host
                        if (gameRoom.getParticipants().size == 1) {
                            setThisClientAsGameHost();
                        }
                    }
                    updateGameRoomState();
                } catch (NullPointerException e) {
                    Log.e(TAG, "onRealTimeMessageReceived: Received a LEAVE message ", e);
                }
                break;

            case BondfireMessage.TYPE_GAME_DATA:
                try {
                    if (d_onRealTimeMessageReceived)
                        Log.i(TAG, "onRealTimeMessageReceived() got " + incomingMessage.data);
                    if (gameReceiver != null) {
                        gameReceiver.onGameMessageReceived(incomingMessage.data, senderParticipantId);
                    }
                } catch (NullPointerException e) {
                    Log.e(TAG, "onRealTimeMessageReceived: Received a GAME DATA message ", e);
                }
                break;

            case BondfireMessage.TYPE_GAME_SYNC:

                if (gameRoom.getParticipants() != null) {
                    for (int i = 0; i < gameRoom.getParticipants().size; i++) {
                        GameParticipant member = gameRoom.getParticipants().get(i);
                        if (member.getParticipantId().equals(senderParticipantId)) {
                            member.resetWatchDogCounter();
                            break;
                        }
                    }
                }

                break;
            case BondfireMessage.TYPE_ROUND_WIN:
                Log.i(TAG, "onRealTimeMessageReceived() " +
                        "SCORE UPDATE");

                if (roomParticipants != null) {
                    if (roomParticipants.size() > 0) {
                        BondfireParticipant bfParticipant = roomParticipants.get(senderParticipantId);
                        if (bfParticipant != null) {
                            bfParticipant.incrementScore();
                        }
                        updateRoomFragment(inRoom);
                    }
                }

                break;
        }
    }

    /**
     * Assigns a new client to be the game host. If this is called by all clients sharing the game,
     * they should all assign the same leader because the decision is based on who is Game ready the
     * earliest
     */
    private void assignNewGameHost() {

        //check the remaining people in the gameRoom
        GameParticipant potentialLeader = null;
        if (gameRoom.getParticipants().size > 0) {
            //for now just get the first person
            potentialLeader = gameRoom.getParticipants().get(0);
            for (int i = 1; i < gameRoom.getParticipants().size; i++) {

                //compare the join times, and just make the person with the earliest join time the leader
                if (roomParticipants.get(gameRoom.getParticipants().get(i).getParticipantId()).getReadyTime() <
                        roomParticipants.get(potentialLeader.getParticipantId()).getReadyTime()) {
                    potentialLeader = gameRoom.getParticipants().get(i);
                }
            }
        }
        if (potentialLeader != null) {
            if (d_onRealTimeMessageReceived)
                Log.e(TAG, "onRealTimeMessageReceived: New Leader" + potentialLeader.getParticipantName());
            gameRoom.setGameHostId(potentialLeader.getParticipantId());
        }

    }

    /**
     * When receiving a new Game Ready Message, the message contains information that indicates whether
     * the sender sees itself as a Game Host or Game Guest. This function is called when the sender sees itself
     * as a Host AND this client also sees itself as host. There is a conflict of host and both clients
     * need to figure out who is to become Game Host.
     *
     * @param incomingMessage     The received game Ready message
     * @param senderParticipantId the sender participantID
     */
    private void checkAndResolveGameHostConflict(BondfireMessage incomingMessage, String senderParticipantId) {

        if (d_checkAndResolveGameHostConflict)
            Log.i(TAG, "checkAndResolveGameHostConflict() Enter sender is " + incomingMessage.data);

        //check wether this participant sees itself as host or guest and wether we see ourselves as host or guest
        if (incomingMessage.data.equals(HOST) && gameRoom.isHost()) {

            if (d_checkAndResolveGameHostConflict)
                Log.i(TAG, "checkAndResolveGameHostConflict() HOST CONFLICT");
            if (d_checkAndResolveGameHostConflict)
                Log.i(TAG, "checkAndResolveGameHostConflict() MY TIME: " + roomParticipants.get(clientId).getReadyTime());
            if (d_checkAndResolveGameHostConflict)
                Log.i(TAG, "checkAndResolveGameHostConflict() TH TIME: " + Long.valueOf(incomingMessage.data2));

            //there is a conflict, just assign the person with the earliest ready time to be host
            //compare the join times, and just make the person with the earliest join time the leader
            if (roomParticipants.get(clientId).getReadyTime() <
                    Long.valueOf(incomingMessage.data2)) {
                if (d_checkAndResolveGameHostConflict)
                    Log.i(TAG, "checkAndResolveGameHostConflict() Resolve I AM HOST");
                setThisClientAsGameHost();
            } else {
                gameRoom.setGameHostId(senderParticipantId);
                if (d_checkAndResolveGameHostConflict)
                    Log.i(TAG, "checkAndResolveGameHostConflict() Resolve They are HOST");
            }
        } else if (incomingMessage.data.equals(HOST)) {
            gameRoom.setGameHostId(senderParticipantId);
        }
    }

    /**
     * Returns the display name of participant in the participant list
     *
     * @param participantId the participant ID
     * @return the display name of the participant
     */
    private String getParticipantName(String participantId) {
        BondfireParticipant bondfireParticipant = roomParticipants.get(participantId);
        if (bondfireParticipant != null) {
            return bondfireParticipant.getParticipant().getDisplayName().split(" ")[0];
        } else {
            return "Participant";
        }
    }

    /**
     * Check if a participant exists inside a game room
     *
     * @param participantId the participant Id of the client we wish to check
     * @return true if found
     */
    private boolean checkGameParticipantExists(String participantId) {
        if (gameRoom != null) {
            for (GameParticipant participant : gameRoom.getParticipants()) {
                if (participant.getParticipantId().equals(participantId)) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    /**
     * This will mostly be called by a game when it wants to send game data
     *
     * @param targetId   the participant Id of the target client that will get the data
     * @param gameData   the game data
     * @param isReliable indicates wether the message should be reliable or not
     */
    @Override
    public void OnRealTimeMessageSend(String targetId, String gameData, boolean isReliable) {

        RealTimeMessenger.sendGameMessage(
                roomParticipants.get(targetId).getParticipant(),
                inRoom.getRoomId(),
                gameData,
                clientId,
                isReliable
        );
    }

    /**
     * Called by game to pass along its network interface
     *
     * @param receiver the interface that allows network calls to communite with the game
     */
    @Override
    public void bindReceiver(RealTimeMultiplayerMessageReceiver receiver) {
        if (d_bindReceiver) Log.i(TAG, "bindReceiver() ");
        this.gameReceiver = receiver;
        if (gameRoom == null) {
            gameRoom = new GameRoom();
        }
        gameRoom.setConnected(isConnected());
        updateGameRoomState();
    }


    /**
     * called by a host  game client to let others know that they are going to play a game
     */
    @Override
    public void CreateGameInvitations() {

        //If we're connected and we are the client
        if (isConnected()) {
            if (isClientGameHost()) {

                /*
                  Cycle through the participants and see if we can send invitations to people
                 */
                Iterator it = roomParticipants.entrySet().iterator();

                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    BondfireParticipant participant = (BondfireParticipant) pair.getValue();

                    //the client is not ready to receive game data, which means
                    //they are not playing a game in multiplayer mode already, mark them as invitation pending
                    if (!participant.isReadyToReceiveGameData()) {
                        participant.setPendingInvitation(true);
                    }
                }

                sendAnyInvitations();
            }
        }
    }

    /**
     * called by a host game client to destroy any invitations it has, so that it doesn't send out
     * unsent invites
     */
    @Override
    public void DestroyGameInvitations() {

        if (isConnected()) {
            Iterator it = roomParticipants.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                BondfireParticipant participant = (BondfireParticipant) pair.getValue();
                participant.setPendingInvitation(false);
            }
        }
    }

    /**
     * called by a game so that the client knows it is ready to receive game information
     **/
    @Override
    public void setGameConnectionReady() {
        if (d_setGameConnectionReady)
            Log.i(TAG, "setGameConnectionReady() Room Count " + roomParticipants.size());
        ((MainActivity) context).runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        gameRoom.setIsConnectionReady(true);

                        if (isConnected()) {

                            if (d_run) Log.i(TAG, "run() ClientID " + clientId);

                            //add ourselves to the game room
                            GameParticipant participant = new GameParticipant();
                            participant.setParticipantId(clientId);
                            participant.setParticipantName(roomParticipants.get(clientId).getParticipant().getDisplayName());
                            gameRoom.getParticipants().add(participant);
                            if (d_setGameConnectionReady)
                                Log.i(TAG, "setGameConnectionReady() Client ID: " + clientId);
                            gameRoom.setClientId(clientId);
                            roomParticipants.get(clientId).setIsReadyToReceiveGameData(true);
                            roomParticipants.get(clientId).setReadyTime(System.currentTimeMillis());


                            //Wait a certain amount of time before we decide on Game host in case there are Ready messages in transit
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    //Check if we should make ourselves game host. We can only become host if no one else
                                    //is GAme Ready.
                                    boolean shouldClientBeHost = true;
                                    Iterator it = roomParticipants.entrySet().iterator();

                                    while (it.hasNext()) {
                                        Map.Entry pair = (Map.Entry) it.next();
                                        BondfireParticipant part = (BondfireParticipant) pair.getValue();

                                        if (isOurClient(part.getParticipant().getParticipantId()))
                                            continue;

                                        if (isParticipantInSameGameAsMe(part.getParticipant().getParticipantId()) && part.isReadyToReceiveGameData()) {
                                            Log.i(TAG, "run() Should NOT BE HOST");
                                            shouldClientBeHost = false;
                                        } else {
                                            Log.i(TAG, "run() Client going to be GAME HOST, no one seems to be ready yet");
                                        }
                                    }

                                    if (shouldClientBeHost) setThisClientAsGameHost();

                                    //Send to everyone who has the game active
                                    sendReadyMessageToAvailableParticipants();
                                    updateGameRoomState();

                                    //Start the connection sync
                                    startConnectionSyncher();


                                }
                            }, random.nextInt(1200) + 1);

                        } else {
                            //This should never be called here because this method is called IF we see the room is
                            //being connected. This means CONNECTION STATE IS NOT PROPERLY SYNCED
                            if (d_setGameConnectionReady)
                                Log.i(TAG, "setGameConnectionReady() BAD PLACE");
                        }
                    }
                });
    }

    /**
     * Called by a game to indicate to this client and everyone else that a point should be awarded
     * to this client
     */
    @Override
    public void BroadcastWonRound() {
        ((MainActivity) context).runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        if (isConnected()) {
                            roomParticipants.get(clientId).incrementScore();

                            RealTimeMessenger.broadcastRoundWin(
                                    new ArrayList<>(roomParticipants.values()),
                                    inRoom.getRoomId(),
                                    clientId);

                            updateRoomFragment(inRoom);
                        }
                    }
                });
    }

    private void setThisClientAsGameHost() {
        if (gameRoom != null && clientId != null) {
            gameRoom.setGameHostId(clientId);
        }
    }

    /**
     * Let the target participant know they are ready to start receiving data
     *
     * @param participant the target participant
     */
    private void sendReadyMessage(BondfireParticipant participant) {
        if (isConnected()) {
            if (gameRoom != null) {
                RealTimeMessenger.sendGameReadyState(
                        participant.getParticipant(),
                        inRoom.getRoomId(),
                        gameRoom.isHost() ? HOST : GUEST,
                        String.valueOf(roomParticipants.get(clientId).getReadyTime()),
                        clientId
                );
            }
        }
    }

    /**
     * Scan the people in the room and let them know our client is ready to receive game data.
     * This message will only be sent to people that have matching game Ids with the client
     */
    private void sendReadyMessageToAvailableParticipants() {

        Iterator it = roomParticipants.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            BondfireParticipant part = (BondfireParticipant) pair.getValue();

//            if(isParticipantInSameGameAsMe(part.getParticipant().getParticipantId())){

            //Send out message to this participant, except if that participant is us!
            if (isOurClient(part.getParticipant().getParticipantId())) continue;

            sendReadyMessage(part);
//            }
        }
    }

    /**
     * Called by the game to let the client and others know that the client is no longer able to
     * receive game data. gameRoom state is cleared
     */
    @Override
    public void DestroyGameConnection() {
        if (d_DestroyGameConnection) Log.i(TAG, "DestroyGameConnection() ");
        if (isConnected()) {
            try {
                Iterator it = roomParticipants.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    BondfireParticipant part = (BondfireParticipant) pair.getValue();

                    /* only send a LEAVE room message if we see that we have a game ID and
                       if gameId matches with our client's gameID
                     */
                    if (isParticipantInSameGameAsMe(part.getParticipant().getParticipantId())) {

                        //Send out message
                        RealTimeMessenger.sendLeftGameMessage(
                                part.getParticipant(),
                                inRoom.getRoomId(),
                                roomParticipants.get(clientId).getGameId(),
                                gameRoom.isHost() ? HOST : GUEST,
                                clientId
                        );
                    }
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "DestroyGameConnection: Send Leave Message ", e);
            }

            roomParticipants.get(clientId).setIsReadyToReceiveGameData(false);
        }
        if (gameRoom != null) {
            gameRoom.setIsConnectionReady(false);
            gameRoom.getParticipants().clear();
            gameRoom.setGameHostId("");
        }
        //stop trying to sync
        stopConnectionSyncher();
    }

    /**
     * Send an invite to the specified participant
     *
     * @param participant the target participant
     */
    public void sendGameInvitation(Participant participant) {
        if (isConnected()) {
            RealTimeMessenger.sendGameInvite(
                    participant,
                    inRoom.getRoomId(),
                    String.valueOf(((MainActivity)context).getMGameManager().getInformation().gameId),
                    clientId);
        }
    }

    /**
     * Called by a Host game client to let joining members know that the host is about to star
     * t playing a game
     */
    private void sendAnyInvitations() {
        if (isConnected()) {
            if (isClientGameHost()) {

                //Iterate through the room participant list and see if they have a pending invitation
                Iterator it = roomParticipants.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    BondfireParticipant participant = (BondfireParticipant) pair.getValue();

                    if (participant.isPendingInvitation()) {
                        if (participant.getParticipant().getStatus() == Participant.STATUS_JOINED) {
                            sendGameInvitation(participant.getParticipant());
                            participant.setPendingInvitation(false);
                        }
                    }
                }
            }
        }
    }

    /**
     * the game will inquire wether it should skip to multiplayer when it loads up
     */
    public void setSkipToMultiplayer(boolean skipToMultiplayer) {
        this.skipToMultiplayer = skipToMultiplayer;
    }

    /**
     * Called by a game to ee if it should go  ahead to its multiplayer state after starting up
     *
     * @return true means yes
     */
    @Override
    public boolean shouldGoToMultiplayerMenu() {
        boolean shouldSkip = skipToMultiplayer;
        skipToMultiplayer = false;
        return shouldSkip;
    }

    /**
     * Room status Update Listener
     **/
    @Override
    public void onRoomConnecting(Room room) {
        if (d_onRoomConnecting) Log.i(TAG, "onRoomConnecting() Enter");
        inRoom = room;
        updateRoomFragment(room);
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        if (d_onRoomAutoMatching) Log.i(TAG, "onRoomAutoMatching() Enter");
        inRoom = room;
        updateRoomFragment(room);
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> list) {
        if (d_onPeerInvitedToRoom) Log.i(TAG, "onPeerInvitedToRoom() Enter");
        inRoom = room;
        updateRoomFragment(room);
    }

    @Override
    public void onPeerDeclined(Room room, List<String> list) {
        if (d_onPeerDeclined) Log.i(TAG, "onPeerDeclined() Enter");
        inRoom = room;
        updateRoomFragment(room);
    }

    @Override
    public void onPeerJoined(Room room, List<String> list) {
        if (d_onPeerJoined) Log.i(TAG, "onPeerJoined() Enter");
        inRoom = room;
        updateRoomFragment(room);
    }

    @Override
    public void onPeerLeft(Room room, List<String> list) {
        if (d_onPeerLeft) Log.i(TAG, "onPeerLeft() ENter");
        inRoom = room;
        updateRoomFragment(room);
    }

    /**
     * Called  by Google when we are connected to the gameRoom. We're not ready to play yet
     **/
    @Override
    public void onConnectedToRoom(Room room) {
        if (d_onConnectedToRoom) Log.i(TAG, "onConnectedToRoom() Enter");

        //
        updateBondfireParticipants(room.getParticipants());
        clientId = room.getParticipantId(Games.Players.getCurrentPlayerId(googleApiClient));

        //save the gameRoom
        inRoom = room;
    }

    /**
     * Called by Google to let client know they have been disconnected from the room
     *
     * @param room updated Rooom
     */
    @Override
    public void onDisconnectedFromRoom(Room room) {
        if (d_onDisconnectedFromRoom) Log.i(TAG, "onDisconnectedFromRoom() Enter");

        Toast.makeText(context, "You left the room", Toast.LENGTH_SHORT).show();
        inRoom = room;
        updateGameRoomState();

        if (service != null) {
            service.stopForeground(true);
            /*if (!isBoundToService) {
                googleApiClient.disconnect();
            }*/
        }
        leaveRoom();
    }

    @Override
    public void onPeersConnected(Room room, List<String> list) {
        if (d_onPeersConnected) Log.i(TAG, "onPeersConnected() Enter");

        inRoom = room;
        updateRoomFragment(room);

        //pend invitations for the new arrivals
        if (isConnected()) {
            if (gameRoom != null) {
                if (gameRoom.isConnectionReady() && gameRoom.isHost()) {
                    for (String participantId : list) {
                        BondfireParticipant participant = roomParticipants.get(participantId);
                        participant.setPendingInvitation(true);
                    }
                }
            }
        }

        sendAnyInvitations();
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> list) {
        if (d_onPeersDisconnected) Log.i(TAG, "onPeersDisconnected() Enter");

        inRoom = room;
        updateRoomFragment(room);
    }

    @Override
    public void onP2PConnected(String s) {
        if (d_onP2PConnected) Log.i(TAG, "onP2PConnected() Enter " + s);
    }

    @Override
    public void onP2PDisconnected(String s) {
        if (d_onP2PDisconnected) Log.i(TAG, "onP2PDisconnected() Enter " + s);
    }

    /**
     * Room Update Listener
     */
    @Override
    public void onRoomCreated(int statusCode, Room room) {
        if (d_onRoomCreated) Log.i(TAG, "onRoomCreated() Enter");

        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "onRoomCreated: Error: onRoomCreated, status " + statusCode);
            return;
        }

        //get client ID
        clientId = room.getParticipantId(Games.Players.getCurrentPlayerId(googleApiClient));

        inRoom = room;
        ((MainActivity)context).showRealTimeRoomFragment();

        //create a room object to pass to the game
        if (gameRoom == null) {
            gameRoom = new GameRoom();
        }

        updateRoomFragment(room);

        //wait a few seconds to receive game information from other players
        if (d_onRoomCreated) Log.i(TAG, "onRoomCreated() Waiting 1500 ms");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (d_onRoomCreated) Log.i(TAG, "onRoomCreated() Done Waiting");
                gameRoom.setConnected(true); //we are connected to a game already, so just go through
                updateGameRoomState();
            }
        }, 1500);
    }

    /**
     * Called when the client has a joined a room
     *
     * @param statusCode of the event
     * @param room       the new room status
     */
    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        if (d_onJoinedRoom) Log.i(TAG, "onJoinedRoom() Enter");

        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "onJoinedRoom: Error: onRoomJoined, Status " + statusCode);
            return;
        }

        ((MainActivity)context).showRealTimeRoomFragment();

        //set notifications
        participantAdapter.setNotificationVisible();
        ((MainActivity) context).setSocialNotificationOnTabs(TabViewBase.TAB_SOCIAL);

        //create a new room to pass to the game
        if (gameRoom == null) {
            gameRoom = new GameRoom();
        }

        //update the participants and then set connection to true
        inRoom = room;
        clientId = room.getParticipantId(Games.Players.getCurrentPlayerId(googleApiClient));
        updateRoomFragment(room);
        gameRoom.setConnected(true);

        //record the event
        switch (room.getParticipants().size()) {
            case 1:
                ((GamePlayServiceActivity) context).submitEvent(
                        GamePlayServiceActivity.decryptString(context.getResources().getString(R.string.event_client_joined_room)), 1);
                break;
            case 2:
                ((GamePlayServiceActivity) context).submitEvent(
                        GamePlayServiceActivity.decryptString(context.getResources().getString(R.string.event_client_joined_a_room_of_2_participants)), 1);
                break;
            case 3:
                ((GamePlayServiceActivity) context).submitEvent(
                        GamePlayServiceActivity.decryptString(context.getResources().getString(R.string.event_client_joined_a_room_of_3_participants)), 1);
                break;
            case 4:
                ((GamePlayServiceActivity) context).submitEvent(
                        GamePlayServiceActivity.decryptString(context.getResources()
                                .getString(R.string.event_client_joined_a_room_of_4_participants)), 1);
                break;
            case 5:
                ((GamePlayServiceActivity) context).submitEvent(
                        GamePlayServiceActivity.decryptString(context.getResources()
                                .getString(R.string.event_client_joined_a_room_of_5_participants)), 1);
                break;
            case 6:
                ((GamePlayServiceActivity) context).submitEvent(
                        GamePlayServiceActivity.decryptString(context.getResources()
                                .getString(R.string.event_client_joined_a_room_of_6_participants)), 1);
                break;
            case 7:
                ((GamePlayServiceActivity) context).submitEvent(
                        GamePlayServiceActivity.decryptString(context.getResources()
                                .getString(R.string.event_client_joined_a_room_of_7_participants)), 1);
                break;
            case 8:
                ((GamePlayServiceActivity) context).submitEvent(
                        GamePlayServiceActivity.decryptString(context.getResources()
                                .getString(R.string.event_client_joined_a_room_of_8_participants)), 1);
                break;
        }
    }

    /**
     * we have left room, return to main screen
     */
    @Override
    public void onLeftRoom(int statusCode, String s) {
        if (d_onLeftRoom) Log.i(TAG, "onLeftRoom() ENter");
        MainActivity.Companion.showNoRealTimeRoomFragment();

        //we reset room and THEN updateGameRoomState()
        resetVariables();
        updateGameRoomState();
    }

    /**
     * called w
     * hen gameRoom is fully connected, I.e. all players are in the gameRoom
     */
    @Override
    public void onRoomConnected(int statusCode, Room room) {
        if (d_onRoomConnected) Log.i(TAG, "onRoomConnected() enter");

        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "onRoomConnected: **Error: onRoomConnected " + statusCode);
            return;
        }

        if (participantAdapter != null) {
            participantAdapter.setNotificationVisible();
        }

        ((MainActivity) context).setSocialNotificationOnTabs(TabViewBase.TAB_SOCIAL);
        inRoom = room;
        participantAdapter.setRoomConnected(true);

        //Notify everyone of my  status
        getStatusAndBroadcast();
        //update the room and its roomParticipants
        updateRoomFragment(room);

        if (!isAppInForeground()) {
            Intent resultIntent = new Intent(context, MainActivity.class);
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            context,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setContentTitle("Bondfire")
                    .setContentText("People are joining the room!")
                    .setSmallIcon(R.drawable.bf_bird_icon)
                    .setLights(Color.MAGENTA, 3000, 5000)
                    .setVibrate(vibratePatternRoomConnect)
                    .setAutoCancel(true);

            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotifyMgr =
                    (NotificationManager) (context).getSystemService(Context.NOTIFICATION_SERVICE);

            mNotifyMgr.notify(1, mBuilder.build());

        }
    }

    /**
     * Update the participants list which the client is handling
     *
     * @param participants the newly received list of participants
     */
    private void updateBondfireParticipants(ArrayList<Participant> participants) {
        for (Participant p : participants) {
            BondfireParticipant bondfireParticipant = roomParticipants.get(p.getParticipantId());
            if (bondfireParticipant != null) {
                bondfireParticipant.setParticipant(p);
            } else {
                try {
                    BondfireParticipant newParticipant = new BondfireParticipant();
                    newParticipant.setParticipant(p);
                    newParticipant.setClientStatus(-1);
                    newParticipant.setGameId("");
                    newParticipant.setIsHost(false);
                    roomParticipants.put(p.getParticipantId(), newParticipant);
                } catch (NullPointerException e) {
                    Log.e(TAG, "updateBondfireParticipants: ", e);
                }
            }
        }
    }

    /**
     * Updates the participant listview
     *
     * @param room the room object containing participant statuses
     */
    public void updateRoomFragment(Room room) {
        if (d_updateRoom) Log.i(TAG, "updateRoomFragment() ");
        try {
            if (room != null) {
                updateBondfireParticipants(room.getParticipants());
            }
            if (roomParticipants != null) {
                if (d_updateRoom)
                    Log.i(TAG, "updateRoomFragment() Size:" + roomParticipants.size());
                //set the other people
                participantAdapter.clear();
                participantAdapter.addAll(new ArrayList<>(roomParticipants.values()));
                participantAdapter.notifyDataSetChanged();
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "updateRoomFragment: Null", e);
        }
    }

    public void updateInviteeStatus(List<String> list) {
        //we also need to update invitee status so we know to send out invitations





/*
        if (hasInvitations()) {
            //create a new list
            ArrayList<Participant> newInvitees = new ArrayList<>();
            for (int i = 0; i < roomParticipants.size(); i++) {
                Participant participant = roomParticipants.get(i);
                for (Iterator<Participant> iterator = invitationsRemaining.iterator(); iterator.hasNext(); ) {
                    Participant invitee = iterator.next();
                    if (invitee.getParticipantId().equals(participant.getParticipantId())) {
                        newInvitees.add(participant);
                        iterator.remove();
                    }
                }
            }
            invitationsRemaining = newInvitees;
        }
*/
    }

    /**
     * Update the room state for the game.
     */
    public void updateGameRoomState() {
        if (d_updateGameRoomState) Log.i(TAG, "updateGameRoomState() Enter");

        if (gameRoom != null) {
            for (GameParticipant participant : gameRoom.getParticipants()) {
                if (d_updateGameRoomState)
                    Log.i(TAG, "updateGameRoomState() " + participant.getParticipantName());
            }
        }
        if (gameReceiver != null) {
            gameReceiver.onRoomConfigurationChanged(gameRoom);
        }
    }


    /**
     * Accept the received room invitation
     *
     * @param invId the participant Id of the inviter
     */
    public void acceptInviteRoom(String invId) {
        if (d_acceptInviteRoom) Log.i(TAG, "acceptInviteRoom() Enter");

        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
        roomConfigBuilder.setInvitationIdToAccept(invId)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
        Games.RealTimeMultiplayer.join(googleApiClient, roomConfigBuilder.build());
    }

    /**
     * called when the user has declined invitation to join gameRoom
     **/
    public void declineInviteRoom(String invId) {
        Games.RealTimeMultiplayer.declineInvitation(googleApiClient, invId);
    }

    /**
     * Leave the current room
     */
    public void leaveRoom() {
        if (d_leaveRoom) Log.i(TAG, "leaveRoom() Enter");
        if (isConnected()) {
            Games.RealTimeMultiplayer.leave(googleApiClient, this, inRoom.getRoomId());
            MainActivity.Companion.showNoRealTimeRoomFragment();
            stopConnectionSyncher();
            resetVariables();
        }
    }

    /**
     * Reset variable states to a state where nothing is connected yet
     */
    private void resetVariables() {

        roomParticipants.clear();
        clientId = "";

        if (service != null) {
            service = null;
        }

        if (gameRoom != null) {
            gameRoom.getParticipants().clear();
            gameRoom.setGameHostId("");
            gameRoom.setIsConnectionReady(false);
            gameRoom.setConnected(false);
            gameRoom.setClientId("");
        }

        if (participantAdapter != null) {
            participantAdapter.clear();

            participantAdapter.setRoomConnected(false);
        }

        if (chatAdapter != null) {
            chatAdapter.clear();
        }

        if (inRoom != null) {
            inRoom = null;
        }
    }

    /**
     * handle the result of "select players UI" we launched when the user clicked the
     * "Invite friends" button. We react by creating a gameRoom with those players
     *
     * @param data contains information about how to set up the room
     */
    public void handleInvitePlayersToRoom(Intent data) {
        if (d_handleInvitePlayersToRoom) Log.i(TAG, "handleInvitePlayersToRoom() Enter");

        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        if (d_handleInvitePlayersToRoom)
            Log.i(TAG, "handleInvitePlayersToRoom() Found " + invitees.size() + "  players");

        //get the automatch criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(minAutoMatchPlayers, maxAutoMatchPlayers, 0);
        }

        //Create the gameRoom
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.addPlayersToInvite(invitees);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        if (autoMatchCriteria != null) {
            rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        }
        Games.RealTimeMultiplayer.create(googleApiClient, rtmConfigBuilder.build());
    }

    // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.
    public void handleInvitationInboxResult(Intent data) {
        Log.d(TAG, "Invitation Coming from Invitation Box.");
        Invitation inv = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);
        // accept invitation
        if (inv != null) {
            acceptInviteRoom(inv.getInvitationId());
        } else {
            Toast.makeText(context, "Unable to Join", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isConnected() {
        return inRoom != null;
    }

    /**
     * Called when the the client needs to broadcast a new chat message to everyone
     *
     * @param message the chat message
     */
    public void broadcastChatMessage(String message) {
        if (isConnected()) {
            //send to everyone else
            RealTimeMessenger.sendChatMessage(
                    new ArrayList<>(roomParticipants.values()),
                    inRoom.getRoomId(),
                    message,
                    clientId);
            //update our chat
            chatAdapter.add(Games.Players.getCurrentPlayer(googleApiClient).getDisplayName().split(" ")[0] + ": " + message);
        }
    }

    /**
     * Called when the client needs to broadcast to everyone else the status of the client
     * If the status is STATUS_GAME we also include the gameId
     *
     * @param client_status the client status
     */
    public void broadcastClientStatus(int client_status) {
        if (d_broadcastClientStatus) Log.i(TAG, "broadcastClientStatus() " + client_status);
        try {
            if (isConnected()) {
                String gameId = "";
                if (((MainActivity) context).getMGameManager().isLoaded()) {
                    gameId = String.valueOf(((MainActivity) context).getMGameManager().getInformation().gameId);
                }
                if (client_status == BondfireMessage.STATUS_LOBBY) {
                    //we just entered the lobby, destroy any existing game connections
                    DestroyGameConnection();
                }
                RealTimeMessenger.broadcastClientStatus(
                        new ArrayList<>(roomParticipants.values()),
                        inRoom.getRoomId(),
                        client_status,
                        gameId,
                        clientId);
                roomParticipants.get(clientId).setClientStatus(client_status);
                roomParticipants.get(clientId).setGameId(gameId);
                updateRoomFragment(inRoom);

            }
        } catch (NullPointerException e) {
            Log.e(TAG, "broadcastClientStatus: Information was likely null", e);
        }
    }

    /**
     * This class is destroying.
     */
    public void onDestroy() {
        if (d_onDestroy) Log.i(TAG, "onDestroy() ");
        if (!isConnected()) {
            if (d_onDestroy) Log.i(TAG, "onDestroy() Destroyed Messenger");
            RealTimeMessenger.destroy();
            resetVariables();
        }
    }


    /**
     * @return true if we are host of game
     */
    public boolean isClientGameHost() {
        if (isConnected()) {
            if (gameRoom != null) {
                if (gameRoom.isConnectionReady()) {
                    if (gameRoom.isHost()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check if the given participant is in the same game as us
     *
     * @param participantId of the client we wish to check
     * @return true if they are playing the same game
     */
    public static boolean isParticipantInSameGameAsMe(String participantId) {
        try {
            if (participantId != null && clientId != null) {
                if (!participantId.isEmpty() && !clientId.isEmpty()) {
                    if (roomParticipants != null) {
                        if (roomParticipants.size() > 0) {
                            return roomParticipants.get(participantId).getGameId().equals(roomParticipants.get(clientId).getGameId());
                        }
                    }
                }
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "isParticipantInSameGameAsMe: Tried to compare a null participant", e);
        }
        return false;
    }


    /**
     * Check if the given participant Id is the local client
     *
     * @param participantId to check
     * @return true if it is this client
     */
    public static boolean isOurClient(String participantId) {
        if (clientId != null) {
            if (!clientId.isEmpty()) {
                return participantId.equals(clientId);
            }
        }
        return false;
    }


    /**
     * Determine the status of the client and update for others
     */
    public void getStatusAndBroadcast() {
        if (d_getStatusAndBroadcast) Log.i(TAG, "getStatusAndBroadcast() ");


        if (isConnected()) {
            if (isAppInForeground()) {
                if (((MainActivity) context).getMGameManager().isLoaded()) {
                    broadcastClientStatus(BondfireMessage.STATUS_GAME);
                } else {
                    broadcastClientStatus(BondfireMessage.STATUS_LOBBY);
                }
            } else {
                broadcastClientStatus(BondfireMessage.STATUS_BUSY);
            }
        }
    }

    /**
     * Change the room host
     *
     * @param newHostId the participant Id of the client to become new host
     */
    public void changeHost(String newHostId) {
        if (isConnected()) {

            //only execute this command from a Host Client
            if (gameRoom != null) {
                if (!newHostId.equals(gameRoom.getGameHostId())) {
                    if (gameRoom.isConnectionReady() && isParticipantInSameGameAsMe(newHostId)) {
                        gameRoom.setGameHostId(newHostId);

                        RealTimeMessenger.sendHostChangeMessage(
                                new ArrayList<>(roomParticipants.values()), inRoom.getRoomId(), newHostId, clientId);

                        updateRoomFragment(inRoom);
                        updateGameRoomState();

                    }
                }
            }
        }
    }

    /**
     * Create a quick game
     **/
    public void automatch() {
        // auto-match criteria to invite one random automatch opponent.
        Bundle am = RoomConfig.createAutoMatchCriteria(1, 7, 0);

        // build the room config:
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
        roomConfigBuilder.setAutoMatchCriteria(am);
        RoomConfig roomConfig = roomConfigBuilder.build();

        // create room:
        Games.RealTimeMultiplayer.create(googleApiClient, roomConfig);
    }

    /* when a game is running, there needs to be a system to check if the player is still connected.
       If the watchdog timer runs out, it must tell that to the game, so it can react accordingly.*/
    private Handler watchDogHandler;
    private static int WATCHDOG_CHECK_INTERNAL = 3000; //check every 3 seconds for sync message
    private static int WATCHDOG_SYNC_INTERVAL = 3300; //send a message out every 3 seconds

    /**
     * send out a sync message every few seconds
     */
    Runnable sendSyncRunnable = new Runnable() {
        @Override
        public void run() {
            if (d_run) Log.i(TAG, "run() SEND SYNC MESSAGE");
            try {
                //send out a connection every 4 seconds
                if (gameRoom.getParticipants() != null) {
                    for (int i = 0; i < gameRoom.getParticipants().size; i++) {

                        BondfireParticipant participant =
                                roomParticipants.get(gameRoom.getParticipants()
                                        .get(i).getParticipantId());

                        if (isOurClient(participant.getParticipant().getParticipantId())) continue;
                        RealTimeMessenger.sendGameSyncMessage(participant.getParticipant(), inRoom.getRoomId());
                    }
                }
            } finally {
                watchDogHandler.postDelayed(sendSyncRunnable, WATCHDOG_SYNC_INTERVAL);
            }
        }
    };

    /**
     * Check if clients are still connected
     */
    Runnable checkSyncRunable = new Runnable() {
        @Override
        public void run() {
            if (d_run) Log.i(TAG, "run() CHECK WATCH DOG");
            try {
                if (gameRoom.getParticipants() != null) {
                    for (int j = 0; j < gameRoom.getParticipants().size; j++) {
                        GameParticipant participant = gameRoom.getParticipants().get(j);

                        if (isOurClient(participant.getParticipantId())) continue;
                        if (participant.isWatchDogExpired()) {
                            if (d_run)
                                Log.i(TAG, "run() EXPIRED FOR " + participant.getParticipantName());
                            gameRoom.removeParticipant(participant.getParticipantId());

                            if (d_run) Log.i(TAG, "run() OLD HOST:" + gameRoom.getGameHostId());
                            if (d_run) Log.i(TAG, "run() Part:" + participant.getParticipantId());

                            if (gameRoom.getGameHostId().equals(participant.getParticipantId())) {
                                //assigne a new host
                                if (d_run) Log.i(TAG, "run() Assign new host");
                                assignNewGameHost();
                            }
                            updateGameRoomState();
                        }
                    }
                }
            } finally {
                watchDogHandler.postDelayed(checkSyncRunable, WATCHDOG_CHECK_INTERNAL);
            }
        }
    };

    /**
     * Tell the client to start accepting and send sync messages.
     */
    private void startConnectionSyncher() {

        sendSyncRunnable.run();

        for (GameParticipant participant : gameRoom.getParticipants()) {
            participant.resetWatchDogCounter();
        }
        checkSyncRunable.run();
    }

    /**
     * Tell the client to stop accepting and sending sync messages
     */
    private void stopConnectionSyncher() {
        watchDogHandler.removeCallbacks(sendSyncRunnable);
        watchDogHandler.removeCallbacks(checkSyncRunable);
    }

    private boolean isAppInForeground() {
        return isBoundToService;
    }

    public void leftAppFromNotification() {
        /*try {
            ((GamePlayServiceActivity) context).submitEvent(
                    GamePlayServiceActivity.decryptString(context.getResources()
                            .getString(R.string.event_user_left_room_from_the_notification)), 1);
        } catch (IllegalStateException e) {
            Log.i(TAG, "onStartCommand: Tried to record event", e);
        } catch (ClassCastException e) {
            Log.i(TAG, "onStartCommand: Tried to record event, but cannot cast", e);
        }*/
    }
}