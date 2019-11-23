package com.bondfire.app.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.bondfire.app.engine.Engine;
import com.bondfire.app.ui.Box;

/**
 * Created by alvaregd on 01/11/15.
 */
public class ColourLeaf extends Box {

    private final static float RATIO = 0.5f;
    private Texture leafTexture;

    private float colorTimer;
    private float angleTimer;
    private float MAX_colorTimer = 10; //time in sec to go through base chart
    private float MAX_angleTimer = 20; //time in sec it takes to make 1 revolution

    private final static float L = 0.5f;
    private final static float S = 1f;
    private float              H = 1f;

    private float MAX_HEIGHT = 0f;

    private float revealrate = 0.3f;
    private float revealTimer = 0.0f;

    private float angle = 0;
    private float MAX_ANGLE = 360;

    private float R;
    private float G;
    private float B;

    public ColourLeaf(float x, float y, float width){

        this.x = x;
        this.y = y;

        leafTexture = new Texture(Gdx.files.internal("bondfire/leaf_horizontal.png"));

//        leafTexture = Engine.getEngineContent().getAtlas("flower").findRegion("leaf");

        this.width = width;
        this.height = 0f;
        MAX_HEIGHT = width*RATIO;
    }

    public void update(float dt){

        if(revealTimer < revealrate){
            revealTimer +=dt;
            height = revealTimer/revealrate *MAX_HEIGHT;
        }

        angleTimer +=dt;
        colorTimer +=dt;
        if(colorTimer > MAX_colorTimer){
            colorTimer = 0f;
        }
        if(angleTimer > MAX_angleTimer){
            angleTimer = 0f;
        }

        angle = MAX_ANGLE * angleTimer/ MAX_angleTimer;
        H = colorTimer/MAX_colorTimer;
        hslToRgb(H,S,L);
    }

    public void render(ShapeRenderer sr){

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(1, 1, 0, 1);
        sr.rect(x, y, width,height);
        sr.end();
    }

    public void render(Batch sb, ShaderProgram shader){
        //        shader.setUniformf("u_resolution", width , height );
//        shader.setUniformf("u_centerPosition", this.x  , this.y );

        shader.begin();
        shader.setUniformf("u_color",R,G,B,1);
        shader.end();

        sb.begin();
//        sb.draw(leafTexture, x - width /2 ,y - height/2 ,width,height);

        sb.draw(
                leafTexture,
                x,
                y - height/2,
                0,
                height/2,
                width,
                height,
                1,
                1,
                angle,
                0,0,
                leafTexture.getWidth(),
                leafTexture.getHeight(),
                false,false);

//        sb.draw(leafTexture, x , y- height/2, 0,height/2, width,height,1,1,angle,true);
        sb.end();
    }

    /**
     * Converts an HSL base value to RGB. Conversion formula
     * adapted from http://en.wikipedia.org/wiki/HSL_color_space.
     * Assumes h, s, and l are contained in the set [0, 1] and
     * returns r, g, and b in the set [0, 255].
     * @param   h       The hue
     * @param   s       The saturation
     * @param   l       The lightness
     * @return  Array           The RGB representation
     */
    public void hslToRgb(float h, float s, float l){
        float r, g, b;

        if (s == 0f) {
            R = G = B = l; // achromatic
        } else {
            float q = l < 0.5 ? l * (1 + s) : l + s - l * s;
            float p = 2 * l - q;
            R = hueToRgb(p, q, h + 1f / 3f);
            G = hueToRgb(p, q, h);
            B = hueToRgb(p, q, h - 1f/3f);
        }
//        int[] rgb = {(int) (r * 255), (int) (g * 255), (int) (b * 255)};
//        return rgb;
    }

    public float hueToRgb(float p, float q, float t) {
        if (t < 0f)
            t += 1f;
        if (t > 1f)
            t -= 1f;
        if (t < 1f/6f)
            return p + (q - p) * 6f * t;
        if (t < 1f/2f)
            return q;
        if (t < 2f/3f)
            return p + (q - p) * (2f/3f - t) * 6f;
        return p;
    }

    public void setAngle(float angle){
        angleTimer =angle / MAX_ANGLE * MAX_angleTimer;
        colorTimer = angle / MAX_ANGLE ;
    }

    public void setSpinTime(float revTime){
        this.MAX_angleTimer = revTime;
    }

}