package com.bondfire.app.android.network.turnbasedmultiplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.badlogic.gdx.Gdx;
import com.bondfire.app.R;
import com.bondfire.app.android.activity.MainActivity;
import com.bondfire.app.android.adapter.TurnBasedMatchesAdapter;
import com.bondfire.app.android.adapter.TurnBasedParticipantsAdapter;
import com.bondfire.app.android.managers.MatchManager;
import com.bondfire.app.handler.EventShowScore;
import com.bondfire.app.handler.EventLeaveGame;
import com.bondfire.app.handler.EventNotYourTurn;
import com.bondfire.app.handler.TurnBasedEvent;
import com.bondfire.app.handler.EventQueue;
import com.bondfire.app.handler.EventYourTurn;
import com.bondfire.app.services.TurnBasedMultiplayerActionListener;
import com.bondfire.app.services.TurnBasedMultiplayerDataPacket;
import com.bondfire.app.services.TurnBasedMultiplayerReceiverListener;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;

import java.util.ArrayList;

/**This class helps manage the Turn Based Multiplayer connection aspect of bondfire
 * Here we handle callbacks that come from the Net AND from the user taking action
 * in their client*/
public class TurnBasedMultiplayerManager implements
        OnTurnBasedMatchUpdateReceivedListener
{

    /** DEBUG SWICHES allows us to view quickly enable/disable message to help us monitor the processes*/
    private final static String Tag = TurnBasedMultiplayerManager.class.getName();
    private final static boolean d_startMatch = false;
    private final static boolean d_updateMatch = false;
    private final static boolean d_onCheckgamesClicked = false;
    private final static boolean d_onFindPLayersClicked = false;
    private final static boolean d_handleSelectPlayers = false;
    private final static boolean d_onQuickMatchClicked = false;
    private final static boolean d_quickMatchResulCallback = false;
    private final static boolean d_onCancelClicked = false;
    private final static boolean d_onLeaveClicked = false;
    private final static boolean d_onFinishedClicked = false;
    private final static boolean d_onDoneCLicked = false;
    private final static boolean d_getNextParticipantId = false;
    private final static boolean d_rematch = false;
    private final static boolean d_processResultCancel = false;
    private final static boolean d_processresultLeave = false;
    private final static boolean d_processResultUpdate = false;
    private final static boolean d_processResultInitiate = false;
    private final static boolean d_askForRematch =false;
    private final static boolean d_saveCurrentGame = false;
    private final static boolean d_updateMatchFromOnConnect = false;
    private final static boolean d_generateListener = false;
    private final static boolean d_injectReceiver = false;
    private final static boolean d_ExamineEventDelay = false;
    private final static boolean d_handlelookatMatches = false;
    private final static boolean d_setViewVisibility = false;
    private final static boolean d_clean = false;
    private final static boolean d_updateParticipantList = false;
    private final static boolean d_actionListener =false;
    private final static boolean d_injectUI =false;
    private final static boolean d_eventNotYourTurn =false;

    static TurnBasedMultiplayerReceiverListener mReceiverListener; //Listener for when we receive information from the net

    //Temp UI
    EditText etData;   //TODO DELETE
    TextView tvTurnCount;//TODO DELETE
    TextView playerName;//TODO DELETE
    LinearLayout llMatchup ;//TODO DELETE
    LinearLayout llgamePlay;//TODO DELETE

    TurnBasedParticipantsAdapter participantList;         //holds reference to the participants list

    MatchManager mMatchManager;

    public final static int RC_SELECT_PLAYERS = 10003;    //user returned from Select Player activity
    public final static int RC_LOOK_AT_MATCHES = 10004;   //user returned from find matches activity

    public boolean isDoingTurn = false;                   // Should I be showing the turn API?
    public TurnBasedMatch mMatch;                         // This is the current match we're in; null if not loaded
    private GoogleApiClient mGoogleApiClient;             //the google client
    private Activity act;                                 //reference to the main activity
    private EventQueue events;                            //holds our events and our game data
    private int tempGameId = -1;

    public TurnBasedMultiplayerManager(Activity activity){
        this.act = activity;
        this.events = new EventQueue();
        this.mMatchManager = new MatchManager();
    }

    /**
     *  Pass the Google Api Client around
     * @param client
     */
    public void setApiClient(GoogleApiClient client){
        this.mGoogleApiClient = client;
        this.mMatchManager.setGoogleClient(client);
        mMatchManager.LoadMatches(-1);
    }

    /** Displays your inbox. You will get back handleApiConnectivity where
     you will need to figure out what you clicked on.*/
    public void onCheckGamesClicked() {
        if(d_onCheckgamesClicked)Log.e(Tag,"onCheckGamesClicked()");
        Intent intent = Games.TurnBasedMultiplayer.getInboxIntent(mGoogleApiClient);
        this.act.startActivityForResult(intent, RC_LOOK_AT_MATCHES);
    }

    /** display the dialog which allows user to pick opponents/teamnates for a match **/
    public void onFindPlayersClicked(int gameId, int min,int max){
        if(d_onFindPLayersClicked)Log.e(Tag,"onFindPlayersClicked()");
        tempGameId = gameId;

        Intent intent = Games .TurnBasedMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, min, max);
        act.startActivityForResult(intent, RC_SELECT_PLAYERS);
    }

    /** This is what happens when we return from the opponent pick dialog */
    public void handleSelectPlayers(Intent data){
        if(d_handleSelectPlayers)Log.e(Tag,"handleSelectPlayers()");

        //get invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

        //get auto-match criteria
        Bundle autoMatchCriteria;
        int minAutoMatchPlayers = data.getIntExtra(
                Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(
                Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
        } else {
            autoMatchCriteria = null;
        }

        //This game did not load up properly, need to restart
        if(tempGameId == -1){
            Log.e(Tag,"UNABLE TO START GAME, Reason: Incorrect Game ID");
            return;
        }

        //Everything went ok. create a match with the given criteria.
        TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
                .addInvitedPlayers(invitees)
                .setAutoMatchCriteria(autoMatchCriteria)
                .setVariant(tempGameId)
                .build();

        // Create and start the match.
        Games.TurnBasedMultiplayer
                .createMatch(mGoogleApiClient, tbmc)
                .setResultCallback(new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.InitiateMatchResult initiateMatchResult) {
                        processResult(initiateMatchResult);
                    }
                });
    }

    /**This is picked when we return  from the game inbox. */
    public void handleLookAtMatches(Intent data){
        if(d_handlelookatMatches)Log.e(Tag,"handleLookAtMatches() enter");

        TurnBasedMatch match = data
                .getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH);
        if (match != null) {
            updateMatch(match,false);
        }
    }

    /** when the user clicks the quickmatch*/
    public void onQuickMatchClicked(int gameId) {
        if(d_onQuickMatchClicked)Log.e(Tag,"onQuickMatchClicked(), gameId: " + gameId);

        tempGameId = gameId;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                1, 5, 0);
        TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
                .setAutoMatchCriteria(autoMatchCriteria).setVariant(gameId).build();

        // Start the match
        ResultCallback<TurnBasedMultiplayer.InitiateMatchResult> cb = new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
            @Override
            public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
                if(d_quickMatchResulCallback) Gdx.app.log(Tag,"onResult() QuickMatch");
                processResult(result);
            }
        };

        Games.TurnBasedMultiplayer.createMatch(mGoogleApiClient, tbmc).setResultCallback(cb);
    }

    /**
     *  Cancel the game. Should possibly wait until the game is canceled before
     giving up on the view.
     */
    public void onCancelClicked() {

        if(d_onCancelClicked)Log.e(Tag,"onCancelClicked()");
        Games.TurnBasedMultiplayer.cancelMatch(mGoogleApiClient, mMatch.getMatchId())
                .setResultCallback(new ResultCallback<TurnBasedMultiplayer.CancelMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.CancelMatchResult result) {
                        processResult(result);
                    }
                });

        isDoingTurn = false;
    }

    /** The user can leave the game at any time. This function decides if it should leave the game
     * on its turn, or not on its turn.
     * **/
    public void onLeaveClicked() {
        if(d_onLeaveClicked)Log.e(Tag,"d_onLeaveClicked() Enter");

        //Is it our turn?
        if(mMatch.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN){
            if(d_onLeaveClicked)Log.e(Tag,"onLeaveClicked() Leaving on my turn");
            String nextParticipantId = getNextParticipantId(); //Calculate this inside the game

            if(nextParticipantId != null && !nextParticipantId.isEmpty()){
                Games.TurnBasedMultiplayer.leaveMatchDuringTurn(mGoogleApiClient, mMatch.getMatchId(),
                        nextParticipantId).setResultCallback(
                        new ResultCallback<TurnBasedMultiplayer.LeaveMatchResult>() {
                            @Override
                            public void onResult(TurnBasedMultiplayer.LeaveMatchResult result) {
                                processResult(result);
                            }
                        });
            }else{
                Log.e(Tag,"No New participants, cancel game");
                onCancelClicked();
            }
        } else if (mMatch.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN){

            if(d_onLeaveClicked)Log.e(Tag,"onLeaveClicked() Leaving not on my turn");
                Games.TurnBasedMultiplayer.leaveMatch(mGoogleApiClient, mMatch.getMatchId()).setResultCallback(
                        new ResultCallback<TurnBasedMultiplayer.LeaveMatchResult>() {
                            @Override
                            public void onResult(TurnBasedMultiplayer.LeaveMatchResult result) {
                                processResult(result);
                            }
                        });
        }
    }

    /**
     * onFinishClicked Notifies the manager that the game is finished
     */
    public void onFinishClicked() {
        if(d_onFinishedClicked)Log.e(Tag,"onFinishedClicked()");
        Games.TurnBasedMultiplayer.finishMatch(mGoogleApiClient, mMatch.getMatchId())
                .setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                        processResult(result);
                    }
                });
        isDoingTurn = false;
        updateUI();
    }

    /**
     * Notifies the manager that a player has completed their turn
     * @param turn The Next person's turn
     */
    public void onDoneClicked(String turn) {
        if(d_onDoneCLicked)Log.e(Tag,"onDoneClicked()");


        //TODO max data size is 131072

        // Create the next turn
        TurnBasedMultiplayerDataPacket data1 = events.getData();
        Gdx.app.log(Tag,"DATA:" + data1.gameData);
        Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mMatch.getMatchId(),
                ((TurnBasedPacket) data1).persist(),turn).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                        processResult(result);
                    }
                });
    }

    // If you choose to rematch, then call it and wait for a response.
    public void rematch() {
        if(d_rematch)Log.e(Tag,"rematch()");
        Games.TurnBasedMultiplayer.rematch(mGoogleApiClient, mMatch.getMatchId()).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
                        processResult(result);
                    }
                });
        mMatch = null;
        isDoingTurn = false;
    }

    // Rematch dialog
    public void askForRematch() {
        if(d_askForRematch)Log.e(Tag,"askForRematch()");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(act);
        alertDialogBuilder.setMessage("Do you want a rematch?");
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Sure, rematch!",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                rematch();
                            }
                        })
                .setNegativeButton("No.",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
        alertDialogBuilder.show();
    }

    /**
     * Get the next participant. In this function, we assume that we are
     * round-robin, with all known players going before all automatch players.
     * This is not a requirement; players can go in any order. However, you can
     * take turns in any order.
     *
     * @return participantId of next player, or null if automatching
     */

    //TODO this needs to be decided in game
    public String getNextParticipantId() {
        if(d_getNextParticipantId)Log.e(Tag,"getNextParticipantId()");

        if(mGoogleApiClient == null){
            Log.e(Tag,"API CLIENT IS NULL");
        }

        String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
        String myParticipantId = mMatch.getParticipantId(playerId);

        ArrayList<String> participantIds = mMatch.getParticipantIds();

        for(int i = 0; i < participantIds.size(); i++){
            System.out.println("Player: " + i + " is "+ participantIds.get(i));
        }

        int desiredIndex = -1;

        for (int i = 0; i < participantIds.size(); i++) {
            if (participantIds.get(i).equals(myParticipantId)) {
                desiredIndex = i + 1;
            }
        }

        if (desiredIndex < participantIds.size()) {
            return participantIds.get(desiredIndex);
        }

        if (mMatch.getAvailableAutoMatchSlots() <= 0) {
            // You've run out of automatch slots, so we start over.
            return participantIds.get(0);
        } else {
            // You have not yet fully automatched, so null will find a new
            // person to play against.
            return null;
        }
    }

    //when we cancel a match
    private void processResult(TurnBasedMultiplayer.CancelMatchResult result) {
        if(d_processResultCancel)Log.e(Tag,"precessResultCancel()");

        if (!checkStatusCode(null, result.getStatus().getStatusCode())) {
            return;
        }

        isDoingTurn = false;
        mMatchManager.removeMatch(mMatch.getMatchId());
        EventLeaveMatch(getMyParticipandId(),true);
        mMatch = null;
        updateUI();
        showWarning("Match",
                "This match is canceled.  All other players will have their game ended.");
    }

    //when we leave match
    private void processResult(TurnBasedMultiplayer.LeaveMatchResult result) {
        if(d_processresultLeave)Log.e(Tag,"processResultLeave()");

        TurnBasedMatch match = result.getMatch();
        if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
            if(d_processresultLeave)Log.e(Tag,"did not get a match");
            return;
        }

        if(match == null){
            Log.e(Tag, "Match is null?");
        }

        isDoingTurn = false;
        mMatchManager.removeMatch(mMatch.getMatchId());
        EventLeaveMatch(getMyParticipandId(),true);
        mMatch = null;
        updateUI();
    }

    //called when we start a new match
    public void processResult(TurnBasedMultiplayer.InitiateMatchResult result){
        if(d_processResultInitiate)Log.e(Tag,"processResultInitiate(), Enter");
        TurnBasedMatch match = result.getMatch();

        if(!checkStatusCode(match, result.getStatus().getStatusCode())){
            return;
        }

        mMatchManager.addMatch(match);

        if(match.getData() != null){
            //this is a game that has already started, so i'l just start
            updateMatch(match,true);
            return;
        }
        startMatch(match);
    }

    //called when a turn is over and its a new person's turn
    public void processResult(TurnBasedMultiplayer.UpdateMatchResult result) {
        if (d_processResultUpdate) Log.e(Tag, "processResultUpdate() Enter");

        TurnBasedMatch match = result.getMatch();
        if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
            return;
        }
        if (match.canRematch()) {

        }

        try{
            updateMatch(match,true);
        }catch (NullPointerException e){
            Log.e(Tag,"processResult() Update, match is null",e);
        }

        updateUI();
    }

    public void updateMatchFromOnConnect(TurnBasedMatch match){
        if(d_updateMatchFromOnConnect)Log.e(Tag,"updateMatchFromOnConnect()");
        this.mMatch = match;

       if(match.getData() != null){
           //data is not null, we have a previous game
           updateMatch(match,false);
           return;
       }
        startMatch(match);
    }

    public void updateMatch(TurnBasedMatch match, boolean isFromNetwork) {
        if (d_updateMatch) Log.e(Tag, "UpdateMatch()");
        mMatch = match;

        int status = match.getStatus();
        int turnStatus = match.getTurnStatus();


        switch (status) {
            case TurnBasedMatch.MATCH_STATUS_CANCELED:
                showWarning("Canceled!", "This game was canceled!");
                EventTheirTurn(getMyParticipandId(),isFromNetwork);
                return;
            case TurnBasedMatch.MATCH_STATUS_EXPIRED:
                showWarning("Expired!", "This game is expired.  So sad!");
                return;
            case TurnBasedMatch.MATCH_STATUS_AUTO_MATCHING:
                showWarning("Waiting for auto-match...",
                        "We're still waiting for an automatch partner.");
                return;
            case TurnBasedMatch.MATCH_STATUS_COMPLETE:
                // Note that in this state, you must still call "Finish" yourself,
                // so we allow this to continue.
                if(turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE)
                {
                    EventLeaveMatch(getMyParticipandId(),isFromNetwork);
                    showWarning(
                            "Complete!",
                            "This game is over; someone finished it, and so did you!  There is nothing to be done.");
                    break;
                }
                showWarning("Complete!",
                        "This game is over; someone finished it!  You can only finish it now.");
                EventShowScore(getMyParticipandId(),isFromNetwork); //and in here you will call finish
                //TODO Finish the game yourself
                return;
        }

        // OK, it's active. Check on turn status.
        switch (turnStatus) {
            case TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN:
                if (d_updateMatch) Log.e(Tag, "UpdateMatch() My Turn");
                EventYourTurn(getMyParticipandId(),isFromNetwork);
                break;
            case TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN:
                if (d_updateMatch) Log.e(Tag, "UpdateMatch() Not your turn");
//                showWarning("Alas...", "It's not your turn.");
                EventTheirTurn(getMyParticipandId(),isFromNetwork);
                break;
            case TurnBasedMatch.MATCH_TURN_STATUS_INVITED:
                showWarning("Good inititative!",
                        "Still waiting for invitations.\n\nBe patient!");
                break;
        }

        updateUI();
        mMatchManager.updateMatch(match);
        updateParticipantList(mMatch);
    }

    public void saveCurrentGame(){
        try{
            if(d_saveCurrentGame)Log.e(Tag,"saveCurrentGame()");
            if(mMatch != null){
                if(mMatch.getStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN){

                    if(d_saveCurrentGame)Log.e(Tag,"My Turn, saving game");

                    String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
                    String myParticipantId = mMatch.getParticipantId(playerId);

                    Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mMatch.getMatchId(),
                            ((TurnBasedPacket)events.getData()).persist(), myParticipantId).setResultCallback(
                            new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                                @Override
                                public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                                    processResult(result);
                                }
                            });
                }
            }
        }catch (NullPointerException e) {
            Log.e(Tag,"SaveCurrentGame() : (EventsQueue)Events is most likely Null",e);
        }

    }

    public void startMatch(TurnBasedMatch match) {
        if(d_startMatch)Log.e(Tag,"startMatch()");

        TurnBasedPacket  mTurnData = new TurnBasedPacket();
        // Some basic turn data
        ArrayList<String> ids = match.getParticipantIds();

        mTurnData.gameId     = tempGameId;
        mTurnData.turnCounter = 0;
        mTurnData.gameData = "NEW";
        mTurnData.playerCount = ids.size();

        //get the pics so far
        String[] icons = new String[ids.size()];
        String[] images = new String[ids.size()];
        for(int i =0; i < ids.size(); i++){
            icons[i] = match.getParticipant (ids.get(i)).getIconImageUrl();
            images[i] = match.getParticipant(ids.get(i)).getHiResImageUrl();
            Log.e(Tag,"URL: " + icons[i]);
            Log.e(Tag,"IMAGES: " + images[i]);
        }

        mTurnData.iconStrings = icons;
        mTurnData.playerImages = images;

        mMatch = match;

        String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
        String myParticipantId = mMatch.getParticipantId(playerId);

        Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, match.getMatchId(),
                mTurnData.persist(), myParticipantId).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                        processResult(result);
                    }
                });
    }


    @Override
    public void onTurnBasedMatchReceived(TurnBasedMatch turnBasedMatch) {
        mMatch =turnBasedMatch;
        Toast.makeText(act, "A match was updated, got a MATCH", Toast.LENGTH_SHORT).show();
        updateMatch(turnBasedMatch,true);

      /*  try {
            //TODO use the EventQueue
//            mReceiverListener.onTurnBasedMatchReceived(isDoingTurn);
        }catch (NullPointerException e) {
            Log.e(Tag, "Receiver is Null",e);
        }*/
    }

    @Override
    public void onTurnBasedMatchRemoved(String s) {
        Toast.makeText(act, "A match was removed.", Toast.LENGTH_SHORT).show();

       /* try{
            mReceiverListener.onTurnBasedMatchRemoved("removed match");
        }catch (NullPointerException e) {
            Log.e(Tag, "Receiver is Null",e);
        }*/
    }

    /***** CHECK THE MATCH EVENTS *****/
    // Returns false if something went wrong, probably. This should handle
    // more cases, and probably report more accurate results.
    private boolean checkStatusCode(TurnBasedMatch match, int statusCode) {
        switch (statusCode) {
            case GamesStatusCodes.STATUS_OK:
                return true;
            case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_DEFERRED:
                // This is OK; the action is stored by Google Play Services and will
                // be dealt with later.
                Toast.makeText(
                        act,
                        "Stored action for later.  (Please remove this toast before release.)",
                        Toast.LENGTH_SHORT).show();
                // NOTE: This toast is for informative reasons only; please remove
                // it from your final application.
                return true;
            case GamesStatusCodes.STATUS_MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
                showErrorMessage(match, statusCode,
                        R.string.status_multiplayer_error_not_trusted_tester);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_ALREADY_REMATCHED:
                showErrorMessage(match, statusCode,
                        R.string.match_error_already_rematched);
                break;
            case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_FAILED:
                showErrorMessage(match, statusCode,
                        R.string.network_error_operation_failed);
                break;
            case GamesStatusCodes.STATUS_CLIENT_RECONNECT_REQUIRED:
                showErrorMessage(match, statusCode,
                        R.string.client_reconnect_required);
                break;
            case GamesStatusCodes.STATUS_INTERNAL_ERROR:
                showErrorMessage(match, statusCode, R.string.internal_error);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_INACTIVE_MATCH:
                showErrorMessage(match, statusCode,
                        R.string.match_error_inactive_match);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_LOCALLY_MODIFIED:
                showErrorMessage(match, statusCode,
                        R.string.match_error_locally_modified);
                break;
            default:
                showErrorMessage(match, statusCode, R.string.unexpected_status);
                Log.d(Tag, "Did not have warning or string to deal with: "
                        + statusCode);
        }
        return false;
    }

    public void showErrorMessage(TurnBasedMatch match, int statusCode,  int stringId) {
        showWarning("Warning", act.getResources().getString(stringId));
    }
    // Generic warning/info dialog
    public void showWarning(String title, String message) {

        AlertDialog mAlertDialog;         /// our dialog to display messages
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(act);

        // set title
        alertDialogBuilder.setTitle(title).setMessage(message);

        // set dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                    }
                });
        // create alert dialog
        mAlertDialog = alertDialogBuilder.create();

        // show it
        mAlertDialog.show();
    }

    public void injectParticipantAdapter(TurnBasedParticipantsAdapter adapter){
        if(d_injectUI)Log.e(Tag,"injectUI, LISTVIEW");
        this.participantList = adapter;
        participantList.clear();
        participantList.addAll(events.getParticipants());
        participantList.notifyDataSetChanged();
    }

    public void injectMatchesAdapter(TurnBasedMatchesAdapter adapter, int gameId){
        mMatchManager.setMatchesAdpater(adapter,gameId);
    }

    public void injectParticipantAdapter(TextView textView, EditText editText, TextView player, LinearLayout matchup, LinearLayout gamePlay){
        if(d_injectUI)Log.e(Tag,"injectUI, Lots of stuff");
        this.playerName = player;
        this.etData = editText;
        this.tvTurnCount = textView;
        this.llMatchup = matchup;
        this.llgamePlay = gamePlay;
    }

    // Update the visibility based on what state we're in.
    public void updateUI() {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (d_setViewVisibility) Log.e(Tag, "updateUI()");

        /* playerName.setText(Games.Players.getCurrentPlayer(
                 mGoogleApiClient).getDisplayName()); */
                if (mMatch != null) {
                    if (d_setViewVisibility) Log.e(Tag, "showingRoom()");
                    ((MainActivity)act).getmSectionsPagerAdapter().showTurnBasedRoom();
                } else {
                    if (d_setViewVisibility) Log.e(Tag, "showingMatches()");
                    ((MainActivity)act).getmSectionsPagerAdapter().showTurnBasedMatches();
                }
            }
        });
    }

    /** we return access to this class' methods via this call. A game that uses
     * this service will call this method
     * @return generateListener
     */
    public TurnBasedMultiplayerActionListener generateListener(
    ){
        if(d_generateListener) Log.e(Tag,"generatingListener()");

        return new TurnBasedMultiplayerActionListener() {
            @Override
            public void onCancelMatch() {
                onCancelClicked();
            }

            @Override
            public void onLeaveMatch() {
                onLeaveClicked();
            }

            @Override
            public void onTakeTurn(String data) {
                if(d_actionListener)Log.e(Tag,"Action Listener Received TakeTurn Command");
                onDoneClicked(data); //TODO PASS DATA ON JSON FORMAT
            }

            @Override
            public void onFinishMatch() {
                onFinishClicked();
            }

            @Override
            public void onFindPeopleToMatchWith(int gameId, int min, int max) {
                onFindPlayersClicked(gameId,min,max);
            }

            @Override
            public void onInbox() {
                onCheckGamesClicked();
            }

            @Override
            public void onAutoMatch(int gameId) {
                onQuickMatchClicked(gameId);
            }

            @Override
            public void injectReceiverListener(TurnBasedMultiplayerReceiverListener listener) {
                if(d_injectReceiver)Log.e(Tag,"InjectReceiver() " + ((listener == null) ? "NULL":"OK"));
                mReceiverListener = listener;
                events.UpdateCallBacks(listener);
                if(!events.ExecuteAll()){
                    ExamineEventDelay();
                }
            }
        };
    }

    private void ExamineEventDelay(){
        if(d_ExamineEventDelay)Log.e(Tag,"ExamineEventDelay()");
        if(mReceiverListener == null){
            if(d_ExamineEventDelay)Log.e(Tag,"Listener Not Registered, registering");
            //There is no receiver call back registered, probably because the game doesn't accept
            ((MainActivity) act).configureGameManager(events.getData().gameId);
        }else{
            if(d_ExamineEventDelay)Log.e(Tag,"Not sure whats wrong...");
        }
    }

    public void updateParticipantList(TurnBasedMatch match) {
        try{
            if (d_updateParticipantList) Log.e(Tag, "updateParticipantList()");

            if (d_updateParticipantList) Log.e(Tag, "updateParticipantList() Updating List");
            ArrayList<String> people = events.getParticipants();
            people.clear();
            ArrayList<String> participantIds = mMatch.getParticipantIds();
            for (String participantId : participantIds) {
                people.add(match.getParticipant(participantId).getDisplayName());
            }
            events.updateParticipants(people);

            if (participantList != null) {
                injectParticipantAdapter(participantList);
            } else {
                if (d_updateParticipantList) Log.e(Tag, "updateParticipantList() List is null");
            }
        }catch (NullPointerException e){
            Log.e(Tag,"updateParticipantList()",e);
        }
    }

    /**
     *  When the user clicks on match on our match item list, we load up game information
     *  **/
    public void continueMatch(String matchId){
        try{
            updateMatch(mMatchManager.getMatch(matchId),false);
        }catch (NullPointerException e){
            Log.e(Tag,"continueMatch() Returned a null Match",e);
        }
    }
    /***************************************** EVEN UPDATES  ******************************************/

    private void EventYourTurn(String id,boolean isFromNetwork){
        isDoingTurn = true;
        try {
            //pass new data to the game events object
            events.UpdateData(TurnBasedPacket.unpersist(mMatch.getData()));
            EventYourTurn turnBasedEvent = new EventYourTurn(id,isFromNetwork, mReceiverListener);
            if(!turnBasedEvent.execute()) {
                events.push(turnBasedEvent);
                ExamineEventDelay();
            }
        } catch (NullPointerException e) {
            Log.e(Tag, "UpdateMatch() Exeception at my turn", e);
        }
    }

    private void EventTheirTurn(String id,boolean isFromNetwork){
        if(d_eventNotYourTurn)Log.e(Tag,"EventTheirTurn()");

        isDoingTurn = false;
        try{
            //pass new data to the game events object
            events.UpdateData(TurnBasedPacket.unpersist(mMatch.getData()));
            EventNotYourTurn turnBasedEvent = new EventNotYourTurn(id,isFromNetwork,mReceiverListener);
            if(!turnBasedEvent.execute()){
                events.push(turnBasedEvent);
                ExamineEventDelay();
            }
        }catch (NullPointerException e){
            Log.e(Tag, "UpdateMatch() @ not my turn", e);
        }
    }

    private void EventLeaveMatch(String id, boolean isFromNetwork){
        if(d_eventNotYourTurn)Log.e(Tag,"EventTheirTurn()");
        try{
            //pass new data to the game events object
            events.UpdateData(null);
            EventLeaveGame turnBasedEvent = new EventLeaveGame(id,isFromNetwork,mReceiverListener);
            if(!turnBasedEvent.execute()){
                events.push(turnBasedEvent);
                ExamineEventDelay();
            }
        }catch (NullPointerException e){
            Log.e(Tag, "UpdateMatch() @ not my turn", e);
        }
    }

    private void EventShowScore(String id,boolean isFromNetwork){
        if(d_eventNotYourTurn)Log.e(Tag,"EventTheirTurn()");
        try{
            //pass new data to the game events object
            events.UpdateData(null);
            EventShowScore turnBasedEvent = new EventShowScore(id,isFromNetwork,mReceiverListener);
            if(!turnBasedEvent.execute()){
                events.push(turnBasedEvent);
                ExamineEventDelay();
            }
            //TODO remove this garbage;
        }catch (NullPointerException e){
            Log.e(Tag, "UpdateMatch() @ not my turn", e);
        }
    }

    public static void cleanGameInformation(){
        if(d_clean)Log.e(Tag,"Clean");
        mReceiverListener = null;
        TurnBasedEvent.clean();
        EventQueue.clean();
    }

    private String getMyParticipandId(){
        String playerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
        return mMatch.getParticipantId(playerId);
    }
}
