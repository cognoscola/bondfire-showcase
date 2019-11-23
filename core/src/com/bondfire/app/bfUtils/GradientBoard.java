package com.bondfire.app.bfUtils;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.TimeUtils;

public class GradientBoard {

    /** Debugging */
    static Logger loger;

    /** Timing variables used to calculate background every second */
    private final static int SECONDS_PER_DAY   =86400;
    private static long secondOfTheDay;
    private static long millis;
    private static long seconds;
    private static long oldSecond;

    private static float x;
    private static float y;
    private static int Width;
    private static int Height;

    private static boolean isFloorEnabled = true; /** if true, the floor also change base according to time )

    /**The base cycle can be modeled after a simple Sin function
     * Here we have sinusoidal constants*/
    //RED CEILING
    private final static float A_RED_CEILING = 255/3;
    private final static float B_RED_CEILING = (float)((2.0*3.14)/SECONDS_PER_DAY);
    private final static float C_RED_CEILING = (B_RED_CEILING *SECONDS_PER_DAY)/8;//((1/4)*SECONDS_PER_DAY)*B;
    private final static float D_RED_CEILING = 255/2;

    //GREEN CEILING
    private final static float A_BLUE_CEILING = 255/2;
    private final static float B_BLUE_CEILING = (float)((2.0*3.14)/SECONDS_PER_DAY);
    private final static float C_BLUE_CEILING = (B_BLUE_CEILING *SECONDS_PER_DAY)/4;//((1/4)*SECONDS_PER_DAY)*B;
    private final static float D_BLUE_CEILING = 255/2;

    //BLUE CEILING
    private final static float A_GREEN_CEILING = 255/2;
    private final static float B_GREEN_CEILING = (float)((2.0*3.14)/SECONDS_PER_DAY);
    private final static float C_GREEN_CEILING = (B_GREEN_CEILING*SECONDS_PER_DAY)/4;//((1/4)*SECONDS_PER_DAY)*B;
    private final static float D_GREEN_CEILING = 255/2;

    //RED FLOOR
    private final static float A_RED_FLOOR = 255/2;
    private final static float B_RED_FLOOR = (float)((2.0*3.14)/SECONDS_PER_DAY);
    private final static float C_RED_FLOOR = (B_RED_FLOOR *SECONDS_PER_DAY)/8;//((1/4)*SECONDS_PER_DAY)*B;
    private final static float D_RED_FLOOR = 255/2;

    //GREEN FLOOR
    private final static float A_BLUE_FLOOR = 255/2;
    private final static float B_BLUE_FLOOR = (float)((2.0*3.14)/SECONDS_PER_DAY);
    private final static float C_BLUE_FLOOR = (B_BLUE_FLOOR *SECONDS_PER_DAY)/4;//((1/4)*SECONDS_PER_DAY)*B;
    private final static float D_BLUE_FLOOR = 255/2;

    //BLUE FLOOR
    private final static float A_GREEN_FLOOR = 255/2;
    private final static float B_GREEN_FLOOR = (float)((2.0*3.14)/SECONDS_PER_DAY);
    private final static float C_GREEN_FLOOR = (B_GREEN_FLOOR*SECONDS_PER_DAY)/4;//((1/4)*SECONDS_PER_DAY)*B;
    private final static float D_GREEN_FLOOR = 255/2;


    private final static float G_LIMIT  = 0.7490f;

    private boolean doesNotCoverScreen = false;

    /** functions to calcualte the RGB Sky values */
    private void setCeilingRed(float time){
        skyColor.r = (float)((A_RED_CEILING *Math.sin(2*(B_RED_CEILING *time - C_RED_CEILING)) + D_RED_CEILING) /255);
    }
    private void setCeilingGreen(float time){
        skyColor.g = (float) ((A_GREEN_CEILING *Math.sin(B_GREEN_CEILING*time - C_GREEN_CEILING) + D_GREEN_CEILING) /255);
        if(skyColor.g > G_LIMIT){
            skyColor.g = G_LIMIT;
        }
    }
    private void setCeilingBlue(float time){
        skyColor.b = (float) ((A_BLUE_CEILING *Math.sin(B_BLUE_CEILING *time - C_BLUE_CEILING) + D_BLUE_CEILING) /255);
    }

    private void setFloorRed(float time){
        floorColor.r = (float)((A_RED_FLOOR *Math.sin(2*(B_RED_FLOOR *time - C_RED_FLOOR)) + D_RED_FLOOR) /255);
    }

    private void setFloorGreen(float time){
        floorColor.g = (float) ((A_GREEN_FLOOR *Math.sin(B_GREEN_FLOOR*time - C_GREEN_FLOOR) + D_GREEN_FLOOR) /255);
        if(floorColor.g > G_LIMIT){
            floorColor.g = G_LIMIT;
        }
    }
    private void setFloorBlue(float time){
        floorColor.b = (float)((A_RED_FLOOR *Math.sin(2*(B_RED_FLOOR *time - C_RED_FLOOR)) + D_RED_FLOOR) /255);
    }

    private void updateTimeAndColor(){
        millis  = TimeUtils.nanoTime();
        seconds = (long) (millis / 1E9); //update every second
//        seconds = (long) (millis / 1E8); //update every millisecond

        /** we don't need to waste resources calculating every frame, only every second */
        if(seconds != oldSecond){
            oldSecond = seconds;
            secondOfTheDay++;
//            loger.info(" Second of day: " + secondOfTheDay + " R:" + skyColor.r + " G:"+ skyColor.g + " B:" + skyColor.b);
            setCeilingRed(secondOfTheDay);
            setCeilingGreen(secondOfTheDay);
            setCeilingBlue(secondOfTheDay);

            if(isFloorEnabled){
                setFloorRed(secondOfTheDay);
                setFloorGreen(secondOfTheDay);
                setFloorBlue(secondOfTheDay);
            }
        }
    }

    private  Color skyColor;
    private  Color floorColor;

    private GradientBoard(){

    }

    private GradientBoard(int x, int y, int width, int height){

        skyColor = new Color();
        floorColor = new Color( 0.776f,0.775f,0.839f,1f);

        Width  = width;
        Height = height;
        this.x = x;
        this.y = y;
    }

    public static GradientBoard newIntance(int x, int y ,int screenWidgth, int screenHeight, int secondofDay){
        GradientBoard board = new GradientBoard(x,y,screenWidgth, screenHeight);
        board.setDoesNotCover(true);
        board.findSeconds(secondofDay);
        return board;
    }

    public static GradientBoard newIntance(int screenWidgth, int screenHeight, int secondofDay){
       GradientBoard board = new GradientBoard(0,0,screenWidgth, screenHeight);
        board.findSeconds(secondofDay);
       return board;
   }

    public void render(ShapeRenderer painter){
        if(doesNotCoverScreen){
            System.out.println("Rendering BG smal");
            updateTimeAndColor();
            painter.begin(ShapeRenderer.ShapeType.Filled);
//            painter.rect(200,200,200,200, floorColor, floorColor, skyColor, skyColor);
            painter.rect(x - Width / 2, y - Height / 2, Width, Height, floorColor, floorColor, skyColor, skyColor);
            painter.end();
        }else{
            updateTimeAndColor();
            painter.begin(ShapeRenderer.ShapeType.Filled);
            painter.rect(0, 0, Width, Height, floorColor,floorColor,skyColor,skyColor);
            painter.end();
        }
    }

    public void setDoesNotCover(boolean cover){
        this.doesNotCoverScreen = cover;
    }

    public void findSeconds(int secondofDay){
        if(secondofDay != 0){
            secondOfTheDay = secondofDay;
//           loger.info(" Setting Time AT: " + secondOfTheDay);
        }else{
            millis  = TimeUtils.nanoTime();
            secondOfTheDay = (long) (millis / 1E9);
//           loger.info(" Setting Time AT: " + secondOfTheDay);
        }
    }

    public void setFloorEnable(boolean floor){
        this.isFloorEnabled = true;
    }


}
