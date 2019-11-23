package com.bondfire.app.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;

import com.andexert.library.RippleView;
import com.bondfire.app.R;
import com.bondfire.app.android.activity.MainActivity;


public class TabViewBase extends LinearLayout implements View.OnClickListener {

    private Context context;

    public final static int TAB_SOCIAL = 2;
    public final static int TAB_GAME= 1;
    public final static int TAB_INFORMATION = 0;

    RippleView leftButton;
    RippleView middleButton;
    RippleView rightButton;

    ImageView leftImage;
    ImageView middleImage;
    ImageView rightImage;

    ImageView leftAlert, middleAlert,rightALert;

    public TabViewBase(Context context){
        super(context);
        initialize(context);
        this.context = context;
    }
    public TabViewBase(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs,defStyle);
        this.context = context;
        initialize(context);
    }

    public TabViewBase(Context context, AttributeSet attrs){
        super(context, attrs);
        this.context = context;
        initialize(context);
    }

    private MainActivity getMainActivity()
    {
        return (MainActivity)getContext();
    }

    public void initialize(Context context){
        LayoutInflater lif = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        lif.inflate(R.layout.tab_base,this);

        leftImage   = (ImageView) this.findViewById(R.id.leftImage);
        middleImage = (ImageView) this.findViewById(R.id.middleImage);
        rightImage  = (ImageView) this.findViewById(R.id.rightImage);

        //initialize the buttons
        leftButton   = (RippleView) this.findViewById(R.id.leftRipple);
        leftButton.  setRippleDuration(150);
        middleButton = (RippleView) this.findViewById(R.id.middleRipple);
        middleButton.setRippleDuration(150);
        rightButton  = (RippleView) this.findViewById(R.id.rightRipple);
        rightButton. setRippleDuration(150);

        leftAlert = (ImageView)this.findViewById(R.id.leftalert);
        rightALert = (ImageView) this.findViewById(R.id.rightAlert);

        leftButton.  setOnClickListener(this);
        middleButton.setOnClickListener(this);
        rightButton. setOnClickListener(this);

        leftImage.   getDrawable().setAlpha(80);
        middleImage. getDrawable().setAlpha(80);
        rightImage.  getDrawable().setAlpha(80);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.leftRipple:   getMainActivity().setPageView(0);setAlphas(0); break;
            case R.id.middleRipple: getMainActivity().setPageView(1);setAlphas(1); break;
            case R.id.rightRipple:  getMainActivity().setPageView(2);setAlphas(2); break;
        }
    }

    public void setAlphas(int select){
        leftImage.getDrawable().setAlpha(    (select == 0) ? 255:80);
        middleImage.getDrawable().setAlpha(  (select == 1) ? 255:80);
        rightImage.getDrawable().setAlpha(   (select == 2) ? 255:80);
    }

    public void showAlert(int select){
        leftAlert.setVisibility((select == 0)? View.VISIBLE: View.GONE);
    }

    public void showSocialAlart(){
        rightALert.setVisibility(View.VISIBLE);
    }

    public void CancelAlert(int select){

        if (select == TAB_SOCIAL) {
            rightALert.setVisibility(View.GONE);
        } else if (select == TAB_GAME) {

        } else if (select == TAB_INFORMATION) {
            leftAlert.setVisibility(View.GONE);
        }
    }

}
