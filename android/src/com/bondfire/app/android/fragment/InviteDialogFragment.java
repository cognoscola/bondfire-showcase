package com.bondfire.app.android.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.app.DialogFragment;
import androidx.core.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bondfire.app.android.R;
import com.google.android.gms.games.multiplayer.Invitation;

import java.lang.reflect.Field;

/**
 * Created by alvaregd on 17/02/16.
 * The pop up the lets the user know they have received an invite
 */
public class InviteDialogFragment extends DialogFragment {

    private final static String Tag = "GameDescriptionFragment";
    private final static boolean d_onDestroy = false;
    private final static boolean d_onCreateView = false;
    private final static boolean d_lifecycle = false;


    View rootView;
    /** reference to the background  */
    Bitmap bg;

    public interface InviteDialogListener {
        void onDeclinePressed(Invitation inviteResult);
        void onJoinPressed(Invitation invitation);
    }

    private InviteDialogListener mListener;
    private Invitation invitation;

    private String gameName;
    private String hostName;

    private void setListener(InviteDialogListener listener){this.mListener = listener;}
    private void setInvitation(Invitation invitation){this.invitation = invitation;}
    public void setGameName(String gameName) {this.gameName = gameName;}
    public void setHostName(String hostName) {this.hostName = hostName;}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(androidx.core.app.DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        /** Creating the view for the dialog fragment... */
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_invite_dialog, container, false);
        }

        if (invitation != null) {

            String text = invitation.getInviter().getDisplayName() + " has invited you to join his party!";
            ((TextView)rootView.findViewById(R.id.tv_invite_instruction)).setText(text);

            rootView.findViewById(R.id.b_accept).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onJoinPressed(invitation);
                    dismiss();
                }
            });
            rootView.findViewById(R.id.b_decline).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onDeclinePressed(invitation);
                    dismiss();
                }
            });
        }else {

            String tex = "Game Invite!";
            ((TextView) (rootView).findViewById(R.id.invite_title)).setText(tex);
            tex= hostName + " is playing " + gameName + ". Would you like to Join?";
            ((TextView)rootView.findViewById(R.id.tv_invite_instruction)).setText(tex);

            rootView.findViewById(R.id.b_accept).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onJoinPressed(null);
                    dismiss();
                }
            });
            rootView.findViewById(R.id.b_decline).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onDeclinePressed(null);
                    dismiss();
                }
            });
        }


        return rootView;
    }

    public static InviteDialogFragment newInstance(int num, InviteDialogListener listener, Invitation invitation){
        InviteDialogFragment f = new InviteDialogFragment();
        f.setListener(listener);
        f.setInvitation(invitation);
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);
        return f;
    }

    public static InviteDialogFragment newInstance(
            int num, InviteDialogListener listener, String gameName, String hostName){
        InviteDialogFragment f = new InviteDialogFragment();
        f.setListener(listener);
        f.setInvitation(null);
        f.setGameName(gameName);
        f.setHostName(hostName);
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(d_onDestroy)
            Log.e(Tag,"onDestroy");
        bg        = null;
        rootView  = null;
        mListener = null;
    }

}
