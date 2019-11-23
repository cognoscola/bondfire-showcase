package com.bondfire.app.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;


import com.bondfire.app.R;
import com.bondfire.app.android.adapter.TurnBasedMatchesAdapter;
import com.bondfire.app.android.adapter.TurnBasedParticipantsAdapter;
import com.bondfire.app.android.data.PurchaseData;
import com.bondfire.app.android.fragment.GamePlayFragment;
import com.bondfire.app.android.network.realtime.RealTimeManager;
import com.bondfire.app.android.network.turnbasedmultiplayer.TurnBasedMultiplayerManager;
import com.bondfire.app.android.utils.billing.IabHelper;
import com.bondfire.app.android.utils.billing.IabResult;
import com.bondfire.app.android.utils.billing.Inventory;
import com.bondfire.app.android.utils.billing.Purchase;
import com.bondfire.app.android.utils.crypt.AesCbcWithIntegrity;
import com.bondfire.app.android.utils.gameservices.AchievementStore;
import com.bondfire.app.android.utils.gameservices.ActionResolver;
import com.bondfire.app.android.utils.social.NetworkManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class GamePlayServiceActivity extends BaseActivity implements NetworkManager.GoogleSignInListener,
        ActionResolver {

    private final static String TAG = GamePlayServiceActivity.class.getName();
    private final static boolean d_leaderboard = true;
    private final static boolean d_loginGPGS = true;
    private final static boolean d_onCreate = true;
    private final static boolean d_onActivityResult = true;
    private final static boolean d_onStart = true;
    private final static boolean d_configureNetwork = true;

//    private final static int GAME_SWIFTY = 0;
    public final static int LEADERBOARD_CODE = 2;

    private NetworkManager networkManager;
    public  NetworkManager getNetworkManager() {return networkManager;}

    public static final String SKU_PAID_VERSION = "bondfire_paid_version";
    IabHelper billingHelper;
    static final int RC_REQUEST = 10001;

    private static AesCbcWithIntegrity.SecretKeys keys;
    PurchaseData purchaseData;

    Gson gson;


    // Listener that's called when we finish querying the items and subscriptions we own
    //TODO Disabled BILLING
/*
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (billingHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                Log.e(TAG,"Failed to query inventory: " + result );
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            */
/*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             *//*

            // Do we have the premium upgrade?
            Purchase premiumPurchase = inventory.getPurchase(SKU_PAID_VERSION);
            purchaseData.isPaidVersion = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            Log.d(TAG, "User is " + (purchaseData.isPaidVersion ? "PAID VERSION" : "FREE VERSION"));
        }
    };
*/


    // Callback for when a purchase is finished
    //TODO disabled Billing
/*
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (billingHelper == null) return;

            if (result.isFailure()) {
//                complain("Error purchasing: " + result);
//                setWaitScreen(false);
                return;
            }

            if (!verifyDeveloperPayload(purchase)) {
//                complain("Error purchasing. Authenticity verification failed.");
//                setWaitScreen(false);
                return;
            }

            Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_PAID_VERSION)) {
                // bought the premium upgrade!
                Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
                Toast.makeText(getApplication(), "Thank you for upgrading! Removing All Adds!", Toast.LENGTH_LONG).show();
                purchaseData.isPaidVersion = true;
                savePurchaseData();
            }
        }
    };
*/

    private boolean verifyDeveloperPayload(Purchase purchase) {
        String payload = purchase.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gson = new Gson();

        //Encrypt our key temporarily
        try {
            keys = AesCbcWithIntegrity.generateKeyFromPassword(
                    Base64.encodeToString(getResources().getString(R.string.cannot_make_purchase).getBytes(), Base64.DEFAULT),
                    Base64.encodeToString(getResources().getString(R.string.purchase_success).getBytes(), Base64.DEFAULT));
            if(d_onCreate) Log.i(TAG, "onCreate() KEY_NAME:" + encryptString("nakedTwister"));

        } catch (GeneralSecurityException e) {
            Log.e(TAG, "onCreate: Security Exception", e);
        }

        //TODO disabled billing
/*        if(!loadPurchaseData())purchaseData = new PurchaseData();

        billingHelper = new IabHelper(this, decryptResource(R.string.license));
        billingHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    Log.i(TAG, "onIabSetupFinished: Problem setting up in-app billing:" + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (billingHelper == null) return;

                List<String> additionalSkuList = new ArrayList<>();
                additionalSkuList.add(SKU_PAID_VERSION);
                billingHelper.queryInventoryAsync(true, additionalSkuList, mGotInventoryListener);
            }
        });*/
    }

    protected void configureNetwork(){

        if(d_onCreate)Log.e(TAG,"onCreate()");
        if (networkManager == null) {
            if(d_configureNetwork) Log.i(TAG, "configureNetwork() Creating Network");
            //Enable both Games and Client Plus
            networkManager = NetworkManager.newInstance(this, NetworkManager.CLIENT_GAMES | NetworkManager.CLIENT_PLUS);
            networkManager.enableDebugLog(true);
            networkManager.setup(this);
        }else{
            if(d_configureNetwork) Log.i(TAG, "configureNetwork() Network already exists ");
            networkManager.setContext(this);
            if (networkManager.isRoomConnected()) {
                if(d_configureNetwork) Log.i(TAG, "configureNetwork() Real Time room connected");
            }
        }
    }

    @Override
    protected void onStart() {
        if(d_onStart) Log.i(TAG, "onStart() ");
        super.onStart();
        networkManager.bindtoService();
        networkManager.onStart(this);
        updateGlobally();
    }

    @Override
    protected void onStop() {
        super.onStop();
        networkManager.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //TODO disabled BILLING
        /*if (billingHelper != null) {
            billingHelper.dispose();
        }*/
        billingHelper = null;
        keys = null;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult() requestCode:" + requestCode + " resultCode:" + resultCode);

        // Returning from the 'Select Match' dialog
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSignInFailed() {

    }

    @Override
    public void onSignInSucceeded() {

    }

    /*** action resolver **/
    @Override
    public boolean getSignedInGPGS() {
        return networkManager.isSignedIn();
    }

    @Override
    public void loginGPGS() {
        if(d_loginGPGS)Log.e(TAG,"LoginGPSGS()");

        try {
            runOnUiThread(new Runnable(){
                public void run() {
                    if(d_loginGPGS)Log.e(TAG,"On UI thread, run Sign in");
                    networkManager.beginUserInitiatedSignIn();
                }
            });
        } catch (final Exception ex) {
            Log.e(TAG,"Unable to log in",ex);
        }
    }

    @Override
    public void logoutGPGS() {
        networkManager.signOut();
    }

    @Override
    public void submitScoreGPGS(String leaderboardId, int score) {
        Games.Leaderboards.submitScore(networkManager.getApiClient(), leaderboardId, score);
    }

    @Override
    public void unlockAchievementGPGS(String achievementId) {
        Games.Achievements.unlock(networkManager.getApiClient(), achievementId);
    }

    @Override
    public void getLeaderboardGPGS(String leaderboardId) {

        if (networkManager.isSignedIn()) {
            if(d_leaderboard) Log.e(TAG,"getLeaderGPSGS() Signed in, getting board");
            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(networkManager.getApiClient(), leaderboardId), LEADERBOARD_CODE);
        }
        else if (!networkManager.isConnecting()) {
            if(d_leaderboard) Log.e(TAG,"Not Signed in, logging in..");
            loginGPGS();
        } else{
            if(d_leaderboard) Log.e(TAG,"Not Signed in or reconnecting");
        }
    }

    @Override
    public void getAchievementsGPGS() {
        if (networkManager.isSignedIn()) {
            startActivityForResult(Games.Achievements.getAchievementsIntent(networkManager.getApiClient()), 101);
        }
        else if (!networkManager.isConnecting()) {
            loginGPGS();
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e(TAG,"On New intent");
    }

    public void updateGlobally(){
        if(getSignedInGPGS()){
            AchievementStore.pushGlobally(this);
        }
    }

    public static void clean(){

        TurnBasedMultiplayerManager.cleanGameInformation();
    }

    /************* TURN BASED MULTIPLAYER GAME ************************/

    public void  openMatches() {
        if (networkManager.isSignedIn()) {
            networkManager.getTurnManager().onCheckGamesClicked();
        }
        else if (!networkManager.isConnecting()) {
            loginGPGS();
        }
    }


    public void onFindPlayersClicked(int gameId,int min,int max){
        try{
            if(networkManager.isSignedIn()){
                networkManager.getTurnManager().onFindPlayersClicked(gameId,min,max);
            }else{
                loginGPGS();
            }
        }catch (NullPointerException e){
            Log.e(TAG,"onDoneClicked()",e);
        }
    }

    public void onQuickMatch(int gameId){
        try{
            if(networkManager.isSignedIn()){
                networkManager.getTurnManager().onQuickMatchClicked(gameId);
            }else{
                loginGPGS();
            }

        }catch (NullPointerException e){
            Log.e(TAG,"onDoneClicked()",e);
        }
    }

    public void onCancelClicked(){
        try{
            if(networkManager.isSignedIn()){
                networkManager.getTurnManager().onCancelClicked();
            }else{
                Log.e(TAG, "onCancelClicked: Not Signed In!");
            }
        }catch (NullPointerException e){
            Log.e(TAG,"onDoneClicked()",e);
        }
    }

    public void onLeaveClicked(){
        try{
            if(networkManager.isSignedIn()){
                networkManager.getTurnManager().onLeaveClicked();
            }else{
                Log.e(TAG,"onLeaveClicked: Not Signed in!");
            }
        }catch (NullPointerException e){
            Log.e(TAG,"onDoneClicked()",e);
        }
    }

    public void onFinishedClicked(){
        try{
            if(networkManager.isSignedIn()){
                networkManager.getTurnManager().onFinishClicked();
            }else{
                Log.e(TAG, "onFinishedClicked: Not Signed in!");
            }

        }catch (NullPointerException e){
            Log.e(TAG,"onDoneClicked()",e);
        }
    }

    public void onDoneClicked(String data){
        try{
            if(networkManager.isSignedIn()){
                networkManager.getTurnManager().onDoneClicked(data);
            }else{
                Log.e(TAG,"onDoneClicked: Not Signed in!");
            }

        }catch (NullPointerException e){
            Log.e(TAG,"onDoneClicked()",e);
        }
    }

    public void injectUi(TurnBasedParticipantsAdapter adapter){

       try{
           networkManager.getTurnManager().injectParticipantAdapter(adapter);
       }catch (NullPointerException e){
           Log.e(TAG,"injectUI()",e);
       }
    }

    public void injectUi(TurnBasedMatchesAdapter adapter, int gameId){
        try{
            networkManager.getTurnManager().injectMatchesAdapter(adapter, gameId);
        }catch (NullPointerException e){
            Log.e(TAG,"injectUI()",e);
        }
    }

    /** REAL TIME MULTIPLAYER UI **/
    public void invitePlayersToRoom(){
        try{
            if(networkManager.isSignedIn()){
                Intent intent = Games.RealTimeMultiplayer.
                        getSelectOpponentsIntent(networkManager.getApiClient(), 1, 7);
                startActivityForResult(intent, RealTimeManager.RC_SELECT_PLAYERS);
            }else{
                loginGPGS();
            }
        }catch (NullPointerException e){
            Log.e(TAG,"onDoneClicked()",e);
        }
    }

    /** launch intent to see invitations **/
    public void seeInvitations(){
        try{
            if(networkManager.isSignedIn()){
                Intent intent = Games.Invitations
                        .getInvitationInboxIntent(networkManager.getApiClient());
                startActivityForResult(intent, RealTimeManager.RC_INVITATION_INBOX);
            }else{
                loginGPGS();
            }
        }catch (NullPointerException e){
            Log.e(TAG,"onDoneClicked()",e);
        }
    }

    /** launch intent to see invitations **/
    public void automatch(){
        try{
            if(networkManager.isSignedIn()){
                getNetworkManager().getRealTimeManager().automatch();
            }else{
                loginGPGS();
            }
        }catch (NullPointerException e){
            Log.e(TAG,"onDoneClicked()",e);
        }
    }


    public void sendChat(String message) {
        try{
            if(networkManager.isSignedIn()){
                networkManager.sendChatMessage(message);
            }else{
                loginGPGS();
            }
        }catch (NullPointerException e){
            Log.e(TAG,"onDoneClicked()",e);
        }
    }

    public  void submitEvent(String eventId, int count){
        try {
            if(networkManager.isSignedIn()){
                if (!eventId.isEmpty()) {
                    GoogleApiClient apiClient = networkManager.getApiClient();
                    if (apiClient != null) {
                        Games.Events.increment(apiClient,eventId,count);
                    }
                }
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "submitEvent: tried to log EVENT", e );
        }
    }

    // User clicked the "Upgrade to Premium" button.
    public void onPurchaseClicked(String SKU) {
        Log.d(TAG, "Upgrade button clicked; launching purchase flow for upgrade.");

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */

        //TODO DISABLED
        /*String payload = "";
        billingHelper.launchPurchaseFlow(this, SKU, RC_REQUEST,
                mPurchaseFinishedListener, payload);*/
    }

    public boolean savePurchaseData() {

        //load up preferences
        SharedPreferences preferences = getSharedPreferences(decryptResource(R.string.pur_name), MODE_PRIVATE);
        if (preferences == null) return false;
        if (purchaseData == null) return false;
        preferences.edit().putString(
                decryptResource(R.string.pur_key),
                encryptString(gson.toJson(purchaseData))
        ).apply();

        return true;
    }

    /**
     * Load purchase data from internal memory
     * @return true if the load was successful
     */
    public boolean loadPurchaseData() {

        //load up preferences
        try {
            SharedPreferences preferences = getSharedPreferences(decryptResource(R.string.pur_name), MODE_PRIVATE);
            if (preferences == null) return false;
            String purchaseDataString = preferences.getString(decryptResource(R.string.pur_key), "");
            if (purchaseDataString.isEmpty()) return false;
            purchaseData = gson.fromJson(decryptString(purchaseDataString), PurchaseData.class);
            if (purchaseData == null) return false;
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "loadPurchaseData: was trying to load purchase data",  e);
            return false;
        }
        return true;
    }

    private String decryptResource(int resourceId) {
           return decryptString(getResources().getString(resourceId));
    }

    public static String decryptString(String cipher) {

        try {
            return AesCbcWithIntegrity
                    .decryptString(new AesCbcWithIntegrity.CipherTextIvMac(cipher), keys);

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "decryptString: tried to get preference", e);
            return "";
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "decryptString: tried to get prefers", e);
            return "";
        } catch (NullPointerException e) {
            Log.e(TAG, "decryptString: Was trying to perform security stuff  ", e);
            return "";
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "decryptString: ",e );
            return "";
        }
    }

    public static  String encryptString(String plainText){
        try{
            return AesCbcWithIntegrity.encrypt(plainText, keys).toString();
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "encryptString: tried to get preference", e);
            return "";
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "encryptString: tried to get prefers", e);
            return "";
        } catch (NullPointerException e) {
            Log.e(TAG, "encryptString: Was trying to perform security stuff  ", e);
            return "";
        }
    }
}
