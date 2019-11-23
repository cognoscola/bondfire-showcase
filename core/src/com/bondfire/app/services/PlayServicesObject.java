package com.bondfire.app.services;

/** This object is the transport of the Play Services Items
 * such as leaderboards and achievements into the
 * libgdx classes
 */

public class PlayServicesObject {

    private String[] leaderBoards;
    private String[] achievementIds;
    private String[] events;
    private PlayServicesListener listener;

    public PlayServicesObject(
            String[] leaderboard,
            String[] ids,
            String[] events,
            PlayServicesListener listener){
        this.achievementIds = ids;
        this.listener = listener;
        this.leaderBoards = leaderboard;
        this.events = events;
    }

    public void submitEvent(int position, int incrementAount){listener.submitEvent(events[position],incrementAount);    }

    public void Unlock(int position){
        listener.Unlock(achievementIds[position]);
    }

    public void Score(int score) {
        System.out.println("ENTER SCORE");
        listener.SetScore(leaderBoards, score);
    }

}
