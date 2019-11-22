package com.bondfire.app.android.background;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;

public class BackgroundGradientView extends View {

    private final static String TAG = "BackgroundGradientView";

    /** set the background to refresh at 60Hz */
    protected static final long FPS_DELAY = 1000 / 60;

    /** calculate background every second */
    private final static int SECONDS_PER_DAY =  86400;

    /** calculate the RGB values of the base gradients used by backgound.
     * The base cycle can be modeled after a simple Sin function*/
    //RED CEILING
    private final static float A_RED = 255/2;
    private final static float B_RED = (float)((2.0*3.14)/SECONDS_PER_DAY);
    private final static float C_RED = (float)(B_RED*SECONDS_PER_DAY)/8;//((1/4)*SECONDS_PER_DAY)*B;
    private final static float D_RED = 255/2;

    //GREEN CEILING
    private final static float A_BLUE = 255/2;
    private final static float B_BLUE = (float)((2.0*3.14)/SECONDS_PER_DAY);
    private final static float C_BLUE = (float)(B_BLUE*SECONDS_PER_DAY)/4;//((1/4)*SECONDS_PER_DAY)*B;
    private final static float D_BLUE = 255/2;

    //BLUE CEILING
    private final static float A_GREEN = 255/2;
    private final static float B_GREEN = (float)((2.0*3.14)/SECONDS_PER_DAY);
    private final static float C_GREEN = (float)(B_GREEN*SECONDS_PER_DAY)/4;//((1/4)*SECONDS_PER_DAY)*B;
    private final static float D_GREEN = 255/2;

    int Sky_Red;
    int Sky_Green;
    int Sky_Blue;

    //Disconnected = grey
    private final static int State_disconnected_R = 150;
    private final static int State_disconnected_G = 150;
    private final static int State_disconnected_B = 150;

    //Bluetooth connected =  blueish green;
    private final static int State_Bluetooth_R = 13;
    private final static int State_Bluetooth_G = 152;
    private final static int State_Bluetooth_B = 186;

    //Network Connected = bright yellow;
    private final static int State_LAN_R = 255;
    private final static int State_LAN_G = 105;
    private final static int State_LAN_B = 2;

    //Server connected = redish purple
    private final static int State_Server_R = 227;
    private final static int State_Server_G = 66;
    private final static int State_Server_B = 52;

    private int ConnectStateRed = 100;
    private int ConnectStateGreen = 100;
    private int ConnectStateBlue = 100;

    /** connection states for the background*/
    public enum ConnectionState{
        DISCONNECED, BLUETOOTH, LAN, SERVER
    }

    /** default is disconnected state */
    private ConnectionState mConnectionState = ConnectionState.DISCONNECED;

    private Context context;

    Shader shader;
    Paint bgpaint = new Paint();
    //    static Calendar c;
    static RectF gradientRect = new RectF();

    Paint pattern = new Paint();

    int Second;
    int Second_of_Day;

    int circleIndexer;

    Time time = new Time();

    private Bitmap gradientBitmap;

    //CIRCLE STUFF
    private Bitmap bmp;
    CircleInfo[] circles;
    Paint[] circlePaints;

    int pixel;

    private static final int Columns = 4;
    private static final int Rows    = 6;
    private static final int total = Columns*Rows;

    private static final int circleRadius = 20;

    private static boolean isPaused;

    Canvas mCanvas;

    protected final Runnable animator = new Runnable() {

        @Override
        public void run() {
            nextFrame();
            invalidate();
        }
    };

    public BackgroundGradientView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
//        createPaints();
//        resetAnimation();
    }

    protected void createPaints() {

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    @Override
    public void setVisibility(int visibility) {

        super.setVisibility(visibility);
        switch (visibility) {
            case View.VISIBLE:
                startAnimation();
                break;
            default:
                stopAnimation();
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // Determine horizontal and vertical padding
        int paddingX = getPaddingLeft() + getPaddingRight();
        int paddingY = getPaddingBottom() + getPaddingTop();

        int minW, minH, w, h;
        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case MeasureSpec.EXACTLY:
                // Try for a height based on our minimum including vertical padding
                minH = getSuggestedMinimumHeight() + paddingY;
                h = MeasureSpec.getSize(heightMeasureSpec);
                // Set the width according to the height as our control should be
                // square, again compensating for padding
                minW = MeasureSpec.getSize(h) - paddingY + paddingX;
                w = resolveSize(minW, widthMeasureSpec);
                break;
            default:
                // Try for a width based on our minimum including horizontal padding
                minW = getSuggestedMinimumWidth() + paddingX;
                w = resolveSize(minW, widthMeasureSpec);

                // Set the height according to the width as our control should be
                // square, again compensating for padding
                minH = MeasureSpec.getSize(w) - paddingX + paddingY;
                h = resolveSize(minH, heightMeasureSpec);
                break;
        }
        setMeasuredDimension(w, h);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void nextFrame() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
//            Log.e(TAG, "Less than JellyBean");
            postDelayed(animator, FPS_DELAY);
        } else {
//            Log.e(TAG, "Higher than JellyBean");
            postOnAnimation(animator);
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        //configure bitmap stuff
        bmp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(bmp);

        int test = getResources().getConfiguration().orientation;

        switch(test){
            case Configuration.ORIENTATION_PORTRAIT: {
                //bitmap dimensions
                int resx = getRight() / Columns;
                int resy = getBottom() / Rows;

                circles = new CircleInfo[total];
                circlePaints = new Paint[total];


                //DERIVE POSITIONS
                for (int pos = 0; pos < total; pos++) {  //ROW
                    circlePaints[pos] = new Paint();
                    circlePaints[pos].setStyle(Paint.Style.FILL);
                    circles[pos] = new CircleInfo();
                    circles[pos].setI(pos % Columns);
                    circles[pos].setJ((pos - circles[pos].getI()) / Columns);
                    circles[pos].setX(circles[pos].getI() * resx + circleRadius * 3);
                    circles[pos].setY(circles[pos].getJ() * resy + circleRadius * 3);
                }
                break;
            }
            case Configuration.ORIENTATION_LANDSCAPE:{
                int resx = getRight() / Rows;
                int resy = getBottom() / Columns;

                circles = new CircleInfo[total];
                circlePaints = new Paint[total];

                //DERIVE POSITIONS
                for (int pos = 0; pos < total; pos++) {  //ROW

                    circlePaints[pos] = new Paint();
                    circlePaints[pos].setStyle(Paint.Style.FILL);

                    circles[pos] = new CircleInfo();

                    circles[pos].setI(pos % Rows);
                    circles[pos].setJ((pos - circles[pos].getI()) / Rows);

                    circles[pos].setX(circles[pos].getI() * resx + circleRadius * 3);
                    circles[pos].setY(circles[pos].getJ() * resy + circleRadius * 3);
                }
                break;
            }
        }
    }

    private int compare(int a, int b){

        if(a - b > 0){
            return a - 1;
        }else {
            return a + 1;
        }

        //int returns = ((a - b) > 0) ? a--:a++;
        //Log.e("COLOR", "A:"+a + "B:" + b);
        // return returns;
    }

    private int checkEquals(int a, int b){
        return (a == b) ? a: compare(a,b);
    }

    private void updateColor(){
        switch(mConnectionState){
            case DISCONNECED:
                ConnectStateRed = checkEquals(ConnectStateRed, State_disconnected_R);
                ConnectStateGreen = checkEquals(ConnectStateGreen, State_disconnected_G);
                ConnectStateBlue = checkEquals(ConnectStateBlue, State_disconnected_B);
                break;
            case BLUETOOTH:
                ConnectStateRed = checkEquals(ConnectStateRed, State_Bluetooth_R);
                ConnectStateGreen = checkEquals(ConnectStateGreen, State_Bluetooth_G);
                ConnectStateBlue = checkEquals(ConnectStateBlue, State_Bluetooth_B);
                break;
            case LAN:
                ConnectStateRed = checkEquals(ConnectStateRed, State_LAN_R);
                ConnectStateGreen = checkEquals(ConnectStateGreen, State_LAN_G);
                ConnectStateBlue = checkEquals(ConnectStateBlue, State_LAN_B);
                break;
            case SERVER:
                ConnectStateRed = checkEquals(ConnectStateRed, State_Server_R);
                ConnectStateGreen = checkEquals(ConnectStateGreen, State_Server_G);
                ConnectStateBlue = checkEquals(ConnectStateBlue, State_Server_B);
                break;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        time.setToNow();
        updateColor();

        /** Are we still on the current second */
        if(time.second != Second){

            // Log.e("Connection State", "State:"+mConnectionState + " RGB: " +ConnectStateRed + " " +ConnectStateGreen + " " +ConnectStateBlue  );

            Second_of_Day = time.hour * 60 * 60 + time.minute * 60 + time.second;
            // Log.e("OnDraw()", "State = " + mConnectionState);

            Second = time.second;
            setRed(Second_of_Day);
            setGreen(Second_of_Day);
            setBlue(Second_of_Day);

            shader  = new LinearGradient(0, 0, 0, getBottom(), Color.argb(255, Sky_Red, Sky_Green, Sky_Blue), Color.argb(255, ConnectStateRed, ConnectStateGreen, ConnectStateBlue), Shader.TileMode.CLAMP);
            bgpaint.setShader(shader);
            gradientRect.set(0, 0, getRight(), getBottom());
            mCanvas.drawRect(gradientRect, bgpaint);


            //   Log.e("onDraw()"," WIDTH:" + bmp.getWidth() + " HEIGHT: " + bmp.getHeight() );
            for(circleIndexer = 0; circleIndexer < total; circleIndexer++){
                pixel =  bmp.getPixel(479,  circles[circleIndexer].getY());
                circlePaints[circleIndexer].setColor(Color.rgb(Color.red(pixel), Color.green(pixel), Color.blue(pixel)));
                mCanvas.drawCircle(circles[circleIndexer].getX() , circles[circleIndexer].getY(), circleRadius*2, circlePaints[circleIndexer]);
            }
        }
        canvas.drawBitmap(bmp, 0,0,null);
    }

    protected void resetAnimation() {
    }

    public void startAnimation() {
        if (isShown()) {
            nextFrame();
        }
    }

    public void stopAnimation() {
        removeCallbacks(animator);
    }


    private void setRed(int time){
        Sky_Red = (int)Math.round(A_RED*Math.sin(2*(B_RED*time - C_RED)) + D_RED);
    }

    private void setGreen(int time){
        Sky_Green = (int)Math.round(A_GREEN*Math.sin(B_GREEN*time - C_GREEN) + D_GREEN);
        if(Sky_Green > 191){  //191
            Sky_Green = 191;
        }
    }
    private void setBlue(int time){
        Sky_Blue = (int)Math.round(A_BLUE*Math.sin(B_BLUE*time - C_BLUE) + D_BLUE);
    }

    public void SetConnectionState(ConnectionState state){
        this.mConnectionState = state;
    }

    public Bitmap getBackgroundBitmap(){
      /*  setDrawingCacheEnabled(true);
        Bitmap bitmap = getDrawingCache();
        setDrawingCacheEnabled(false);*/
        return bmp;
    }

}
