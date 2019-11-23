package com.bondfire.app.services;

public interface PlayServicesListener {

    void Unlock(String id);

    void SetScore(String[] leaderBoardId, int score);

    void submitEvent(String eventId, int incrementAmount);
}
