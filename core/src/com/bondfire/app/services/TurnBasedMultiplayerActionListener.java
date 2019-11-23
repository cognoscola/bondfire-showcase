package com.bondfire.app.services;


public interface TurnBasedMultiplayerActionListener {

    void onCancelMatch();  //usr cancelled the match for everyone
    void onLeaveMatch();   //user left the match
    void onTakeTurn(String data);     //the user is done his turn
    void onFinishMatch();  //Indicate that the match is finished;

    void onFindPeopleToMatchWith(int gameId, int min, int max); //find someone to match with
    void onInbox();                 //Check inbox
    void onAutoMatch(int gameId);             //find automatch

    void injectReceiverListener(TurnBasedMultiplayerReceiverListener listener);


}
