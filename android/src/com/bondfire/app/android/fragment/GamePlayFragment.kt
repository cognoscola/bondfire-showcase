package com.bondfire.app.android.fragment

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.CycleInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import com.bondfire.app.Main

import com.bondfire.app.android.R
import com.bondfire.app.android.activity.MainActivity
import com.bondfire.app.android.activity.GamePlayServiceActivity
import com.bondfire.app.android.data.GameInformation
import com.bondfire.app.android.interfaces.OnGameStateChangedListener
import com.bondfire.app.android.managers.GameManager
import kotlinx.android.synthetic.main.activity_main.*

/** This fragment is responsible for display UI which will manage general game state
 * With this fragment one can Pause, resume and exit game, and also launch the leader board
 * These actions happen outside of the libgdx context
 */

class GamePlayFragment : BaseFragment(), View.OnClickListener {

    /** access to the parent view  */
    var rootView: View? = null
        private set
    private var gm: GameManager? = null

    private var buttonResume: ImageView? = null
    private var buttonExit: ImageView? = null
    private var buttonLeaderboard: ImageView? = null
    private var leftSwipeIndicator: ImageView? = null
    private var rightSwipeIndicator: ImageView? = null

    private val firstTime = true

    private val leaderboardId: String? = null

    lateinit internal var mGameStateListener: OnGameStateChangedListener

    private var gamenfo: GameInformation? = null

    var isShowingInterface = false
        private set
    private var handler: Handler? = null
    private val fadeOutRunnable = Runnable {
        leftSwipeIndicator!!.animate().alpha(0.0f).duration = 150
        rightSwipeIndicator!!.animate().alpha(0.0f).duration = 150
    }

    private val cycleInterpolator: CycleInterpolator? = null
    private val listener = object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation) {

        }

        override fun onAnimationEnd(animation: Animation) {

        }

        override fun onAnimationRepeat(animation: Animation) {

        }
    }


    /**Make the ImageView Buttons react when touch  */
    internal var touchListener: View.OnTouchListener = View.OnTouchListener { view, motionEvent ->
        if (motionEvent.action == MotionEvent.ACTION_DOWN) {
            (view as ImageView).drawable.alpha = GLOW_PRESSED
        } else if (motionEvent.action == MotionEvent.ACTION_UP) {
            (view as ImageView).drawable.alpha = GLOW_UNPRESSED
        }
        false
    }

    internal var indicatorTouchListener: View.OnTouchListener = View.OnTouchListener { view, event ->
        if (event.action == MotionEvent.ACTION_DOWN) {
            (view as ImageView).drawable.alpha = GLOW_PRESSED
        } else if (event.action == MotionEvent.ACTION_UP) {
            (view as ImageView).drawable.alpha = GLOW_UNPRESSED
        }
        false
    }


    private fun injectListener(listener: OnGameStateChangedListener) {
        this.mGameStateListener = listener
    }

    private fun injectGameManager(gm: GameManager) {
        this.gm = gm
    }

    override fun getModules(): List<Any>? {
        return null
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            rootView = inflater!!.inflate(R.layout.fragment_game_play, container, false)
        }

        try {
            /**rootView is the frame layout that holds the container for a game  */
            //TODO FIX ISSUE WHERE gm is LOST when pressing on stop
            gm!!.LoadGame(gamenfo!!)

            buttonResume = rootView!!.findViewById(R.id.b_pause) as ImageView
            buttonResume!!.setOnClickListener(this)
            buttonResume!!.setOnTouchListener(touchListener)

            buttonExit = rootView!!.findViewById(R.id.b_exit) as ImageView
            buttonExit!!.setOnClickListener(this)
            buttonExit!!.setOnTouchListener(touchListener)

            buttonLeaderboard = rootView!!.findViewById(R.id.b_leaderboard) as ImageView
            buttonLeaderboard!!.setOnClickListener(this)
            buttonLeaderboard!!.setOnTouchListener(touchListener)

            leftSwipeIndicator = rootView!!.findViewById(R.id.iv_left_swipe_indicator) as ImageView
            leftSwipeIndicator!!.visibility = View.VISIBLE
            leftSwipeIndicator!!.alpha = GLOW_UNPRESSED_FLOAT
            rightSwipeIndicator = rootView!!.findViewById(R.id.iv_right_swipe_indicator) as ImageView
            rightSwipeIndicator!!.visibility = View.VISIBLE
            rightSwipeIndicator!!.alpha = GLOW_UNPRESSED_FLOAT

            handler = Handler()

            (activity as MainActivity).lockableViewPager?.setGamePaused(false)
        } catch (e: NullPointerException) {
            Log.e(Tag, "NullPointer", e)
        }

        return rootView
    }

    fun continueWithGameLogic() {
        gm!!.LoadGame(gamenfo!!)
    }

    fun swapGame(information: GameInformation) {
        gamenfo = information
        gm!!.LoadGame(gamenfo!!)
    }


    override fun onPause() {
        super.onPause()
        gm!!.Pause()
        (activity as MainActivity).lockableViewPager?.setGamePaused(true)
    }

    override fun onResume() {
        super.onResume()
        /** We only want to resume when the game loads up  */
        /*if(firstTime){
            firstTime = false;
            gm.onResume();
        }else{
            gm.RefreshView();
        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()

        leftSwipeIndicator!!.visibility = View.GONE
        rightSwipeIndicator!!.visibility = View.GONE
        if (d_onDestroyView) Log.e(Tag, "onDestroyView()")
        /** Tell the game manager to stop all things game related  */
        gm!!.ExitGame()
        //        mGameStateListener.onDestroyGame();
        rootView = null
        buttonResume =  null
        buttonExit = null
    }

    /** Show the UI when the game pauses  */
    fun showInterface() {

        isShowingInterface = true

        buttonResume!!.visibility = View.VISIBLE
        buttonResume!!.bringToFront()
        buttonResume!!.drawable.alpha = GLOW_UNPRESSED

        buttonExit!!.visibility = View.VISIBLE
        buttonExit!!.bringToFront()
        buttonExit!!.drawable.alpha = GLOW_UNPRESSED

        buttonLeaderboard!!.visibility = View.VISIBLE
        buttonLeaderboard!!.bringToFront()
        buttonLeaderboard!!.drawable.alpha = GLOW_UNPRESSED
    }

    /** on click actions  */
    override fun onClick(view: View) {

        when (view.id) {
            R.id.b_pause ->
                /**Use pressed the resume button so tell the Manager to resume Game Thread
                 * also remove Pause UI   */
                hideInterface()

            R.id.b_exit -> {
                /** Tell the game manager to stop all things game related  */
                gm!!.ExitGame()
                //                MainActivity.blurredImage.setVisibility(View.GONE);
                //                MainActivity.blurredImage.invalidate();
                /** tell the main app to swap the fragments back   */
                mGameStateListener.onDestroyGame()
                GamePlayServiceActivity.clean()
                isShowingInterface = false
            }
            R.id.b_leaderboard -> (activity as GamePlayServiceActivity).getLeaderboardGPGS(gamenfo!!.leaderBoardId)
        }
    }

    fun hideInterface() {
        gm!!.onResume()
        buttonResume!!.visibility = View.GONE
        buttonExit!!.visibility = View.GONE
        buttonLeaderboard!!.visibility = View.GONE
        (activity as MainActivity).lockableViewPager?.setGamePaused(false)
        //                MainActivity.blurredImage.setVisibility(View.GONE);
        //                MainActivity.blurredImage.invalidate();
        isShowingInterface = false
    }

    fun flashIndicators() {
        Log.e(Tag, "flashIndicators: FLASH ")

        leftSwipeIndicator!!.animate().alpha(0.8f).duration = 150
        rightSwipeIndicator!!.animate().alpha(0.8f).duration = 150
        handler!!.postDelayed(fadeOutRunnable, 110)
    }

    companion object {

        private val Tag = "GamePlayFragment"
        private val d_onDestroyView = true

        private val GLOW_PRESSED = 250
        private val GLOW_UNPRESSED = 100
        private val GLOW_PRESSED_FLOAT = 1f
        private val GLOW_UNPRESSED_FLOAT = 0f

        fun newInstance(num: Int, gm: GameManager, listener: OnGameStateChangedListener, gameInfo: GameInformation): GamePlayFragment {
            val f = GamePlayFragment()
            f.gamenfo = gameInfo
            f.injectGameManager(gm)
            f.injectListener(listener)
            val args = Bundle()
            args.putInt("num", num)
            f.arguments = args
            return f
        }
    }
}