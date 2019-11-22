package com.bondfire.app.android.utils.gameservices;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.badlogic.gdx.Gdx;
import com.bondfire.app.android.activity.GamePlayServiceActivity;
import com.google.gson.Gson;

import java.util.ArrayList;

public class AchievementStore {

    private final static String Tag = "AchievementStore";

    public final static String PREF_ACHIEVEMENTS = "ACHIEVEMENTS";
    public final static String KEY_ACHIEVEMENT = "KEYACHIEVENT";

    public final static int NUM_GAMEs = 1;

    private static ArrayList<AchievementGame> gameList;

    private static void LoadLocally(Context context){

        Gson gson = new Gson();
        SharedPreferences prefs = context.getSharedPreferences(PREF_ACHIEVEMENTS, Activity.MODE_PRIVATE);
        String data = prefs.getString(KEY_ACHIEVEMENT,"");
        if(data.isEmpty()){
            gameList = new ArrayList<>();
            for(int i =0 ;i < NUM_GAMEs;i++){
                gameList.add(new AchievementGame());
            }
        }else{
            gameList = gson.fromJson(data, ArrayList.class);
        }
    }

    private static void SaveLocally(Context context){

        Gson gson = new Gson();
        String data = gson.toJson(gameList);
        SharedPreferences prefs = context.getSharedPreferences(PREF_ACHIEVEMENTS, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ACHIEVEMENT,data);
        editor.apply();
    }

    public static void updateLocally(Context context, int game, String id){

        try {

            if (gameList == null) {
                LoadLocally(context);
            }
            gameList.get(game).addId(id);
            SaveLocally(context);

        }catch (IndexOutOfBoundsException e){
            Gdx.app.log(Tag,"Index out of bound exception",e);
        }
    }

    public static void pushGlobally(Context context){

        try {
            String id;
            if(gameList == null){
                LoadLocally(context);
            }

            for(int i = 0; i < gameList.size(); i++){
                for(int j = 0; j< gameList.get(i).list.size(); j++){
                    id =  gameList.get(i).getNextId();
                    if(!id.equals( "-1"))
                        ((GamePlayServiceActivity)context).unlockAchievementGPGS(id);
                }
            }
        }
        catch (NullPointerException e){
            Gdx.app.log(Tag,"Unable to Load Achievement score",e);
        }


    }
}
