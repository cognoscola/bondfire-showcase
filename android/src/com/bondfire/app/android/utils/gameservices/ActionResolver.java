package com.bondfire.app.android.utils.gameservices;

public interface ActionResolver {

    public boolean getSignedInGPGS();
    public void loginGPGS();
    public void logoutGPGS();
    public void submitScoreGPGS(String leaderboardId, int score);
    public void unlockAchievementGPGS(String achievementId);
    public void getLeaderboardGPGS(String leaderboardId);
    public void getAchievementsGPGS();
}