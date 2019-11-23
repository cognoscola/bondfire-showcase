package com.bondfire.app.networkUtils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.StreamUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by alvaregd on 31/07/15.
 * Downloads and Image from the net or elsewhere and converts to texture Region
 */
public class ImageDownloader {

    private final static String Tag = ImageDownloader.class.getName();
    private final static boolean d_run = false;
    private final static boolean d_download =false;

    public interface ImageDownloadCallBack {
        void TextureImageReceived(TextureRegion image);
    }

    public static void getImage(final String url, final ImageDownloadCallBack callback) {
        new Thread(new Runnable() {
            /**
             * Downloads the content of the specified url to the array. The array has to be big enough.
             */
            private int download(byte[] out, String url) {
                InputStream in = null;
                try {
                    HttpURLConnection conn = null;
                    conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(false);
                    conn.setUseCaches(true);
                    conn.connect();
                    in = conn.getInputStream();
                    int readBytes = 0;
                    while (true) {
                        int length = in.read(out, readBytes, out.length - readBytes);
                        if (length == -1) break;
                        readBytes += length;
                    }
                    return readBytes;
                } catch (Exception ex) {
                    System.out.println(Tag + " Exception while downloading image: "+ ex.getMessage());
                    return 0;
                } finally {
                    if(d_download)System.out.println(Tag + " Closing Stream.");
                    StreamUtils.closeQuietly(in);
                }
            }

            @Override
            public void run() {
                byte[] bytes = new byte[200 * 1024]; // assuming the content is not bigger than 200kb.
                if(d_run) System.out.println(Tag + " Fetching:" + url);
                int numBytes = download(bytes, url);
//                int numBytes = download(bytes, "http://lh4.googleusercontent.com/-15r8MK5M5y8/AAAAAAAAAAI/AAAAAAAAACo/iIgmQtrmXV8/s96-ns/");
                if (numBytes != 0) {
                    // load the pixmap, make it a power of two if necessary (not needed for GL ES 2.0!)
                    Pixmap pixmap = new Pixmap(bytes, 0, numBytes);
                    final int originalWidth = pixmap.getWidth();
                    final int originalHeight = pixmap.getHeight();
                    int width = MathUtils.nextPowerOfTwo(pixmap.getWidth());
                    int height = MathUtils.nextPowerOfTwo(pixmap.getHeight());
                    final Pixmap potPixmap = new Pixmap(width, height, pixmap.getFormat());
                    potPixmap.drawPixmap(pixmap, 0, 0, 0, 0, pixmap.getWidth(), pixmap.getHeight());
                    pixmap.dispose();
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            callback.TextureImageReceived(new TextureRegion(new Texture(potPixmap), 0, 0, originalWidth, originalHeight));
                        }
                    });
                }
            }
        }).start();
    }
}
