package com.bondfire.app.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.badlogic.gdx.graphics.Pixmap;
import com.bondfire.app.android.imports.stackblur.StackBlurManager;

public class BlurrableImageView extends ImageView  {

    float radius;
    private StackBlurManager stackBlurManager;
    private Bitmap original;

    public BlurrableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BlurrableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BlurrableImageView(Context context) {
        super(context);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        original = bm;
        stackBlurManager = new StackBlurManager(bm);
        super.setImageBitmap(process(radius,bm));
    }

    public void setPercentBlur(float percent){
        this.radius = percent * 5;
        super.setImageBitmap(process(radius, original));
    }

    public Bitmap process(float radius, Bitmap bm){
        if(stackBlurManager != null){
            return stackBlurManager.process(radius);
        }else{
            return bm;
        }
    }


    public void setImagePixmap(Pixmap pixmap){
        if(pixmap == null){

            return ;
        }

        int w = pixmap.getWidth();
        int h = pixmap.getHeight();

        System.out.println("original W: " + w);
        System.out.println("original H: " + h);
        int[] pixels = new int[w * h];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {

                //convert RGBA to RGB
                int value = pixmap.getPixel(x, y);
                int R = ((value & 0xff000000) >>> 24);
                int G = ((value & 0x00ff0000) >>> 16);
                int B = ((value & 0x0000ff00) >>> 8);
                int A = ((value & 0x000000ff));
                int i = x + (y * w);
                pixels[i] = (A << 24) | (R << 16) | (G << 8) | B;
            }
        }
           /* try{
                FileHandle fh;
                do{
                    fh = Gdx.files.external("trolololo.png");
                }while (fh.exists());

                PixmapIO.writePNG(fh, pixmap);

            }catch (Exception e){
            }*/

        pixmap.dispose();
        setImageBitmap(Bitmap.createScaledBitmap(
                Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888),
                w/8,
                h/8,
                true));
    }
}
