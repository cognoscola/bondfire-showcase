package com.bondfire.app.android.interfaces;

import android.graphics.Bitmap;

public interface IGameListener {

    public int StartGame(int game);

    public int StopGame();

    public int NotifyClientReady(String source);

    public void IncomingGameLogic(String[] data, String source);

    public void TransferImager(Bitmap Image, int imageid);
}

