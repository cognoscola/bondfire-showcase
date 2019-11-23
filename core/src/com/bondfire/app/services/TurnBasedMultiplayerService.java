package com.bondfire.app.services;

public class TurnBasedMultiplayerService {

    private final static String Tag = TurnBasedMultiplayerService.class.getName();
    private final static boolean d_injectReceiver = true;

    private TurnBasedMultiplayerActionListener multiplayerActionListener;
    private TurnBasedMultiplayerReceiverListener multiplayerReceiverListener;

    public TurnBasedMultiplayerService(TurnBasedMultiplayerActionListener listener){
        this.multiplayerActionListener = listener;
    }

    //usr cancelled the match for everyone
    public void onCancelMatch(){
        multiplayerActionListener.onCancelMatch();
    }

    //user left the match
    public void onLeaveMatch(){
        multiplayerActionListener.onLeaveMatch();
    }

    //the user is done his turn
    public void onTakeTurn(String data){
        multiplayerActionListener.onTakeTurn(data);
    }

    //Indicate that the match is finished;
    public void onFinishMatch(){
        multiplayerActionListener.onFinishMatch();
    }

    //find someone to match with
    public void onFindPeopleToMatchWith(){
//        multiplayerActionListener.onFindPeopleToMatchWith(-1);
    }
    //Check inbox
    public void onInbox(){
        multiplayerActionListener.onInbox();
    }

    //find automatch
    public void onAutoMatch(){
        multiplayerActionListener.onAutoMatch(-1);
    }

    public void injectReceiver(TurnBasedMultiplayerReceiverListener receiver){
        if(d_injectReceiver)System.out.println("inject Receiver Called!");

        this.multiplayerReceiverListener = receiver;
        multiplayerActionListener.injectReceiverListener(receiver);
    }

}
