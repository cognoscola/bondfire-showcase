package com.bondfire.app.android.fragment;

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

    public LibgdxContainerFragment() {
    }

    public static LibgdxContainerFragment newInstance(GameInformation information){
        if(d_newInstance)Log.e(TAG,"newInstance Libgdx()");
        LibgdxContainerFragment fragment = new LibgdxContainerFragment();
        fragment.gameInfo = information;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        createServices();
        createGame();
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
                ((MainActivity)getActivity()).getmAdview().destroy();
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
                    ((MainActivity)getActivity()).getmAdview().loadAd(new AdRequest.Builder().build());
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
                    PlayServicesIdStore.getLeaderBoards(gameInfo.gameId, getActivity()),
                    PlayServicesIdStore.getAchievements(gameInfo.gameId, getActivity()),
                    PlayServicesIdStore.getEvents(gameInfo.gameId, getActivity()),
                    new PlayServicesListener() {
                        @Override
                        public void Unlock(final String id) {
                            (getActivity()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (((GamePlayServiceActivity) getActivity()).getSignedInGPGS()) {
                                        ((GamePlayServiceActivity) getActivity()).unlockAchievementGPGS(id);
                                    } else {
                                        Toast.makeText(getActivity(), "Achievement Unlocked", Toast.LENGTH_SHORT).show();
                                        /** save the progress locally */
                                        AchievementStore.updateLocally(getActivity(), 0, id);
                                    }
                                }
                            });
                        }

                        @Override
                        public void SetScore(final String[] leaderBoard, final int score) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (((GamePlayServiceActivity) getActivity()).getSignedInGPGS()) {
                                        ((GamePlayServiceActivity) getActivity()).submitScoreGPGS(leaderBoard[0], score);
                                    }
                                }
                            });
                        }

                        @Override
                        public void submitEvent(String eventId, int incrementAmount) {
//                            if(d_submitEvent) Log.i(TAG, "submitEvent() " + eventId + " " +incrementAmount);
                            ((GamePlayServiceActivity)getActivity()).submitEvent(eventId, incrementAmount);
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
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    ((MainActivity) getActivity()).getmAdview().setVisibility(visibility ? View.VISIBLE : View.INVISIBLE);
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
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((MainActivity)getActivity()).getmAdview().destroy();
                                AdRequest adRequest = new AdRequest.Builder().build();
                                ((MainActivity)getActivity()).getmAdview().loadAd(adRequest);
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
                    new TurnBasedMultiplayerService(((GamePlayServiceActivity)getActivity()).getNetworkManager().getTurnManager().generateListener());
        }

        if (gameInfo.usesRealTimeMultiplayerServices) {
            if(d_createServices) Log.i(TAG, "createServices() Uses RTMP");
            realTimeMultiplayerService = RealTimeMultiplayerService.newInstance();
            realTimeMultiplayerService.setSender(((GamePlayServiceActivity)getActivity())
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
        LinearLayout parent = new LinearLayout(getActivity());
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
                                        ((MainActivity) getActivity()).setPageView(2);
                                    }
                                });
                            }

                            @Override
                            public void setInformation(final String title, final String content, final boolean show) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        GameInstructionFragment instructionFragment = ((MainActivity) getActivity()).getmSectionsPagerAdapter().getInstructionsFragment();
                                        instructionFragment.setInformation(title, content);
                                        if (show) {
                                            ((MainActivity) getActivity()).setPageView(0);
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
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        turnBasedMultiplayerService = null;
        realTimeMultiplayerService = null;
        controller = null;
        playServicesObject = null;
    }


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
                ((MainActivity) getActivity()).setPageView(2);
            }
        });
    }

    @Override
    public void setInformation(final String title, final String content, final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GameInstructionFragment instructionFragment = ((MainActivity) getActivity()).getmSectionsPagerAdapter().getInstructionsFragment();
                instructionFragment.setInformation(title,content);
                if (show) {
                    ((MainActivity) getActivity()).setPageView(0);
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
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });

    }

    @Override
    public void stopKeepingScreenOn() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });
    }
}
