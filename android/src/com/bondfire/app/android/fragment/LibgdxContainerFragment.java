package com.bondfire.app.android.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.bondfire.app.Main;
import com.bondfire.app.android.activity.MainActivity;
import com.bondfire.app.android.activity.GamePlayServiceActivity;
import com.bondfire.app.android.data.GameInformation;
import com.bondfire.app.android.data.PlayServicesIdStore;
import com.bondfire.app.android.utils.gameservices.AchievementStore;
import com.bondfire.app.bfUtils.BondfireGraphicsModifier;
import com.bondfire.app.bfUtils.BondfireServicesAndControllers;
import com.bondfire.app.callbacks.PlatformInterfaceController;
import com.bondfire.app.services.AdController;
import com.bondfire.app.services.AdControllerListener;
import com.bondfire.app.services.PlayServicesListener;
import com.bondfire.app.services.PlayServicesObject;
import com.bondfire.app.services.RealTimeMultiplayerService;
import com.bondfire.app.services.ServiceUtils;
import com.bondfire.app.services.TurnBasedMultiplayerService;
import com.bondfire.dex.Dex;
import com.bondfire.linespark.LineSpark;
import com.bondfire.swiftyglider.SwiftyGlider;
import com.google.android.gms.ads.AdRequest;

import java.util.Calendar;

public class LibgdxContainerFragment
        extends AndroidFragmentApplication
        implements PlatformInterfaceController,
        BondfireGraphicsModifier {

    private final static String TAG = LibgdxContainerFragment.class.getName();
    private final static boolean d_newInstance = false;
    private final static boolean d_createServices = false;
    private final static boolean d_createGame = false;
    private final static boolean d_onresume = false;
    private final static boolean d_onPause = false;
    private final static boolean d_getService = true;
    private final static boolean d_submitEvent = true;

    View rootView;
    ApplicationAdapter game;
    private PlayServicesObject playServicesObject;
    private AdController controller;
    private  TurnBasedMultiplayerService turnBasedMultiplayerService;
    private static RealTimeMultiplayerService realTimeMultiplayerService;
    Calendar cal;

    public GameInformation getGameInfo() {
        return gameInfo;
    }
    private GameInformation gameInfo;

    private Activity activity;

    public LibgdxContainerFragment() {
    }

    public static LibgdxContainerFragment newInstance(GameInformation information){
        if(d_newInstance)Log.e(TAG,"newInstance Libgdx()");
        LibgdxContainerFragment fragment = new LibgdxContainerFragment();
        fragment.gameInfo = information;
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public void startActivity(Intent intent) {
        createServices();
        createGame();
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(rootView == null) GenerateBrokenGameScreen();
        return rootView;
    }

    public void getBitmapBackground(Main.PixmapListener listener)  {
//        Main.getScreenshot(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true, listener);
    }

    @Override
    public void onPause() {
        if(d_onPause)Log.e(TAG,"onPause()");
        super.onPause();
        if(controller != null){
            if(controller.isShowing()){
                controller.setTempVisibility(false);
                ((MainActivity)activity).getmAdview().destroy();
            }
        }
    }



    @Override
    public void onResume() {
        if(d_onresume)Log.e(TAG,"onResume()");
        super.onResume();

        try {
            if(controller != null){
                if(controller.isShowing()){
                    controller.setTempVisibility(true);
                    ((MainActivity)activity).getmAdview().loadAd(new AdRequest.Builder().build());
                }
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "onResume: Try to get adView, but it was null",e );
        }
    }

    private void createServices(){

        if(d_createServices)Log.e(TAG,"createServices() Enter");

        if(gameInfo.usesLeaderBoardServices) {

            if (d_createServices) Log.e(TAG, "Uses Leader Board");
            playServicesObject = new PlayServicesObject(
                    PlayServicesIdStore.getLeaderBoards(gameInfo.gameId, activity),
                    PlayServicesIdStore.getAchievements(gameInfo.gameId, activity),
                    PlayServicesIdStore.getEvents(gameInfo.gameId, activity),
                    new PlayServicesListener() {
                        @Override
                        public void Unlock(final String id) {
                            (activity).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (((GamePlayServiceActivity) activity).getSignedInGPGS()) {
                                        ((GamePlayServiceActivity) activity).unlockAchievementGPGS(id);
                                    } else {
                                        Toast.makeText(activity, "Achievement Unlocked", Toast.LENGTH_SHORT).show();
                                        /** save the progress locally */
                                        AchievementStore.updateLocally(activity, 0, id);
                                    }
                                }
                            });
                        }

                        @Override
                        public void SetScore(final String[] leaderBoard, final int score) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (((GamePlayServiceActivity) activity).getSignedInGPGS()) {
                                        ((GamePlayServiceActivity) activity).submitScoreGPGS(leaderBoard[0], score);
                                    }
                                }
                            });
                        }

                        @Override
                        public void submitEvent(String eventId, int incrementAmount) {
//                            if(d_submitEvent) Log.i(TAG, "submitEvent() " + eventId + " " +incrementAmount);
                            ((GamePlayServiceActivity)activity).submitEvent(eventId, incrementAmount);
                        }
                    });
        }

        if(gameInfo.usesAdvertisementServices){
            if(d_createServices)Log.e(TAG,"Uses Adds");
//            * create a controller to pass to the game as well
            controller =  new AdController(new AdControllerListener() {
                @Override
                public void setAdVisibility(final boolean visibility) {
                    try{
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    ((MainActivity) activity).getmAdview().setVisibility(visibility ? View.VISIBLE : View.INVISIBLE);
                                } catch (NullPointerException e) {
                                    Log.e(TAG, "run: tried to use the adview",e );
                                }
                            }
                        });

                    }catch (NullPointerException e){
                        Log.e("Libgdx","Activiy is Destroyed");
                    }
                }

                @Override
                public void newRequest() {
                    try {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((MainActivity)activity).getmAdview().destroy();
                                AdRequest adRequest = new AdRequest.Builder().build();
                                ((MainActivity)activity).getmAdview().loadAd(adRequest);
                            }
                        });
                    } catch (NullPointerException e) {
                        Log.e("Libgdx", "Activiy is Destroyed");
                    }
                }
            });
        }

        if(gameInfo.usesTurnBasedMultiplayerService){
            if(d_createServices)Log.e(TAG," Uses TBMP");
            turnBasedMultiplayerService =
                    new TurnBasedMultiplayerService(((GamePlayServiceActivity)activity).getNetworkManager().getTurnManager().generateListener());
        }

        if (gameInfo.usesRealTimeMultiplayerServices) {
            if(d_createServices) Log.i(TAG, "createServices() Uses RTMP");
            realTimeMultiplayerService = RealTimeMultiplayerService.newInstance();
            realTimeMultiplayerService.setSender(((GamePlayServiceActivity)activity)
                    .getNetworkManager().getRealTimeManager());
        }

        if(gameInfo.usesDayTimer){
            if(d_createServices)Log.e(TAG,"USes Timer");
            cal = Calendar.getInstance();
        }
    }

    //If the game doesn't launch correctly, display a screen which shows that the
    //game wasn't able to launch correclty
    private void GenerateBrokenGameScreen(){
        LinearLayout parent = new LinearLayout(activity);
        parent.setBackground(new ColorDrawable(Color.parseColor("#FF0000")));
        rootView = parent;
    }

    private void createGame(){
        if(d_createGame)Log.e(TAG,"createGame() ENter with ID " +gameInfo.gameId );

        switch(gameInfo.gameId) {
            case 0:
                game = new SwiftyGlider(cal.get(Calendar.HOUR_OF_DAY) * 60 * 60 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND));
                ((BondfireServicesAndControllers) game).setPlayServicesResources(playServicesObject);
                ((BondfireServicesAndControllers) game).setAdController(controller);
                ((BondfireServicesAndControllers) game).setPlatformController(this);
                rootView = initializeForView(game);
                break;
            case 1:
                game = Dex.newInstance(0);
                ((BondfireServicesAndControllers) game).setPlatformController(this);
                rootView = initializeForView(game);
                break;

            case 2:
                game = new LineSpark(cal.get(Calendar.HOUR_OF_DAY) * 60 * 60 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND),
                        new PlatformInterfaceController() {
                            @Override
                            public void onCreate() {
                                if (d_createGame) Log.e(TAG, "Injecting TBMP Services");
                                ((LineSpark) game).injectTurnBasedMultiplayerListener(turnBasedMultiplayerService);
                            }

                            @Override
                            public void SendToast(String message) {
                                MakeToast(message);
                            }

                            @Override
                            public void ShowMatches() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((MainActivity) activity).setPageView(2);
                                    }
                                });
                            }

                            @Override
                            public void setInformation(final String title, final String content, final boolean show) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        GameInstructionFragment instructionFragment = ((MainActivity) activity).getmSectionsPagerAdapter().getInstructionsFragment();
                                        instructionFragment.setInformation(title, content);
                                        if (show) {
                                            ((MainActivity) activity).setPageView(0);
                                        }
                                    }
                                });
                            }

                            @Override
                            public void getService(int service) {

                            }

                            @Override
                            public void keepScreenOn() {

                            }

                            @Override
                            public void stopKeepingScreenOn() {

                            }
                        }
                );
                rootView = initializeForView(game);
                break;
        }
    }

    public void MakeToast(final String message) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        turnBasedMultiplayerService = null;
        realTimeMultiplayerService = null;
        controller = null;
        playServicesObject = null;
    }


    /*public void onDestroy() {


    }*/


    @Override
    public void setGraphicsEffectPercent(float effectPerfect) {
        if(game != null) {
            ((BondfireGraphicsModifier) game).setGraphicsEffectPercent(effectPerfect);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


    }


    //Parent Interface control
    @Override
    public void onCreate() {
        //TODO
    }

    @Override
    public void SendToast(String message) {
        MakeToast(message);
    }

    @Override
    public void ShowMatches() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((MainActivity) activity).setPageView(2);
            }
        });
    }

    @Override
    public void setInformation(final String title, final String content, final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GameInstructionFragment instructionFragment = ((MainActivity) activity).getmSectionsPagerAdapter().getInstructionsFragment();
                instructionFragment.setInformation(title,content);
                if (show) {
                    ((MainActivity) activity).setPageView(0);
                }
            }
        });
    }

    @Override
    public void getService(final int service) {
        switch (service) {
            case ServiceUtils.REAL_TIME_SERVICE:
                if (d_getService) Log.i(TAG, "getService() REAL_TIME_SERVICE");
                if (realTimeMultiplayerService == null) {
                    if (d_getService)
                        Log.i(TAG, "getService() service is null, probably because we " +
                                "are trying to access it from a seperate thread");
                } else {
                    ((BondfireServicesAndControllers) game).setRealTimeServices(realTimeMultiplayerService);
                }
                break;
        }
    }

    @Override
    public void keepScreenOn() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });

    }

    @Override
    public void stopKeepingScreenOn() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });
    }
}
