package com.bondfire.app.android.fragment

import android.content.res.Resources
import android.os.Bundle
import androidx.core.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView

import com.andexert.library.RippleView
import com.bondfire.app.android.R
import com.bondfire.app.android.activity.GamePlayServiceActivity
import com.bondfire.app.android.adapter.RealTimeChatAdapter
import com.bondfire.app.android.adapter.RealTimeParticipantAdapter
import com.bondfire.app.android.network.realtime.RealTimeManager
import kotlinx.android.synthetic.main.action_bar.*

/**
 * Created by alvaregd on 17/02/16.
 * Fragment that holds Real Time Room Information
 */
class RealTimeRoomFragment : Fragment() {

    internal var rootView: View? = null
    internal var mParticipantAdapter: RealTimeParticipantAdapter? = null
    internal var mChatAdapter: RealTimeChatAdapter? = null

    internal var realTimeManager: RealTimeManager? = null

    lateinit internal var lvParticipants: ListView
    lateinit internal var lvChat: ListView
    lateinit internal var title: TextView
    lateinit internal var subTitle: TextView
    lateinit internal var res: Resources

    lateinit internal var participantImage: ImageView
    lateinit var chatImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (d_onCreate) Log.e(TAG, "OnCreate()")
    }

    override fun onPause() {
        if (d_onPause) Log.e(TAG, "onPause()")
        super.onPause()
    }

    override fun onResume() {
        if (d_onresume) Log.e(TAG, "onResume()")
        super.onResume()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // return super.onCreateView(inflater, container, savedInstanceState);
        if (rootView == null) {
            rootView = inflater!!.inflate(R.layout.fragment_real_time_room, container, false)
        } else {
            Log.e(TAG, "onCreateView() rootView not Null")
        }

        res = resources

        realTimeManager = (activity as GamePlayServiceActivity).networkManager.realTimeManager

        //if we already have existing adapters
        mChatAdapter = realTimeManager!!.chatAdapter
        mParticipantAdapter = realTimeManager!!.participantAdapter

        if (mParticipantAdapter == null) {
            mParticipantAdapter = RealTimeParticipantAdapter(activity)
            realTimeManager!!.participantAdapter = mParticipantAdapter
        } else {
            if (d_onCreateView) Log.i(TAG, "onCreateView() got participant adapter from service")
        }

        if (mChatAdapter == null) {
            mChatAdapter = RealTimeChatAdapter(activity)
            realTimeManager!!.chatAdapter = mChatAdapter
        } else {
            if (d_onCreateView) Log.i(TAG, "onCreateView() got chat adapter from service")
        }

        lvChat = rootView!!.findViewById(R.id.lv_chats) as ListView
        lvChat.adapter = mChatAdapter

        lvParticipants = rootView!!.findViewById(R.id.lv_room_participants) as ListView
        lvParticipants.adapter = mParticipantAdapter

        title = rootView!!.findViewById(R.id.tv_room_title) as TextView
        subTitle = rootView!!.findViewById(R.id.tv_win_title) as TextView

        mChatAdapter!!.setNotification(rootView!!.findViewById(R.id.messagesAlert) as ImageView)
        mParticipantAdapter!!.setNotification(rootView!!.findViewById(R.id.participantsAlert) as ImageView)

        participantImage = rootView!!.findViewById(R.id.participantImage) as ImageView
        chatImage = rootView!!.findViewById(R.id.chatImage) as ImageView

        //set initial state
        if (realTimeManager!!.viewState == VIEW_PARTICIPANT) {
            showParticipantControls()
        } else {
            showChatControls()
        }

        val room = rootView!!.findViewById(R.id.b_real_time_participants) as RippleView
        room.rippleDuration = 75
        room.rippleAlpha = 150
        room.setOnRippleCompleteListener { showParticipantControls() }

        val messages = rootView!!.findViewById(R.id.b_real_time_messages) as RippleView
        messages.rippleDuration = 75
        messages.rippleAlpha = 150
        messages.setOnRippleCompleteListener { showChatControls() }

        realTimeManager!!.updateRoomFragment(null)
        return rootView
    }

    private fun showParticipantControls() {
        //set the visibility of the controls
        lvChat.visibility = View.GONE
        lvParticipants.visibility = View.VISIBLE
        subTitle.visibility = View.VISIBLE
        title.text = res.getString(R.string.social_title_Participants)
        //change the actionbar UI
        activity?.actionBarView?.hideChatControls()
        realTimeManager!!.saveViewState(VIEW_PARTICIPANT)
        //update the notification icons
        mChatAdapter!!.isNoLongerViewingSection()
        mParticipantAdapter!!.setNotificationInvisibile()
        chatImage.drawable.alpha = 80
        participantImage.drawable.alpha = 255
    }

    private fun showChatControls() {
        //set the visibility of the controls
        lvParticipants.visibility = View.GONE
        lvChat.visibility = View.VISIBLE
        subTitle.visibility = View.INVISIBLE
        title.text = res.getString(R.string.social_title_chat)
        //change the actionbar UI
        activity?.actionBarView?.showChatControls()
        realTimeManager!!.saveViewState(VIEW_CHAT)
        //update the notification icons
        mChatAdapter!!.setNotificationInvisibile()
        mParticipantAdapter!!.isNoLongerViewingSection()
        chatImage.drawable.alpha = 255
        participantImage.drawable.alpha = 80
    }


    override fun onDestroy() {
        super.onDestroy()
        realTimeManager = null
    }


    companion object {

        private val TAG = RealTimeRoomFragment::class.java.name
        private val d_onCreateView = true
        private val d_onCreate = false
        private val d_onPause = false
        private val d_onresume = false
        private val d_newInstance = false

        val VIEW_CHAT = 1
        val VIEW_PARTICIPANT = 0

        fun newInstance(num: Int): RealTimeRoomFragment {
            if (d_newInstance) Log.e(TAG, "newInstance()")
            val fragment = RealTimeRoomFragment()
            val args = Bundle()
            args.putInt("num", num)
            fragment.arguments = args
            return fragment
        }
    }

}
