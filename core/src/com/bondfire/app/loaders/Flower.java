package com.bondfire.app.loaders;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.bondfire.app.ui.Box;

import java.util.ArrayList;

/**
 * Created by alvaregd on 01/11/15.
 */
public class Flower  extends Box {

    private int coverLeafCount = 0;
    private int totalLeafCount = 0;

    private int leafsPerRevolution = 0;

    private int leafSize  = 100;
    private float leafSizeExtra = 0f;
    private float centerRadius = 25;
    private float coverRadius = 150;

    private int leafIndexer = 0;

    /** growth stuff */
    private boolean isGrowing = false;
    private float revealRate = 5f;   //will grow every 5 seconds
    private float revealTimer = 0f;
    private float reavelAngle = 0f; // the angle at which a new leaf will appear

    /** flower angle stuff */
    private float angleTraversed = 0f;             //the current angle of the flower
    private final static float MAX_GROWN_TRAVERSE_ANGLE = 1440f;
    private final float MAX_ANGLE = 360;  //the max allowable angle
    private float spinRate = 5;           //the time it takes to complete a full revolution
    private float spinTimer = 0f;         //timer to keep track of spin
    private float totalRevealTime = 0f;

    private ArrayList<ColourLeaf> leafs;
    private ArrayList<ColourLeaf> coverLeafs;

    public Flower(float x, float y ){
        this.x = x;
        this.y = y;

        leafs  = new ArrayList<ColourLeaf>();
        coverLeafs = new ArrayList<ColourLeaf>();

    }

    public void setLeafsPerRevolution(int rate){
        this.leafsPerRevolution = rate;
    }

    public void isGrowing(boolean growing){
        this.isGrowing = growing;
    }

    public void setSpinRate(float spinRate){
        this.spinRate = spinRate;
        updateRevealRate();
        for(ColourLeaf leaf: coverLeafs){
            leaf.setSpinTime(spinRate);
        }

        for(ColourLeaf leaf: leafs){
            leaf.setSpinTime(spinRate);
        }
    }

    public void update(float dt){
        /** update the individual leafs **/

        for(ColourLeaf leaf: coverLeafs){
            leaf.update(dt);
        }
        for(ColourLeaf leaf: leafs){
            leaf.update(dt);
        }

        angleTraversed += MAX_ANGLE * dt/spinRate;
//        System.out.println("Angle Traversed:" + angleTraversed);

        if(angleTraversed > MAX_GROWN_TRAVERSE_ANGLE){
//            System.out.println("STOPPED GROWING");
            isGrowing = false;
        }

        if(isGrowing){
            revealTimer +=dt;
            spinTimer   +=dt;

            if(revealTimer > revealRate){
                updateExtraSize();

                ColourLeaf leaf = new ColourLeaf(this.x , this.y,

                        //set the size
                        (totalLeafCount < 4) ? leafSize:

                                leafSize + leafSizeExtra );


                leaf.setSpinTime(spinRate);
                leaf.setAngle(reavelAngle);
                if(totalLeafCount < 4)coverLeafs.add(leaf);
                else  leafs.add(leaf);
                totalLeafCount++;
                revealTimer = 0f;
                updateRevealRate();
            }

            if(spinTimer > spinRate){
                //find the total count for that layer
                spinTimer = 0f;


            }
            //the rate growth rate such that the angle between leafs appear equal
            //check the angl
        }

    }

    private void updateRevealRate() {
        if (totalLeafCount < 4) {
            revealRate = 0.25f * spinRate;
        } else {
            if (angleTraversed > 720) {
                revealRate =spinRate/leafsPerRevolution;
            } else if (angleTraversed > 360) {
                revealRate = spinRate/leafsPerRevolution;//0.30f;
            }
        }
    }

    private void updateExtraSize(){
        if(totalLeafCount > 3){
            //calculate extra leaf size
            if(angleTraversed > 1440){
//                System.out.println("stopped growing at > 1080");
            }else if (angleTraversed > 1080){

//                leafSizeExtra = leafSize * ((angleTraversed - 360)/720 + (angleTraversed - 720)/720);
//                System.out.println("growing at > 1080");
            }else if (angleTraversed > 720){

//                System.out.println("growing at > 720");
                leafSizeExtra = leafSize * ((angleTraversed - 360)/720 + (angleTraversed - 720)/720);

            }else if(angleTraversed > 360){

//                System.out.println("growing at > 360");
                leafSizeExtra = leafSize * (angleTraversed - 360)/720 ;
            }
        }
    }

    public void render(Batch sb, ShaderProgram shader) {

        for (leafIndexer = leafs.size() - 1; leafIndexer >= 0; leafIndexer--) {
            leafs.get(leafIndexer).render(sb, shader);
        }

        for (ColourLeaf leaf : coverLeafs) {
            leaf.render(sb, shader);
        }
    }

    public void dispose(){
        leafs.clear();
        coverLeafs.clear();
    }
}
