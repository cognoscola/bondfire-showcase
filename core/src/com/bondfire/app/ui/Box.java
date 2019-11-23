package com.bondfire.app.ui;

/**
 * Created by alvaregd on 01/11/15.
 */
public class Box {

    protected float x;
    protected float y;
    protected float width;
    protected float height;

    public boolean contains(float x, float y){
        return x > this.x - width /2 &&
                x < this.x + width/2 &&
                y > this.y - height /2 &&
                y < this.y + height /2;
    }
}
