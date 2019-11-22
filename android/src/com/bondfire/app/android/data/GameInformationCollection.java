package com.bondfire.app.android.data;


public class GameInformationCollection {

    private String[] titles;
    private String[] leaderBoardId;
    private int   [] resIds;
    private int   [] gameIds;
    private int   [] minPlayerCount;


    private int   [] maxPlayerCount;

    private boolean [] usesLeaderBoardServices;
    private boolean [] usesAdvertisementServices;
    private boolean [] usesTurnBasedMultiPlayerServices;
    private boolean [] usesRealTimeMultiplayerServices;


    private boolean [] usesDayTimer;

    public GameInformationCollection(
            String[] titles,
            String[] leaderBoardId,
            int[] resIds,
            int[] gameIds,
            int[] minPlayers,
            int[] maxPlayers){
        this.titles = titles;
        this.leaderBoardId = leaderBoardId;
        this.resIds =resIds;
        this.gameIds = gameIds;
        this.minPlayerCount = minPlayers;
        this.maxPlayerCount = maxPlayers;
    }

    public String getTitle(int position){ return titles[position]; }

    public String getLeaderboardId(int posittion){return leaderBoardId[posittion];}

    public int getIcon(int position){ return resIds[position];}

    public int getgameId(int position){return gameIds[position];}

    public int getMaxPlayerCount(int position) {
        return maxPlayerCount[position];
    }

    public int getMinPlayerCount(int position) {
        return minPlayerCount[position];
    }

    public int getSize(){
        return titles.length;
    }

    public boolean getUsesLeaderBoardServices(int position) {
        return usesLeaderBoardServices[position];
    }

    public boolean getUsesAdvertisementServices(int position) {
        return usesAdvertisementServices[position];
    }

    public boolean getUsesTurnBasedMultiPlayerServices(int position) {
        return usesTurnBasedMultiPlayerServices[position];
    }

    public boolean getUsesRealTimeMultiplayerServices(int position) {
        return usesRealTimeMultiplayerServices[position];
    }
    public boolean getUsesDayTimer(int position) {
        return usesDayTimer[position];
    }

    public void setUsesTurnBasedMultiPlayerServices(boolean[] usesTurnBasedMultiPlayerServices) {
        this.usesTurnBasedMultiPlayerServices = usesTurnBasedMultiPlayerServices;
    }

    public void setUsesAdvertisementServices(boolean[] usesAdvertisementServices) {
        this.usesAdvertisementServices = usesAdvertisementServices;
    }

    public void setUsesLeaderBoardServices(boolean[] usesLeaderBoardServices) {
        this.usesLeaderBoardServices = usesLeaderBoardServices;
    }


    public void setUsesDayTimer(boolean[] usesDayTimer) {
        this.usesDayTimer = usesDayTimer;
    }


    public void setUsesRealTimeMultiplayerServices(boolean[] usesRealTimeMultiplayerServices) {
        this.usesRealTimeMultiplayerServices = usesRealTimeMultiplayerServices;
    }



}
