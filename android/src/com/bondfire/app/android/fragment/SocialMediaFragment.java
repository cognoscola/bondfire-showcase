package com.bondfire.app.android.fragment;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bondfire.app.R;
import com.bondfire.app.android.activity.MainActivity;
import com.bondfire.app.android.interfaces.ScanFragmentActionListener;

import java.util.HashMap;
import java.util.List;

@SuppressLint("ValidFragment")
public class SocialMediaFragment extends BaseFragment {

    private final static String Tag = "ScanFragment ";
    private final static boolean d_onCreateView = false;
    private final static boolean d_onItemClick = true;

    private final static String tumblr_url = "http://gorillamoa.tumblr.com";
    private final static String instagram_url = "http://instagram.com/gorillamoa";
    private final static String twitter_url = "https://twitter.com/gorillamoa";
    private final static String player_url = "https://player.me/gorillamoa";
    private final static String plus_url = "https://plus.google.com/u/1/104883910958854839031/posts";

    View rootView;
    ScanFragmentActionListener listener;
    HashMap<String, Integer>  services;

//    @Inject ScanListModel mScanListModel;
    public SocialMediaFragment(){}

    public SocialMediaFragment(ScanFragmentActionListener listener){this.listener = listener;}

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(rootView == null){
            rootView = inflater.inflate(R.layout.fragment_not_available_yet, null);
        }

        rootView.findViewById(R.id.iv_twitter_gorillamoa).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = null;
                try {
                    // get the Twitter app if possible
                    getActivity().getPackageManager().getPackageInfo("com.twitter.android", 0);
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?user_id=3241223745"));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                } catch (Exception e) {
                    // no Twitter app, revert to browser
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(twitter_url));
                }
                getActivity().startActivity(intent);
            }
        });

        rootView.findViewById(R.id.iv_instagram_gorillamoa).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iIntent;
                try{
                    iIntent = getActivity().getPackageManager().getLaunchIntentForPackage("com.instagram.android");
                    iIntent.setComponent(new ComponentName( "com.instagram.android", "com.instagram.android.activity.UrlHandlerActivity"));
                    iIntent.setData(Uri.parse("http://instagram.com/p/2_gKnttgRO/"));
                }catch (NullPointerException e){
                    iIntent = new Intent(Intent.ACTION_VIEW);
                    iIntent.setData(Uri.parse(instagram_url));
                }
                startActivity(iIntent);
            }
        });

        rootView.findViewById(R.id.iv_tumblr_gorillamoa).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(tumblr_url));
                startActivity(i);
            }
        });

        rootView.findViewById(R.id.iv_player_gorillamoa).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(player_url));
                startActivity(i);
            }
        });
        rootView.findViewById(R.id.iv_plus_gorillamoa).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(plus_url)));
                }catch (NullPointerException e){
                    Log.e(Tag,"Something went wrong :(");
                    Toast.makeText(getActivity(),"Link Broken :(",Toast.LENGTH_SHORT).show();
                }
            }
        });
        rootView.findViewById(R.id.store_enter_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /** change our fragment padding*/
                try {
                    ((MainActivity)getActivity()).showStoreFragment();
                } catch (IllegalStateException e) {
                    Log.e(Tag, "onClick: Tried to switch to Store Fragment",e );
                }
            }
        });

        return rootView;
    }


    public static SocialMediaFragment newInstance(ScanFragmentActionListener listener){
        return new SocialMediaFragment(listener);

    }

    @Override
    protected List<Object> getModules() {
        return null;
    }

}

