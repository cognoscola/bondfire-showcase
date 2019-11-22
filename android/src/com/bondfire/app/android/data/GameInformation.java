package com.bondfire.app.android.data;

/** Contains informatino regarding a particular game*/
/** this class is created and populated when we select from the game list */
/** we pass this class around the different fragments (such as the GamePlayFragment
 *  libgdx fragment and GameDescriptionFragment. they use this information to do their thing
 */
public class GameInformation {

    public GameInformation(String title, String leaderBoardId, int iconId, int gameId,int min,int max){
        this.leaderBoardId = leaderBoardId;
        this.title = title;
        this.iconId = iconId;
        this.gameId = gameId;
        this.min = min;
        this.max = max;
    }

    public String title;
    public String leaderBoardId;
    public int iconId;
    public int gameId;
    public int min;
    public int max;

    //services
    public boolean usesTurnBasedMultiplayerService;
    public boolean usesAdvertisementServices;
    public boolean usesLeaderBoardServices;
    public boolean usesRealTimeMultiplayerServices;

    //miscallanious
    public boolean usesDayTimer;

    /**
     * @return the range of players this game can take as a string
     */
    public String getPlayerCountString() {
        return String.valueOf(min) + "-" + String.valueOf(max) + " Players";
    }
}
