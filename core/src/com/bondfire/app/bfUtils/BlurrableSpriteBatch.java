package com.bondfire.app.bfUtils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

public class BlurrableSpriteBatch extends SpriteBatch {

    float blurAmount;
    static float R = 2.3f;

    float C_H = 0f;  //target height
    float MAX_H = 0f; //max height
    float MIN_H = 0f; //mininum height

    float C_W = 0f; //target width
    float MAX_W = 0f; //max width
    float MIN_W = 0f; //min width

    float Y; //Y coordinates
    float X; //X coordinates

    public void setBlurAmount(float blurAmount){
        this.blurAmount = blurAmount;
    }

    @Override
    public void draw(
            Texture texture,
            float x, float y,
            float originX,
            float originY,
            float width,
            float height,
            float scaleX,
            float scaleY,
            float rotation,
            int srcX,
            int srcY,
            int srcWidth,
            int srcHeight,
            boolean flipX,
            boolean flipY)

    {
        MAX_W = srcWidth;
        MAX_H = srcHeight;
        MIN_H = MAX_H / R;
        MIN_W = MAX_W / R;

        C_H = MIN_H + (MAX_H - MIN_H) *(( blurAmount/4 )/(1));
        C_W = MIN_W + (MAX_W - MIN_W) *(( blurAmount/4 )/(1));

        Y = srcY + (MAX_H/2 - C_H/2);
        X = srcX + (MAX_W/2 - C_W/2);

       //perform maths here
        super.draw(
                texture,
                x,
                y,
                originX,
                originY,
                width,
                height,
                scaleX,
                scaleY,
                rotation,
                MathUtils.round(X),
                MathUtils.round(Y),
                MathUtils.round(C_W),
                MathUtils.round(C_H),
                flipX,
                flipY);

    }

    @Override
    public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation) {

        MAX_W = region.getRegionWidth();
        MAX_H = region.getRegionHeight();

        MIN_H = MAX_H / R;
        MIN_W = MAX_W / R;

        C_H = MIN_H + (MAX_H - MIN_H) *(( blurAmount/4 )/(1));
        C_W = MIN_W + (MAX_W - MIN_W) *(( blurAmount/4 )/(1));

        Y = region.getRegionX() + (MAX_H/2 - C_H/2);
        X = region.getRegionY() + (MAX_W/2 - C_W/2);

        super.draw(
                region,
                x,
                y,
                originX,
                originY,
                width,
                height,
                scaleX,
                scaleY,
                rotation);
    }

   /*
    @Override
    public void draw(TextureRegion region, float x, float y, float width, float height) {

        //make the circles smaller as blurAmount increases
        MAX_W = 0;
        MAX_H = 0;
        MIN_H = width;
        MIN_W = height;

        C_H = MIN_H + (MAX_H - MIN_H) *(( blurAmount/1 )/(1));
        C_W = MIN_W + (MAX_W - MIN_W) *(( blurAmount/1 )/(1));

        Y = y + (MIN_H - C_H)/2;
        X = x + (MIN_W - C_W)/2;

        super.draw(region, X, Y, C_W, C_H);
    }*/
}
