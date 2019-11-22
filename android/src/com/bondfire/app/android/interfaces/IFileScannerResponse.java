package com.bondfire.app.android.interfaces;

import android.graphics.Bitmap;

public interface IFileScannerResponse {

    void onGameFound(String[] filename, String[] title,
                     Bitmap[] icons);
}
