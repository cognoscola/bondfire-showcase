/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bondfire.app.android.utils.social;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.bondfire.app.android.activity.MainActivity;
import com.bondfire.app.android.fragment.InviteDialogFragment;
import com.bondfire.app.android.network.realtime.BondfireMessage;
import com.bondfire.app.android.network.realtime.RealTimeManager;
import com.bondfire.app.android.network.turnbasedmultiplayer.TurnBasedMultiplayerManager;
import com.bondfire.app.android.services.NetworkService;
//import com.google.android.gms.appstate.AppStateManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Games.GamesOptions;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
//import com.google.android.gms.games.request.GameRequest;
import com.google.android.gms.plus.Plus;

public class NetworkManager implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnInvitationReceivedListener{

    /** Debug **/
    private static final String TAG = NetworkManager.class.getName();
    private final static boolean d_onStop = false;
    public static final boolean d_onConnected = true;
    public static final boolean d_handleApiConnectivity = true;
    public static final boolean d_onInvitationReceived = true;
    public static final boolean d_onInvitationRemoved = true;
    private final static boolean d_onDestroy =true;
    private final static boolean d_onServiceConnected = true;
    private final static boolean d_onServiceDisconnected =true;
    private final static boolean d_NetworkManager = true;

    /** Listener for sign-in success or failure events. */
    public interface GoogleSignInListener {
        /**
         * Called when sign-in fails. As a result, a "Sign-In" button can be
         * shown to the user; when that button is clicked, call */
        void onSignInFailed();

        /** Called when sign-in succeeds. */
        void onSignInSucceeded();
    }

    // configuration done?
    private boolean mSetupDone = false;

    // are we currently connecting?
    private boolean mConnecting = false;

    // Are we expecting the result of a resolution flow?
    boolean mExpectingResolution = false;

    // was the sign-in flow cancelled when we tried it?
    // if true, we know not to try again automatically.
    boolean mSignInCancelled = false;

    /*
     * The Activity we are bound to. We need to keep a reference to the Activity
     * because some games methods require an Activity (a Context won't do). We
     * are careful not to leak these references: we release them on onStop().
     */
    Activity mActivity = null;

    // app context
    Context mAppContext = null;

    // Request code we use when invoking other Activities to complete the
    // sign-in flow.
    public final static int RC_RESOLVE = 9001;

    // Request code when invoking Activities whose result we don't care about.
    final static int RC_UNUSED = 9002;

    // the Google API client builder we will use to create GoogleApiClient
    GoogleApiClient.Builder mGoogleApiClientBuilder = null;

    // Api options to use when adding each API, null for none
    GamesOptions mGamesApiOptions = GamesOptions.builder().build();
//    PlusOptions mPlusApiOptions = null;
//    NoOptions mAppStateApiOptions = null;

    // Google API client object we manage.
    GoogleApiClient mGoogleApiClient = null;

    // Client request flags
    public final static int CLIENT_NONE = 0x00;
    public final static int CLIENT_GAMES = 0x01;
    public final static int CLIENT_PLUS = 0x02;
    public final static int CLIENT_APPSTATE = 0x04;
    public final static int CLIENT_SNAPSHOT = 0x08;
    public final static int CLIENT_ALL = CLIENT_GAMES | CLIENT_PLUS
            | CLIENT_APPSTATE | CLIENT_SNAPSHOT;

    // What clients were requested? (bit flags)
    int mRequestedClients = CLIENT_NONE;

    // Whether to automatically try to sign in on onStart(). We only set this
    // to true when the sign-in process fails or the user explicitly signs out.
    // We set it back to false when the user initiates the sign in process.
    boolean mConnectOnStart = true;

    /*
     * Whether user has specifically requested that the sign-in process begin.
     * If mUserInitiatedSignIn is false, we're in the automatic sign-in attempt
     * that we try once the Activity is started -- if true, then the user has
     * already clicked a "Sign-In" button or something similar
     */
    boolean mUserInitiatedSignIn = false;

    // The connection result we got from our last attempt to sign-in.
    ConnectionResult mConnectionResult = null;

    // The error that happened during sign-in.
    SignInFailureReason mSignInFailureReason = null;

    // Should we show error dialog boxes?
    boolean mShowErrorDialogs = true;

    // Print debug logs?
    boolean mDebugLog = true;

    Handler mHandler;
    /*
     * If we have incoming requests when we connected to the games client, they
     * are here. Otherwise, it's null.
     */
    //TODO fix this
//    ArrayList<GameRequest> mRequests;

    // Listener
    GoogleSignInListener mListener = null;

    // Should we start the flow to sign the user in automatically on startup? If
    // so, up to
    // how many times in the life of the application?
    static final int DEFAULT_MAX_SIGN_IN_ATTEMPTS = 3;
    int mMaxAutoSignInAttempts = DEFAULT_MAX_SIGN_IN_ATTEMPTS;

    /* a turn-based manager in case the game requires the service **/
    private TurnBasedMultiplayerManager mTurnManager;

    /* a real-time manager in case the game requires the service **/
    private RealTimeManager mRealTimeManager;

    //the network service class
    private NetworkService networkService;

    //we use this to connect/bind to a network service
    private ServiceConnection serviceConnector;

    private boolean isBound;

    //save an instance of the dialog incase we need to cancel it later
    private InviteDialogFragment inviteDialogFragment;

    public void setSkipGameToMultiplayer(int skipGameToMultiplayer) {
        this.skipGameToMultiplayer = skipGameToMultiplayer;
    }

    private int skipGameToMultiplayer = -1; //indicates if a game should skip to multiplayer

    public static NetworkManager newInstance(Activity activity, int clientsToUse){
        return new NetworkManager(activity, clientsToUse);
    }

    /**
     * Construct a NetworkManager object, initially tied to the given Activity.
     * After constructing this object, call @link{setup} from the onCreate()
     * method of your Activity.
     *
     * @param clientsToUse
     *            the API clients to use (a combination of the CLIENT_* flags,
     *            or CLIENT_ALL to mean all clients).
     */
    public NetworkManager( Activity activity, int clientsToUse) {

        setContext(activity);
        mRequestedClients = clientsToUse;
        mHandler = new Handler();
        mTurnManager = new TurnBasedMultiplayerManager(mActivity);
        //create a connection to a service if it exists
//        mRealTimeManager = RealTimeManager.newInstance(activity);

    }

    public void setContext(Activity activity) {
        this.mActivity = activity;
        this.mAppContext = activity.getApplicationContext();
    }


    public void bindtoService(){
        serviceConnector = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if(d_onServiceConnected) Log.i(TAG, "onServiceConnected()");

                NetworkService.LocalBinder binder = (NetworkService.LocalBinder) service;
                networkService = binder.getService();

                isBound = true;

                //Check if realtime manager instance exists within the service, if not it means the
                //service probably was not running in the background, which means we are not previously
                //connected into a room
                if (networkService.hasRealTimeManager()) {
                    mRealTimeManager = networkService.getRealTimeManager();
                    mRealTimeManager.setNewContext(mActivity);
                }else{
                    mRealTimeManager = RealTimeManager.newInstance(mActivity);
                    if (mGoogleApiClient != null) {
                        mRealTimeManager.setGoogleApiClient(mGoogleApiClient);
                    }
                    networkService.setRealTimeManager(mRealTimeManager);
                }

                //check if we are connected to a room, if so , switch to the Room Fragment
                if (mRealTimeManager.isConnected()) {
                    if(d_onServiceConnected) Log.i(TAG, "onServiceConnected() Connected To a Room");
                    ((MainActivity)mActivity).showRealTimeRoomFragment();
                }

                mRealTimeManager.setService(networkService);
                mRealTimeManager.setIsBoundToService(true);

                if (skipGameToMultiplayer != -1) {
                    mRealTimeManager.setSkipToMultiplayer(true);
                    ((MainActivity)mActivity).configureGameManager(skipGameToMultiplayer);
                    skipGameToMultiplayer = -1;
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name){
                if(d_onServiceDisconnected) Log.i(TAG, "onServiceDisconnected() ");
                isBound = false;
                mRealTimeManager.setIsBoundToService(false);
            }
        };


        /* Note: we remove this check because we need to keep a reference to the service
         * somewhere, even if the app closes, while the client is connected to a room.
         */
//        if (networkService == null) {
            if(d_NetworkManager) Log.i(TAG, "NetworkManager() Binding to service");
            mAppContext.startService(new Intent(mAppContext, NetworkService.class));
            Intent intent = new Intent(mAppContext, NetworkService.class);
            mAppContext.bindService(intent, serviceConnector, Context.BIND_AUTO_CREATE);
//        }
    }


    /**
     * Sets the maximum number of automatic sign-in attempts to be made on
     * application startup. This maximum is over the lifetime of the application
     * (it is stored in a SharedPreferences file). So, for example, if you
     * specify 2, then it means that the user will be prompted to sign in on app
     * startup the first time and, if they cancel, a second time the next time
     * the app starts, and, if they cancel that one, never again. Set to 0 if
     * you do not want the user to be prompted to sign in on application
     * startup.
     */
//    public void setMaxAutoSignInAttempts(int max) {
//        mMaxAutoSignInAttempts = max;
//    }

    void assertConfigured(String operation) {
        if (!mSetupDone) {
            String error = "NetworkManager error: Operation attempted without setup: "
                    + operation
                    + ". The setup() method must be called before attempting any other operation.";
            logError(error);
            throw new IllegalStateException(error);
        }
    }

    /*private void doApiOptionsPreCheck() {
        if (mGoogleApiClientBuilder != null) {
            String error = "NetworkManager: you cannot call set*ApiOptions after the client "
                    + "builder has been created. Call it before calling createApiClientBuilder() "
                    + "or setup().";
            logError(error);
            throw new IllegalStateException(error);
        }
    }*/

    /**
     * Sets the options to pass when setting up the Games API. Call before
     * setup().
     */
    /*public void setGamesApiOptions(GamesOptions options) {
        doApiOptionsPreCheck();
        mGamesApiOptions = options;
    }*/

    /**
     * Sets the options to pass when setting up the AppState API. Call before
     * setup().
     */
    /*public void setAppStateApiOptions(NoOptions options) {
        doApiOptionsPreCheck();
        mAppStateApiOptions = options;
    }*/

    /**
     * Sets the options to pass when setting up the Plus API. Call before
     * setup().
     */
    /*public void setPlusApiOptions(PlusOptions options) {
        doApiOptionsPreCheck();
        mPlusApiOptions = options;
    }*/

    /**
     * Creates a GoogleApiClient.Builder for use with @link{#setup}. Normally,
     * you do not have to do this; use this method only if you need to make
     * nonstandard setup (e.g. adding extra scopes for other APIs) on the
     * GoogleApiClient.Builder before calling @link{#setup}.
     */
    public GoogleApiClient.Builder createApiClientBuilder() {
        if (mSetupDone) {
            String error = "NetworkManager: you called NetworkManager.createApiClientBuilder() after "
                    + "calling setup. You can only get a client builder BEFORE performing setup.";
            logError(error);
            throw new IllegalStateException(error);
        }

        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(
                mActivity, this, this);

        if (0 != (mRequestedClients & CLIENT_GAMES)) {
            builder.addApi(Games.API, mGamesApiOptions);
            builder.addScope(Games.SCOPE_GAMES);
        }

        if (0 != (mRequestedClients & CLIENT_PLUS)) {
            builder.addApi(Plus.API);
            builder.addScope(Plus.SCOPE_PLUS_LOGIN);
        }

        if (0 != (mRequestedClients & CLIENT_APPSTATE)) {
            //TODO FIX ME
            /*builder.addApi(AppStateManager.API);
            builder.addScope(AppStateManager.SCOPE_APP_STATE);*/
        }

        if (0 != (mRequestedClients & CLIENT_SNAPSHOT)) {
            builder.addScope(Drive.SCOPE_APPFOLDER);
            builder.addApi(Drive.API);
        }


        mGoogleApiClientBuilder = builder;
        return builder;
    }

    /**
     * Performs setup on this NetworkManager object. Call this from the onCreate()
     * method of your Activity. This will create the clients and do a few other
     * initialization tasks. Next, call @link{#onStart} from the onStart()
     * method of your Activity.
     *
     * @param listener
     *            The listener to be notified of sign-in events.
     */
    public void setup(GoogleSignInListener listener) {
        if (mSetupDone) {
            String error = "NetworkManager: you cannot call NetworkManager.setup() more than once!";
            logError(error);
            throw new IllegalStateException(error);
        }
        mListener = listener;
        debugLog("Setup: requested clients: " + mRequestedClients);

        if (mGoogleApiClientBuilder == null) {
            // we don't have a builder yet, so create one
            createApiClientBuilder();
        }

        mGoogleApiClient = mGoogleApiClientBuilder.build();
        mGoogleApiClientBuilder = null;
        mSetupDone = true;
        mTurnManager.setApiClient(mGoogleApiClient);
    }

    /**
     * Returns the GoogleApiClient object. In order to call this method, you
     * must have called @link{setup}.
     */
    public GoogleApiClient getApiClient() {
        if (mGoogleApiClient == null) {
            throw new IllegalStateException(
                    "No GoogleApiClient. Did you call setup()?");
        }
        return mGoogleApiClient;
    }

    /** Returns whether or not the user is signed in. */
    public boolean isSignedIn() {
        return mGoogleApiClient != null && mGoogleApiClient.isConnected();
    }

    /** Returns whether or not we are currently connecting */
    public boolean isConnecting() {
        return mConnecting;
    }
    /**
     * Returns whether or not there was a (non-recoverable) error during the
     * sign-in process.
     */
    public boolean hasSignInError() {
        return mSignInFailureReason != null;
    }
    /**
     * Returns the error that happened during the sign-in process, null if no
     * error occurred.
     */
    public SignInFailureReason getSignInError() {
        return mSignInFailureReason;
    }

    // Set whether to show error dialogs or not.
//    public void setShowErrorDialogs(boolean show) {
//        mShowErrorDialogs = show;
//    }

    /** Call this method from your Activity's onStart(). */
    public void onStart(Activity act) {

        mActivity = act;
        mAppContext = act.getApplicationContext();

        debugLog("onStart");
        assertConfigured("onStart");

        if (mConnectOnStart) {
            if (mGoogleApiClient.isConnected()) {
                Log.w(TAG,
                        "NetworkManager: client was already connected on onStart()");
            } else {
                debugLog("Connecting client.");
                mConnecting = true;
                mGoogleApiClient.connect();
            }
        } else {
            debugLog("Not attempting to connect becase mConnectOnStart=false");
            debugLog("Instead, reporting a sign-in failure.");
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    notifyListener(false);
                }
            }, 1000);
        }
    }

    /** Call this method from your Activity's onStop(). */
    public void onStop() {
        debugLog("onStop");
        assertConfigured("onStop");

        if (isBound) {
            try {
                if (d_onDestroy) Log.i(TAG, "onDestroy() Unbinding");
                mAppContext.unbindService(serviceConnector);
                mActivity = null;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "onStop: Tried to stop service!", e);
            }
        }

        try {
            mRealTimeManager.broadcastClientStatus(BondfireMessage.STATUS_BUSY);
        } catch (NullPointerException e) {
            Log.e(TAG, "onStop: tried to use the manager but it probably didn't have time to load",e);
        }
        //save game before disconnecting
        if (d_onStop) Log.e(TAG, "Saving game if available");
        if (mTurnManager != null && mGoogleApiClient.isConnected()) {
            mTurnManager.saveCurrentGame();
        } else {
            if (d_onStop) Log.e(TAG, "Unable to save game state");
        }

        //We only disconnect if we know we aren't connected to a real time room
        if (mGoogleApiClient.isConnected() && !mRealTimeManager.isConnected()) {
            debugLog("Disconnecting client due to onStop");
            mGoogleApiClient.disconnect();
        } else {
            debugLog("Either client is connected to room, or we alreeady disconnected");
        }
        mConnecting = false;
        mExpectingResolution = false;
    }

    /**Destroy anything if we must because the app is closing */
    public void onDestroy(){
        mRealTimeManager.DestroyGameConnection();
        mRealTimeManager.onDestroy();
    }

    /**
     * Returns the requests received through the onConnected bundle. This should
     * be called from your NetworkManagerListener's
     * Method, to check if there are  incoming requests that must be handled.
     * @return The requests, or null if none were received.
     */
    /*public ArrayList<GameRequest> getRequests() {
        if (!mGoogleApiClient.isConnected()) {
            Log.w(TAG, "Warning: getRequests() should only be called "
                    + "when signed in, "
                    + "that is, after getting onSignInSuceeded()");
        }
        return mRequests;
    }*/

    /** Enables debug logging */
    public void enableDebugLog(boolean enabled) {

        Log.e(TAG, "Enable debug");
        mDebugLog = enabled;
        if (enabled) {
            debugLog("Debug log enabled.");
        }
    }

    /** Sign out and disconnect from the APIs. */
    public void signOut() {

        //If we are connected to a real time room, leave it
        if (mRealTimeManager.isConnected()) {
            mRealTimeManager.leaveRoom();
            //Since we sign out right away, the client will not receive a "onLeftRoom" callback,
            //so just change the fragment manually
            MainActivity.Companion.showNoRealTimeRoomFragment();

            //TODO
//            MainActivity.getActionBarView().hideChatControls();
        }

        if (!mGoogleApiClient.isConnected()) {
            // nothing to do
            debugLog("signOut: was already disconnected, ignoring.");
            return;
        }

        // for Plus, "signing out" means clearing the default account and
        // then disconnecting
        if (0 != (mRequestedClients & CLIENT_PLUS)) {
            debugLog("Clearing default account on PlusClient.");
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
        }

        // For the games client, signing out means calling signOut and
        // disconnecting
        if (0 != (mRequestedClients & CLIENT_GAMES)) {
            debugLog("Signing out from the Google API Client.");
            Games.signOut(mGoogleApiClient);
        }

        // Ready to disconnect
        debugLog("Disconnecting client.");
        mConnectOnStart = false;
        mConnecting = false;
        mGoogleApiClient.disconnect();
    }

    /**
     * Handle activity result. Call this method from your Activity's
     * handleApiConnectivity callback. If the activity result pertains to the sign-in
     * process, processes it appropriately.
     */
    public void handleApiConnectivity(int requestCode, int responseCode, Intent intent) {
        if(d_handleApiConnectivity) Log.i(TAG,
                "handleApiConnectivity() RequestCode" +
                String.valueOf(requestCode) +
                "Response=" + GameHelperUtils.activityResponseCodeToString(responseCode));

        if (requestCode != RC_RESOLVE) {
            debugLog("handleApiConnectivity: request code not meant for us. Ignoring.");
            return;
        }

        // no longer expecting a resolution
        mExpectingResolution = false;

        if (!mConnecting) {
            debugLog("handleApiConnectivity: ignoring because we are not connecting.");
            return;
        }

        // We're coming back from an activity that was launched to resolve a
        // connection problem. For example, the sign-in UI.
        if (responseCode == Activity.RESULT_OK) {
            // Ready to try to connect again.
            debugLog("onAR: Resolution was RESULT_OK, so connecting current client again.");
            connect();
        } else if (responseCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED) {
            debugLog("onAR: Resolution was RECONNECT_REQUIRED, so reconnecting.");
            connect();
        } else if (responseCode == Activity.RESULT_CANCELED) {
            // User cancelled.
            debugLog("onAR: Got a cancellation result, so disconnecting.");
            mSignInCancelled = true;
            mConnectOnStart = false;
            mUserInitiatedSignIn = false;
            mSignInFailureReason = null; // cancelling is not a failure!
            mConnecting = false;
            mGoogleApiClient.disconnect();

            // increment # of cancellations
            int prevCancellations = getSignInCancellations();
            int newCancellations = incrementSignInCancellations();
            debugLog("onAR: # of cancellations " + prevCancellations + " --> "
                    + newCancellations + ", max " + mMaxAutoSignInAttempts);

            notifyListener(false);
        } else {
            // Whatever the problem we were trying to solve, it was not
            // solved. So give up and show an error message.
            debugLog("onAR: responseCode="
                    + GameHelperUtils
                    .activityResponseCodeToString(responseCode)
                    + ", so giving up.");
            giveUp(new SignInFailureReason(mConnectionResult.getErrorCode(),
                    responseCode));
        }
    }

    void notifyListener(boolean success) {
        debugLog("Notifying LISTENER of sign-in "
                + (success ? "SUCCESS"
                : mSignInFailureReason != null ? "FAILURE (error)"
                : "FAILURE (no error)"));
        if (mListener != null) {
            if (success) {
                mListener.onSignInSucceeded();
            } else {
                mListener.onSignInFailed();
            }
        }
    }

    /**
     * Starts a user-initiated sign-in flow. This should be called when the user
     * clicks on a "Sign In" button. As a result, authentication/consent dialogs
     * may show up. At the end of the process, the NetworkManagerListener's
     * onSignInSucceeded() or onSignInFailed() methods will be coalled.
     */
    public void beginUserInitiatedSignIn() {
        debugLog("beginUserInitiatedSignIn: resetting attempt count.");
        resetSignInCancellations();
        mSignInCancelled = false;
        mConnectOnStart = true;

        if (mGoogleApiClient.isConnected()) {
            // nothing to do
            logWarn("beginUserInitiatedSignIn() called when already connected. "
                    + "Calling listener directly to notify of success.");
            notifyListener(true);
            return;
        } else if (mConnecting) {
            logWarn("beginUserInitiatedSignIn() called when already connecting. "
                    + "Be patient! You can only call this method after you get an "
                    + "onSignInSucceeded() or onSignInFailed() callback. Suggestion: disable "
                    + "the sign-in button on startup and also when it's clicked, and re-enable "
                    + "when you get the callback.");
            // ignore call (listener will get a callback when the connection
            // process finishes)
            return;
        }

        debugLog("Starting USER-INITIATED sign-in flow.");

        // indicate that user is actively trying to sign in (so we know to
        // resolve
        // connection problems by showing dialogs)
        mUserInitiatedSignIn = true;

        if (mConnectionResult != null) {
            // We have a pending connection result from a previous failure, so
            // start with that.
            debugLog("beginUserInitiatedSignIn: continuing pending sign-in flow.");
            mConnecting = true;
            resolveConnectionResult();
        } else {
            // We don't have a pending connection result, so start anew.
            debugLog("beginUserInitiatedSignIn: starting new sign-in flow.");
            mConnecting = true;
            connect();
        }
    }

    void connect() {
        if (mGoogleApiClient.isConnected()) {
            debugLog("Already connected.");
            return;
        }
        debugLog("Starting connection.");
        mConnecting = true;
        mGoogleApiClient.connect();
    }

    /**
     * Disconnects the API client, then connects again.
     */
    public void reconnectClient() {
        if (!mGoogleApiClient.isConnected()) {
            Log.w(TAG, "reconnectClient() called when client is not connected.");
            // interpret it as a request to connect
            connect();
        } else {
            debugLog("Reconnecting client.");
            mGoogleApiClient.reconnect();
        }
    }

    /** Called when we successfully obtain a connection to a client. */
    @Override
    public void onConnected(Bundle connectionHint) {
        if(d_onConnected) Log.i(TAG, "onConnected() Enter");

        if (connectionHint != null) {
            if(d_onConnected) Log.i(TAG, "onConnected() connection hint provided. Checking for invite.");

            Invitation inv = connectionHint.getParcelable(Multiplayer.EXTRA_INVITATION);

            //Check if we are coming from an invitation invite
            if (inv != null && inv.getInvitationId() != null) {
                // retrieve and cache the invitation ID
                
                if(d_onConnected) Log.i(TAG, "onConnected() connection hint has a room invite!");
                if(d_onConnected) Log.i(TAG, "onConnected() Invitation ID: " + inv.getInvitationId());

                mRealTimeManager.acceptInviteRoom(inv.getInvitationId());
            }

            //No check if we are coming from matches
            else{

                /*// Do we have any requests pending?
                mRequests = Games.Requests.getGameRequestsFromBundle(connectionHint);
                if (!mRequests.isEmpty()) {
                    // We have requests in onConnected's connectionHint.
                    if(d_onConnected) Log.i(TAG, "onConnected() connection hint has " + mRequests.size()
                            + " request(s)");
                }

                if(d_onConnected) Log.i(TAG, "onConnected() connection hint provided. Checking for TBMP game.");
                Log.e(TAG,"got Match onConnect");
                mTurnManager.updateMatchFromOnConnect( (TurnBasedMatch)connectionHint
                        .getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH));*/
            }

        }

        // we're good to go
        succeedSignIn();

        // This is *NOT* required; if you do not register a handler for
        // invitation events, you will get standard notifications instead.
        // Standard notifications may be preferable behavior in many cases.
        Games.Invitations.registerInvitationListener(mGoogleApiClient, this);

        // Likewise, we are registering the optional MatchUpdateListener, which
        // will replace notifications you would get otherwise. You do *NOT* have
        // to register a MatchUpdateListener.
        Games.TurnBasedMultiplayer.registerMatchUpdateListener(mGoogleApiClient, mTurnManager);

    }

    void succeedSignIn() {
        debugLog("succeedSignIn");
        mSignInFailureReason = null;
        mConnectOnStart = true;
        mUserInitiatedSignIn = false;
        mConnecting = false;
        notifyListener(true);
    }

    private final String GAMEHELPER_SHARED_PREFS = "GAMEHELPER_SHARED_PREFS";
    private final String KEY_SIGN_IN_CANCELLATIONS = "KEY_SIGN_IN_CANCELLATIONS";

    // Return the number of times the user has cancelled the sign-in flow in the
    // life of the app
    int getSignInCancellations() {
        SharedPreferences sp = mAppContext.getSharedPreferences(
                GAMEHELPER_SHARED_PREFS, Context.MODE_PRIVATE);
        return sp.getInt(KEY_SIGN_IN_CANCELLATIONS, 0);
    }

    // Increments the counter that indicates how many times the user has
    // cancelled the sign in
    // flow in the life of the application
    int incrementSignInCancellations() {
        int cancellations = getSignInCancellations();
        SharedPreferences.Editor editor = mAppContext.getSharedPreferences(
                GAMEHELPER_SHARED_PREFS, Context.MODE_PRIVATE).edit();
        editor.putInt(KEY_SIGN_IN_CANCELLATIONS, cancellations + 1);
        editor.apply();
        return cancellations + 1;
    }

    // Reset the counter of how many times the user has cancelled the sign-in
    // flow.
    void resetSignInCancellations() {
        SharedPreferences.Editor editor = mAppContext.getSharedPreferences(
                GAMEHELPER_SHARED_PREFS, Context.MODE_PRIVATE).edit();
        editor.putInt(KEY_SIGN_IN_CANCELLATIONS, 0);
        editor.apply();
    }

    /** Handles a connection failure. */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // save connection result for later reference
        debugLog("onConnectionFailed");

        mConnectionResult = result;
        debugLog("Connection failure:");
        debugLog("   - code: "
                + GameHelperUtils.errorCodeToString(mConnectionResult
                .getErrorCode()));
        debugLog("   - resolvable: " + mConnectionResult.hasResolution());
        debugLog("   - details: " + mConnectionResult.toString());

        int cancellations = getSignInCancellations();

        boolean shouldResolve;

        if (mUserInitiatedSignIn) {
            debugLog("onConnectionFailed: WILL resolve because user initiated sign-in.");
            shouldResolve = true;
        } else if (mSignInCancelled) {
            debugLog("onConnectionFailed WILL NOT resolve (user already cancelled once).");
            shouldResolve = false;
        } else if (cancellations < mMaxAutoSignInAttempts) {
            debugLog("onConnectionFailed: WILL resolve because we have below the max# of "
                    + "attempts, "
                    + cancellations
                    + " < "
                    + mMaxAutoSignInAttempts);
            shouldResolve = true;
        } else {
            shouldResolve = false;
            debugLog("onConnectionFailed: Will NOT resolve; not user-initiated and max attempts "
                    + "reached: "
                    + cancellations
                    + " >= "
                    + mMaxAutoSignInAttempts);
        }

        if (!shouldResolve) {
            // Fail and wait for the user to want to sign in.
            debugLog("onConnectionFailed: since we won't resolve, failing now.");
            mConnectionResult = result;
            mConnecting = false;
            notifyListener(false);
            return;
        }

        debugLog("onConnectionFailed: resolving problem...");

        // Resolve the connection result. This usually means showing a dialog or
        // starting an Activity that will allow the user to give the appropriate
        // consents so that sign-in can be successful.
        resolveConnectionResult();
    }

    /**
     * Attempts to resolve a connection failure. This will usually involve
     * starting a UI flow that lets the user give the appropriate consents
     * necessary for sign-in to work.
     */
    void resolveConnectionResult() {
        // Try to resolve the problem
        if (mExpectingResolution) {
            debugLog("We're already expecting the result of a previous resolution.");
            return;
        }

        if (mActivity == null) {
            debugLog("No need to resolve issue, activity does not exist anymore");
            return;
        }

        debugLog("resolveConnectionResult: trying to resolve result: "
                + mConnectionResult);
        if (mConnectionResult.hasResolution()) {
            // This problem can be fixed. So let's try to fix it.
            debugLog("Result has resolution. Starting it.");
            try {
                // launch appropriate UI flow (which might, for example, be the
                // sign-in flow)
                mExpectingResolution = true;
                mConnectionResult.startResolutionForResult(mActivity,
                        RC_RESOLVE);
            } catch (SendIntentException e) {
                // Try connecting again
                debugLog("SendIntentException, so connecting again.");
                connect();
            }
        } else {
            // It's not a problem what we can solve, so give up and show an
            // error.
            debugLog("resolveConnectionResult: result has no resolution. Giving up.");
            giveUp(new SignInFailureReason(mConnectionResult.getErrorCode()));
        }
    }

    public void disconnect() {
        if (mGoogleApiClient.isConnected()) {
            debugLog("Disconnecting client.");
            mGoogleApiClient.disconnect();
        } else {
            Log.w(TAG,
                    "disconnect() called when client was already disconnected.");
        }
    }

    /**
     * Give up on signing in due to an error. Shows the appropriate error
     * message to the user, using a standard error dialog as appropriate to the
     * cause of the error. That dialog will indicate to the user how the problem
     * can be solved (for example, re-enable Google Play Services, upgrade to a
     * new version, etc).
     */
    void giveUp(SignInFailureReason reason) {
        mConnectOnStart = false;
        disconnect();
        mSignInFailureReason = reason;
        if (reason.mActivityResultCode == GamesActivityResultCodes.RESULT_APP_MISCONFIGURED) {
            // print debug info for the developer
            GameHelperUtils.printMisconfiguredDebugInfo(mAppContext);
        }
        showFailureDialog();
        mConnecting = false;
        notifyListener(false);
    }

    /** Called when we are disconnected from the Google API client. */
    @Override
    public void onConnectionSuspended(int cause) {

        //TODO try to reconnect
        debugLog("onConnectionSuspended, cause=" + cause);
        disconnect();
        mSignInFailureReason = null;
        debugLog("Making extraordinary call to onSignInFailed callback");
        mConnecting = false;
        notifyListener(false);
    }

    public void showFailureDialog() {
        if (mSignInFailureReason != null) {
            int errorCode = mSignInFailureReason.getServiceErrorCode();
            int actResp = mSignInFailureReason.getActivityResultCode();

            if (mShowErrorDialogs) {
                showFailureDialog(mActivity, actResp, errorCode);
            } else {
                debugLog("Not showing error dialog because mShowErrorDialogs==false. "
                        + "" + "Error was: " + mSignInFailureReason);
            }
        }
    }

    /** Shows an error dialog that's appropriate for the failure reason. */
    public static void showFailureDialog(Activity activity, int actResp,
                                         int errorCode) {
        if (activity == null) {
            Log.e("NetworkManager", "*** No Activity. Can't show failure dialog!");
            return;
        }
        Dialog errorDialog;

        switch (actResp) {
            case GamesActivityResultCodes.RESULT_APP_MISCONFIGURED:
                errorDialog = makeSimpleDialog(activity, GameHelperUtils.getString(
                        activity, GameHelperUtils.R_APP_MISCONFIGURED));
                break;
            case GamesActivityResultCodes.RESULT_SIGN_IN_FAILED:
                errorDialog = makeSimpleDialog(activity, GameHelperUtils.getString(
                        activity, GameHelperUtils.R_SIGN_IN_FAILED));
                break;
            case GamesActivityResultCodes.RESULT_LICENSE_FAILED:
                errorDialog = makeSimpleDialog(activity, GameHelperUtils.getString(
                        activity, GameHelperUtils.R_LICENSE_FAILED));
                break;
            default:
                // No meaningful Activity response code, so generate default Google
                // Play services dialog
                errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
                        activity, RC_UNUSED, null);
                if (errorDialog == null) {
                    // get fallback dialog
                    Log.e("NetworkManager",
                            "No standard error dialog available. Making fallback dialog.");
                    errorDialog = makeSimpleDialog(
                            activity,
                            GameHelperUtils.getString(activity,
                                    GameHelperUtils.R_UNKNOWN_ERROR)
                                    + " "
                                    + GameHelperUtils.errorCodeToString(errorCode));
                }
        }

        errorDialog.show();
    }

    static Dialog makeSimpleDialog(Activity activity, String text) {
        return (new AlertDialog.Builder(activity)).setMessage(text)
                .setNeutralButton(android.R.string.ok, null).create();
    }

    static Dialog
    makeSimpleDialog(Activity activity, String title, String text) {
        return (new AlertDialog.Builder(activity)).setMessage(text)
                .setTitle(title).setNeutralButton(android.R.string.ok, null)
                .create();
    }

    public Dialog makeSimpleDialog(String text) {
        if (mActivity == null) {
            logError("*** makeSimpleDialog failed: no current Activity!");
            return null;
        }
        return makeSimpleDialog(mActivity, text);
    }

    public Dialog makeSimpleDialog(String title, String text) {
        if (mActivity == null) {
            logError("*** makeSimpleDialog failed: no current Activity!");
            return null;
        }
        return makeSimpleDialog(mActivity, title, text);
    }

    void debugLog(String message) {
        if (mDebugLog) {
            Log.d(TAG, "NetworkManager: " + message);
        }
    }

    void logWarn(String message) {
        Log.w(TAG, "!!! NetworkManager WARNING: " + message);
    }

    void logError(String message) {
        Log.e(TAG, "*** NetworkManager ERROR: " + message);
    }

    // Represents the reason for a sign-in failure
    public static class SignInFailureReason {
        public static final int NO_ACTIVITY_RESULT_CODE = -100;
        int mServiceErrorCode = 0;
        int mActivityResultCode = NO_ACTIVITY_RESULT_CODE;

        public int getServiceErrorCode() {
            return mServiceErrorCode;
        }

        public int getActivityResultCode() {
            return mActivityResultCode;
        }

        public SignInFailureReason(int serviceErrorCode, int activityResultCode) {
            mServiceErrorCode = serviceErrorCode;
            mActivityResultCode = activityResultCode;
        }

        public SignInFailureReason(int serviceErrorCode) {
            this(serviceErrorCode, NO_ACTIVITY_RESULT_CODE);
        }

        @Override
        public String toString() {
            return "SignInFailureReason(serviceErrorCode:"
                    + GameHelperUtils.errorCodeToString(mServiceErrorCode)
                    + ((mActivityResultCode == NO_ACTIVITY_RESULT_CODE) ? ")"
                    : (",activityResultCode:"
                    + GameHelperUtils
                    .activityResponseCodeToString(mActivityResultCode) + ")"));
        }
    }

    /** onInvitation Received Listener  */
    @Override
    public void onInvitationReceived(final Invitation invitation) {
        if(d_onInvitationReceived) Log.i(TAG, "onInvitationReceived() Enter");

        try {

            //Should we pop up the invitation?
            if (!mRealTimeManager.isConnected() && isBound) {

                //for now always pop up
                FragmentTransaction ft = ((FragmentActivity) mActivity).getSupportFragmentManager().beginTransaction();
                inviteDialogFragment = InviteDialogFragment.newInstance(1, new InviteDialogFragment.InviteDialogListener() {
                    @Override
                    public void onDeclinePressed(Invitation inviteResult) {
                        mRealTimeManager.declineInviteRoom(inviteResult.getInvitationId());
                    }

                    @Override
                    public void onJoinPressed(Invitation inviteResult) {
                        mRealTimeManager.acceptInviteRoom(inviteResult.getInvitationId());
                    }
                }, invitation);
                inviteDialogFragment.show(ft, "Dialog");
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "onInvitationReceived: Tried to popup invitation in-game", e );
        }
    }

    @Override
    public void onInvitationRemoved(String s) {
        if(d_onInvitationRemoved) Log.i(TAG, "onInvitationRemoved() Enter");
        if (inviteDialogFragment != null) {
            if (inviteDialogFragment.isVisible()) {
                inviteDialogFragment.dismiss();
                inviteDialogFragment = null;
            }
        }
    }

    /**Intent actions*/
    public  void handleRealTimePlayerInviteResult(Intent data) {
        if (mRealTimeManager != null) {
            mRealTimeManager.handleInvitePlayersToRoom(data);
        }
    }

    public void handleRealTimeReceiveInviteInboxResult(Intent data) {
        if (mRealTimeManager != null) {
            mRealTimeManager.handleInvitationInboxResult(data);
        }
    }

    public void sendChatMessage(String message) {
        mRealTimeManager.broadcastChatMessage(message);
    }

    public boolean isRoomConnected(){
        return mRealTimeManager.isConnected();
    }

    /** get/set */
    public TurnBasedMultiplayerManager getTurnManager() {return mTurnManager;}
    public RealTimeManager getRealTimeManager() {return mRealTimeManager;}

    // Not recommended for general use. This method forces the
    // "connect on start" flag
    // to a given state. This may be useful when using NetworkManager in a
    // non-standard
    // sign-in flow.
/*
    public void setConnectOnStart(boolean connectOnStart) {
        debugLog("Forcing mConnectOnStart=" + connectOnStart);
        mConnectOnStart = connectOnStart;
    }
*/

}
