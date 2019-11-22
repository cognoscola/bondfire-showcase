package com.bondfire.app.android.fragment

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView

import com.andexert.library.RippleView
import com.bondfire.app.R
import com.bondfire.app.android.activity.GamePlayServiceActivity
import com.bondfire.app.android.activity.MainActivity
import com.bondfire.app.android.statics.Defines

class ConnectionsFragment : BaseFragment(), View.OnClickListener {

    /************** INJECT  */
    //    @Inject ChatListModel mChatListModel;
    //    FragmentStateListener mListener;

    //Views
    internal var rootView: View? = null
    internal var sSendTo: Spinner? = null
    private val mConversationArrayAdapter: ArrayAdapter<String>? = null
    private val lvConversationView: ListView? = null

    private val OnNotice: BroadcastReceiver? = null

    private var mDataView: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (d_onCreate) Log.e(Tag, "OnCreate()")
    }

    override fun onPause() {
        super.onPause()
        activity?.getSharedPreferences(Defines.CHAT_PREFERENCES, Activity.MODE_PRIVATE)?.edit()?.putString("CURRENT_ACTIVE", "")?.commit()
        if (d_onPause) Log.e("OnPause", "Conversation On Pause")
        OnNotice?.let {
            LocalBroadcastManager.getInstance(activity as Context).unregisterReceiver(OnNotice)
        }

    }

    override fun onResume() {
        super.onResume()
        activity?.getSharedPreferences(Defines.CHAT_PREFERENCES, Activity.MODE_PRIVATE)?.edit()?.putString("CURRENT_ACTIVE", "chat")?.commit()
        if (d_onresume) Log.e("OnPause", "Conversation onResume")
        OnNotice?.let {
            LocalBroadcastManager.getInstance(activity as Context).registerReceiver(OnNotice, IntentFilter("ChatMsg"))
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // return super.onCreateView(inflater, container, savedInstanceState);
        if (rootView == null) {
            rootView = inflater!!.inflate(R.layout.fragment_social_no_party, null)

            /*   Button findGame = (Button)rootView.findViewById(R.id.b_findGame);
            findGame.setOnClickListener(this);

            Button inbox = (Button)rootView.findViewById(R.id.b_inbox);
            inbox.setOnClickListener(this);

            Button quickMatch = (Button)rootView.findViewById(R.id.b_quickMatch);
            quickMatch.setOnClickListener(this);*/

            val cancel = rootView!!.findViewById(R.id.b_onCancelClicked) as Button
            cancel.setOnClickListener(this)

            val leave = rootView!!.findViewById(R.id.b_onLeave) as Button
            leave.setOnClickListener(this)

            val finish = rootView!!.findViewById(R.id.b_finish) as Button
            finish.setOnClickListener(this)

            val done = rootView!!.findViewById(R.id.b_done) as Button
            done.setOnClickListener(this)

            mDataView = rootView!!.findViewById(R.id.et_data) as EditText

            val counter = rootView!!.findViewById(R.id.tvTurnCount) as TextView
            val player = rootView!!.findViewById(R.id.tvCurrentPlayer) as TextView

            val llmatchup = rootView!!.findViewById(R.id.matchup_layout) as LinearLayout
            val gameplay = rootView!!.findViewById(R.id.gameplay_layout) as LinearLayout

            (activity as GamePlayServiceActivity).networkManager.turnManager.injectParticipantAdapter(counter, mDataView, player, llmatchup, gameplay)

            /*  lvConversationView = (ListView)rootView.findViewById(R.id.conversationHistory);
         //   mConversationArrayAdapter = new ArrayAdapter<String>( getActivity(), R.layout.conversation_listview_entry);
         //   lvConversationView.setParticipantAdapter(mConversationArrayAdapter);
            if(d_onCreateView)Log.e(Tag+"OnCreateView()","Inflated (Conversation) View Exit");

//            mListener.onAttachFinish(getInterface(), MainActivity.State_Social);
            configureBroadCastReceiver();
            mChatListModel.InjectView(lvConversationView);
            mChatListModel.InjectContext(getActivity());*/
        }

        /*     Calendar cal = Calendar.getInstance();
        int dayofWeek = cal.get(Calendar.DAY_OF_WEEK);
        String quote = "";

        switch (dayofWeek){
            case 0: quote = "A simple smile can brighten even the darkest days! Smile! =)"; break;
            case 1: quote = "Life begins at the end of your comfort zone! Have the courage to face " +
                    "those things which make you uncomfortable. You won't regret it!"; break;
            case 2: quote = "Have the courage to find and pursue your passion because once you do, all areas " +
                    "of your life improve!"; break;
            case 3:  quote = "Don't compare yourself to anybody! Being free from constantly seeking validation from others is vital to knowing " +
                    "yourself and hence what you are really passionate about"; break;
            case 4: quote = "Never let fear consume you. We overcome fear be doing the thing we are mostly afraid of.\n     -Jerome Jarre"; break;
            case 5: quote = "Imagine you are 99 years old and on your deathbed. Suddenly you have the opportunity to come back to " +
                    "right now. What Would you do? Whatever you answer is what you should be doing. -Chris Charmicael"; break;
            case 6: quote = "Failure is the passage to success."; break;
        }

        ((TextView)rootView.findViewById(R.id.tvContent)).setText(quote);*/

        val newParty = rootView!!.findViewById(R.id.b_create_party) as RippleView
        newParty.rippleDuration = 150
        newParty.rippleAlpha = 150
        newParty.setOnRippleCompleteListener {
            (activity as GamePlayServiceActivity).invitePlayersToRoom()
            (activity as GamePlayServiceActivity).submitEvent(
                    GamePlayServiceActivity.decryptString((activity as GamePlayServiceActivity).resources.getString(R.string.event_client_invited_people)), 1)
        }

        val seeInvitations = rootView!!.findViewById(R.id.b_see_invites) as RippleView
        seeInvitations.rippleDuration = 150
        seeInvitations.rippleAlpha = 150
        seeInvitations.setOnRippleCompleteListener {
            (activity as GamePlayServiceActivity).seeInvitations()
            (activity as GamePlayServiceActivity).submitEvent(

                    GamePlayServiceActivity.decryptString((activity as GamePlayServiceActivity).resources.getString(R.string.event_client_checked_invitations)), 1)
        }

        val automatch = rootView!!.findViewById(R.id.b_automatch) as RippleView
        automatch.rippleDuration = 150
        automatch.rippleAlpha = 150
        automatch.setOnRippleCompleteListener {
            (activity as GamePlayServiceActivity).automatch()
            (activity as MainActivity).showProgressBarinActionBar()
            automatch.isEnabled = false

            (activity as GamePlayServiceActivity).submitEvent(

                    GamePlayServiceActivity.decryptString((activity as GamePlayServiceActivity).resources.getString(R.string.event_client_automatched)), 1)

            //prevent user from spamming this button
            Handler().postDelayed({ automatch.isEnabled = true }, 3000)
        }
        return rootView
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
    }
   /* override fun onAttach(activity: Activity) {
        super.onAttach(activity)

        *//*   try{
            mListener = (FragmentStateListener) activity;
        }catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }*//*
    }*/


    override fun getModules(): List<Any>? {
        return null// return Arrays.<Object>asList(new ChatListModule());
    }

    override fun onClick(v: View) {
        when (v.id) {
        /* case R.id.b_findGame:
                ( (PlayServiceGameActivity) getActivity()).onFindPlayersClicked();
                break;

            case R.id.b_inbox:
                ( (PlayServiceGameActivity) getActivity()).onInboxPressed();
                break;

            case R.id.b_quickMatch:
                ( (PlayServiceGameActivity) getActivity()).onQuickMatch();
                break;*/

            R.id.b_onCancelClicked -> (activity as GamePlayServiceActivity).onCancelClicked()

            R.id.b_onLeave -> (activity as GamePlayServiceActivity).onLeaveClicked()

            R.id.b_finish -> (activity as GamePlayServiceActivity).onFinishedClicked()

            R.id.b_done -> (activity as GamePlayServiceActivity).onDoneClicked(mDataView!!.text.toString())
        }
    }

    companion object {

        private val Tag = "ConversationViewFrg "

        private val d_onCreateView = false
        private val d_onCreate = false
        private val d_onMessagedReceived = false
        private val d_interfaceUpdater_general = false
        private val d_onPause = false
        private val d_onresume = false

        fun newInstance(): ConnectionsFragment {
            return ConnectionsFragment()
        }
    }
}
