package com.bondfire.app.callbacks;

/**
 * Allows Games to be able to control the android or iOS interface
 */
public interface PlatformInterfaceController {

    void onCreate();
    void SendToast(String message);
    void ShowMatches();
    void setInformation(String title, String content, boolean show);
    void getService(int service);

    void keepScreenOn();
    void stopKeepingScreenOn();

}
