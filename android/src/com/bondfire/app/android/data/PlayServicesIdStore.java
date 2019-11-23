package com.bondfire.app.android.data;

import android.content.Context;
import com.bondfire.app.R;
import com.bondfire.app.android.activity.GamePlayServiceActivity;

public class PlayServicesIdStore {

    private final static int SWIFTY_GLIDER = 0;

    public static String[] getLeaderBoards(int id, Context context){

        switch (id){
            case SWIFTY_GLIDER:
                return  new String[] {
                        GamePlayServiceActivity.decryptString(context.getResources().getString(R.string.leaderboard_glider_scores))
                        } ;
            default: return new String[] {};
        }
    }

    public static String[] getAchievements(int id, Context context){

        switch (id){
            case SWIFTY_GLIDER:
                return   new String[]{
                        GamePlayServiceActivity.decryptString(context.getResources().getString(R.string.achievement_pro_glider)),
                        GamePlayServiceActivity.decryptString(context.getResources().getString(R.string.achievement_sky_diver)),
                        GamePlayServiceActivity.decryptString(context.getResources().getString(R.string.achievement_persistent_glider)),
                        GamePlayServiceActivity.decryptString(context.getResources().getString(R.string.achievement_master_glider)),
                        GamePlayServiceActivity.decryptString(context.getResources().getString(R.string.achievement_grand_master_glider)),
                } ;

            default: return new String[] {};
        }
    }

    public static String[] getEvents(int id, Context context) {
        switch (id) {
            case SWIFTY_GLIDER:

                String[] ids = context.getResources().getStringArray(R.array.events_swifty_glider);
                for (int i = 0; i < ids.length; i++) {
                    ids[i] = GamePlayServiceActivity.decryptString(ids[i]);
                }
                return ids;
            default: return new String[] {};
        }
    }
}
