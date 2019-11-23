package com.bondfire.app.services;


public class AdController {

    private AdControllerListener listener;
    private boolean isShowing;


    public AdController(AdControllerListener listener){
        this.listener = listener;
    }

    public void setAdVisibility(boolean visibility){
        isShowing = visibility;
        listener.setAdVisibility(visibility);
    }
    public void newRequest(){
        listener.newRequest();
    }

    public boolean isShowing(){
        return isShowing;
    }

    public void setTempVisibility(boolean visibility){
        listener.setAdVisibility(visibility);



    }

}
