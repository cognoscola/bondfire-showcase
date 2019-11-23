package com.bondfire.app.android.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.PagerAdapter

import com.bondfire.app.R
import com.bondfire.app.android.adapter.MainViewPagerAdapter
import com.bondfire.app.android.data.GameInformation
import com.bondfire.app.android.fragment.*
import com.bondfire.app.android.managers.GameManager
import com.bondfire.app.android.network.realtime.RealTimeManager
import com.bondfire.app.android.network.turnbasedmultiplayer.TurnBasedMultiplayerManager
import com.bondfire.app.android.view.ActionBarSocialView
import com.bondfire.app.android.view.ActionBarGameView
import com.bondfire.app.android.view.ActionBarProfileView
import com.bondfire.app.android.view.TabViewBase
//import com.google.android.gms.ads.AdRequest
//import com.google.android.gms.ads.AdView
import com.google.android.gms.games.GamesActivityResultCodes
import kotlinx.android.synthetic.main.action_bar.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.notification_tabs.*

class MainActivity : GamePlayServiceActivity(), ChildFragmentListener {

    var childFragment: Fragment? = null
    internal lateinit var mActionBarProfileView: ActionBarProfileView
    internal lateinit var mActionBarGameView: ActionBarGameView
    internal lateinit var mActionBarSocialView: ActionBarSocialView
//    private var mAdview: AdView? = null

    //helps control behaviour of app when pressing back button
    var isChildFragment: Boolean = false

    /** Game Stuff  */
    var mGameManager: GameManager? = null

    /***********************************************************************************************
     * ACTIVITY LIFE CYCLE CALLS
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (d_onCreate) Log.e(TAG, "onCreate () Enter")

        /** Configure the different views in our activity  */
        configureActionBar()
        configureViewPager()
        //        configureSideBar();
        configureGameManager(-1)
        configureAds()
        configureNetwork()


        val intent = intent
        if (intent != null) {
            checkIntentForGames(intent)
        }

    }


    override fun onStop() {
        if (d_onstop) Log.e(TAG + "onStop()", "on stop Enter")
        super.onStop()
    }

    override fun onStart() {
        if (d_onstart) Log.e(TAG + "onStart()", "onStart Enter")
        super.onStart()
        mSectionsPagerAdapter!!.checkPendingFragments()
    }

    override fun onResume() {
        super.onResume()
        if (d_onResume) Log.e(TAG, "onResume()")
//        mAdview!!.resume()
    }

    override fun onPause() {
        if (d_onPause) Log.e(TAG + "onPause()", "ENter")
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (d_onSavedInstanceState) Log.e(TAG, "onSaveInstanceState()" + "Enter")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (d_onDestroy) Log.e(TAG, "onDestroy()" + "Enter")
        networkManager.onDestroy()
        mGameManager = null
        mSectionsPagerAdapter!!.clean()
        mSectionsPagerAdapter = null

    }

    /***********************************************************************************************
     * CONFIGURATION CALLS
     */
    /**
     * Configure the ActionBar (bottom bar). For each of the 3 tabs,
     * we inflate their respectives actionbar viiews, add it to the ActionBar View
     * Then displayViewByState will display the correct view given the pager index.
     */
    private fun configureActionBar() {

        mActionBarProfileView = ActionBarProfileView.inflateBar(this, actionBarView)
        mActionBarGameView = ActionBarGameView.inflateBar(this, actionBarView)
        mActionBarSocialView = ActionBarSocialView.inflateBar(this, actionBarView)
        actionBarView?.AddViewBar(mActionBarProfileView)
        actionBarView?.AddViewBar(mActionBarGameView)
        actionBarView?.AddViewBar(mActionBarSocialView)
        actionBarView?.displayViewByState(selectedTab)
        actionBarView?.configureActionBarButtons()
    }

    /**
     * Configure the side bars. This is similar to the actionBar except we don't replace views.
     * The SideWbar will simply be visible depending on the selected index of the viewpager.
     * At the moment we are not using the sidebars
     */



    private fun configureViewPager() {

        mSectionsPagerAdapter = MainViewPagerAdapter(supportFragmentManager, this)
        lockableViewPager?.offscreenPageLimit = 3
        lockableViewPager?.adapter = mSectionsPagerAdapter as PagerAdapter
        lockableViewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(Position: Int, positionOffset: Float, positionoffsetPixels: Int) {
                if (d_onPageScrolled)
                    Log.e(TAG, "OnPageScrolled() i: $Position v:$positionOffset i2: $positionoffsetPixels")

                mGameManager!!.getController()!!.onPageScrolled(Position, positionOffset, positionoffsetPixels)

                //                ((BondfireGraphicsModifier)mGameManager.getController()).setGraphicsEffectPercent(Position,positionOffset,positionoffsetPixels);

                if (mGameManager!!.isLoaded && mGameManager!!.getController() != null) {

                    // set behaviour of blur as viewpager is scrolled
                    //set behaviour of swipe indicator as viewpager is scrolled
                    if (Position == 2) {
                        mGameManager!!.getController()!!.setGraphicsEffectPercent(1f)
                    } else if (Position == 1) {
                        mGameManager!!.getController()!!.setGraphicsEffectPercent(positionOffset)
                    } else if (Position == 0) {
                        mGameManager!!.getController()!!.setGraphicsEffectPercent(1f - positionOffset)
                    }
                }
            }

            override fun onPageSelected(i: Int) {
                if (d_configureViewPager) Log.e(TAG, "onPageSelected()" + " Selected: " + i)
                try {
                    //                    supportInvalidateOptionsMenu();
                    actionBarView!!.displayViewByState(i)
                    activityTabRoot?.setAlphas(i)
                    selectedTab = i
                    activityTabRoot?.CancelAlert(i)
                    lockableViewPager.invalidate()
                } catch (e: NullPointerException) {
                    Log.e(TAG, "onPageSelected()", e)
                }

            }

            override fun onPageScrollStateChanged(i: Int) {
                try {
                    if (mGameManager != null) {
                        if (d_configureViewPager) Log.e(TAG, "onPageScrollStateChanged() " + i)
                        if (i == ViewPager.SCROLL_STATE_IDLE) {
                            if (d_ViewPagerStates)
                                println("ViewPage: IDLE, Current Item: " + lockableViewPager?.currentItem)
                            if (lockableViewPager?.currentItem == 1 && !mGameManager!!.isShowingInterface) {
                                if (d_ViewPagerStates)
                                    println("Set Pause False in idle")
                                lockableViewPager?.setGamePaused(false)
                                mGameManager!!.flashIndicators()
                            }
                        }

                        if (i == ViewPager.SCROLL_STATE_DRAGGING) {
                            if (d_ViewPagerStates)
                                println("ViewPage: DRAGGING, Current Item: " + lockableViewPager?.currentItem)
                            if (lockableViewPager?.currentItem == 1) {
                                if (d_ViewPagerStates)
                                    println("Set Pause true in Dragging")
                                lockableViewPager?.setGamePaused(true)
                            }
                        }

                        if (i == ViewPager.SCROLL_STATE_SETTLING) {
                            if (d_ViewPagerStates)
                                println("ViewPage: SETTLING  Current Item: " + lockableViewPager?.currentItem)
                            if (lockableViewPager?.currentItem == 1 && !mGameManager!!.isShowingInterface) {
                                if (d_ViewPagerStates)
                                    println("Set Pause False in settling")
                                lockableViewPager?.setGamePaused(false)
                            }
                            lockableViewPager?.invalidate()
                        }
                    }
                } catch (e: NullPointerException) {
                    Log.e(TAG, "Manager is null", e)
                }

            }
        })
        /** Set the visible page when we first go ino the app  */
        this.setPageView(State_Game)
    }

    fun configureAds() {
        /**DISABLED ADS ON THIS VERSION */
       /*
        mAdview = findViewById(R.id.adView) as AdView
        val adRequest = AdRequest.Builder().addTestDevice("YOUR_DEVICE_HASH").build()
        mAdview!!.loadAd(adRequest)*/
    }

    /***
     * Here we configure everything game related stuff
     */
    fun configureGameManager(gameId: Int) {

        if (d_configureGameManager) Log.e(TAG, "configureGameManager() enter")
        if (mGameManager == null) {
            if (d_configureGameManager) Log.e(TAG, "configureGameManager() Manager is null")

            if (gameId == -1) {
                if (d_configureGameManager)
                    Log.e(TAG, "configureGameManager() Starting with Id -1")
                mGameManager = GameManager(this, R.id.container, false)
            } else {
                if (d_configureGameManager)
                    Log.e(TAG, "configureGameManager() Loading game: " + gameId)
                mSectionsPagerAdapter!!.replaceGameFragmentWithPlay(getGameInformation(gameId))
                mGameManager = GameManager(this, R.id.container, true)
            }
        } else {
            if (d_configureGameManager) Log.e(TAG, "configureGameManager() Manager !null")
            if (gameId != -1) {
                if (d_configureGameManager) {
                    Log.e(TAG, "configureGameManager() Loading game: " + gameId)
                    mSectionsPagerAdapter!!.replaceGameFragmentWithPlay(getGameInformation(gameId))
                }
            }
        }
    }

    fun openCustomMenu(anchor: View) {
        val popMenu = PopupMenu(this, anchor)
        popMenu.inflate(R.menu.games_view_menu)
        popMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_settings -> {
                    /** Hide the UI  */
                    /** Hide the UI  */
                    hideMainInterface()

                    /** change our fragment padding */

                    /** change our fragment padding */
                    childFragment = PreferenceFragment.newInstance()
                    val trans = supportFragmentManager.beginTransaction()
                    trans.add(R.id.childContainer, childFragment as PreferenceFragment).addToBackStack(null)
                    trans.commit()
                    isChildFragment = true
                    setGameBlurPercent(1f)
                }

                R.id.action_leaveGroup -> {
                    networkManager.realTimeManager.leaveRoom()
                    actionBarView!!.hideChatControls()
                    submitEvent(
                            GamePlayServiceActivity.decryptString(resources.getString(R.string.event_user_left_room_from_the_context_menu)), 1)
                }
            }
            false
        }

        when (lockableViewPager?.currentItem) {
            State_Game -> {
                popMenu.menu.findItem(R.id.action_settings).isVisible = true
                popMenu.menu.findItem(R.id.action_leaveGroup).isVisible = false
            }
            State_Social -> {
                popMenu.menu.findItem(R.id.action_leaveGroup).isVisible = networkManager.isRoomConnected
                popMenu.menu.findItem(R.id.action_settings).isVisible = true
            }
            State_Cloud -> {
                popMenu.menu.findItem(R.id.action_bar).isVisible = false
                popMenu.menu.findItem(R.id.action_leaveGroup).isVisible = false
            }
        }
        popMenu.show()
    }

    private fun hideMainInterface() {
        lockableViewPager?.visibility = View.INVISIBLE
        activityTabRoot?.visibility = View.INVISIBLE
        actionBarView!!.visibility = View.INVISIBLE
//        mAdview!!.visibility = View.INVISIBLE
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == TurnBasedMultiplayerManager.RC_LOOK_AT_MATCHES) {


            if (resultCode != Activity.RESULT_OK) {// user canceled
                actionBarView.hideProgressBar()
                return
            }
            try {
                (mGameManager!!.controllFragment as GamePlayFragment).hideInterface()
            } catch (e: NullPointerException) {
                Log.e(TAG, "controllFragment is null because there is no game yet, not worries", e)
            }

            networkManager.turnManager.handleLookAtMatches(data)
        } else if (requestCode == TurnBasedMultiplayerManager.RC_SELECT_PLAYERS) {
            if (resultCode != Activity.RESULT_OK) {
                actionBarView.hideProgressBar()
                return
            }
            try {
                (mGameManager?.controllFragment as GamePlayFragment).hideInterface()
            } catch (e: NullPointerException) {
            }

            networkManager.turnManager.handleSelectPlayers(data)
        } else if (requestCode == RealTimeManager.RC_SELECT_PLAYERS) {
            if (resultCode != Activity.RESULT_OK) {
                actionBarView.hideProgressBar()
                return
            }
            try {
                (mGameManager?.controllFragment as GamePlayFragment).hideInterface()

            } catch (e: NullPointerException) {
                Log.e(TAG, "controlFragment is null because there is no game yet, not worries", e)
            }

            networkManager.handleRealTimePlayerInviteResult(data)
            actionBarView.showProgressBar()
        } else if (requestCode == RealTimeManager.RC_INVITATION_INBOX) {

            if (resultCode != Activity.RESULT_OK) {
                actionBarView.hideProgressBar()
                return
            }
            try {
                (mGameManager?.controllFragment as GamePlayFragment).hideInterface()
            } catch (e: NullPointerException) {

            }
            networkManager.handleRealTimeReceiveInviteInboxResult(data)
            actionBarView.showProgressBar()
        }
        //TODO DISABLED BILLING
        /*else if (billingHelper.handleActivityResult(requestCode, resultCode, data)) {
            Log.d(TAG, "onActivityResult handled by IABUtil.")
        }*/ else if (resultCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED && requestCode == LEADERBOARD_CODE) {
            networkManager.disconnect()
        } else {
            networkManager.handleApiConnectivity(requestCode, resultCode, data)
        }

        //return from log on attempt
        //return from request to invite players to real time room
        //return from request to invite players to real time room
        //Returning from select player dialog

        super.onActivityResult(requestCode, resultCode, data)

        if (childFragment is PreferenceFragment) {
            childFragment?.onActivityResult(requestCode, resultCode, data)
        }

        when (requestCode) {

        }
    }

    /**
     * Check Intent for Games
     * @param intent
     */
    private fun checkIntentForGames(intent: Intent?) {
        if (d_checkIntentForGames) Log.i(TAG, "checkIntentForGames() ")
        if (intent != null) {
            if (intent.action != null) {
                if (intent.action == RealTimeManager.ACTION_JOIN) {
                    if (intent.hasExtra(RealTimeManager.EXTRA_GAMEID)) {
                        val gameId = intent.getIntExtra(RealTimeManager.EXTRA_GAMEID, -1)
                        if (gameId > -1) {
                            if (d_checkIntentForGames) Log.i(TAG, "Starting game: " + gameId)

                            //instead of waiting for the app to load, we'll just set a flag for the
                            //network manager to load the game
                            networkManager.setSkipGameToMultiplayer(gameId)
                        }
                    }
                }
            }
        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (d_onNewIntent) Log.i(TAG, "onNewIntent() onNewIntent")
        checkIntentForGames(intent)
        setIntent(intent)
    }

    override fun onBackPressed() {
        if (d_onBackpressed) Log.e(TAG, "onBackPressed()")

        if (isChildFragment) {
            if (childFragment is PreferenceFragment) {
                isChildFragment = false
                setGameBlurPercent(0f)
            }

            if (childFragment is StoreFragment) {
                isChildFragment = false
            }
            super.onBackPressed()
        } else {
            if (mSectionsPagerAdapter!!.isExitable(selectedTab)) {
                super.onBackPressed()
            } else {
                setPageView(1)
            }
        }
    }

    /**********************************************************************************************
     * FRAGMENT CALLS
     */
    override fun onInstanceShown(fragment: Fragment) {
        this.childFragment = fragment
    }



    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        if (d_onAttachFragment)
            Log.e(TAG, "onAttachFragment() " + "onAttachFragment()")
    }

    fun setPageView(index: Int) {
        try {
            lockableViewPager?.currentItem = index
            selectedTab = index
            activityTabRoot?.setAlphas(index)
        } catch (e: NullPointerException) {
            Log.e(TAG, "setPageView()", e)
        }

    }

    /**
     * Switch to the store fragment
     */
    fun showStoreFragment() {
        hideMainInterface()

        childFragment = StoreFragment.newInstance()
        val trans = supportFragmentManager.beginTransaction()
        trans.add(R.id.childContainer, childFragment!!).addToBackStack(null)
        trans.commit()
        isChildFragment = true
        setGameBlurPercent(1f)
    }

    //sets notification on the social tab
    fun setSocialNotificationOnTabs(tab: Int) {
        if (tab == 2) {
            if (lockableViewPager?.currentItem != 2) {
                activityTabRoot?.showSocialAlart()
            }
        }
    }

    /*fun getmAdview(): AdView {
//        return mAdview!!
    }*/

    fun getmSectionsPagerAdapter(): MainViewPagerAdapter {
        return mSectionsPagerAdapter!!
    }

    companion object {

        /**********************
         * DEBUG
         */
        private val TAG = "Main Activity "
        private val d_onPageScrolled = false
        private val d_configureViewPager = false
        private val d_onAttachFragment = false
        private val d_onCreate = false
        private val d_onDestroy = true
        private val d_onstop = true
        private val d_onstart = false
        private val d_onPause = false
        private val d_onResume = false
        private val d_onSavedInstanceState = false
        private val d_onactivityresult = false
        private val d_ViewPagerStates = false
        private val d_configureGameManager = true
        private val d_onBackpressed = false
        private val d_ShowMainInterface = true
        private val d_onNewIntent = true
        private val d_checkIntentForGames = true
        /**
         * DEFINITIONS
         */
        val State_Cloud = 0
        val State_Game = 1
        val State_Social = 2

        /********************************************************************************************
         * GETTERS AND SETTERS
         */


        //View SideBarLayoutLeft; //For displaying purposes only
        //View SideBarLayoutRight; //for display purposes only


        /** Adapters */
        private var mSectionsPagerAdapter: MainViewPagerAdapter? = null

        /***********
         * Activity  Variables
         */
        internal var selectedTab: Int = 0




        /**********************************************************************************************
         * ACTIVITY ACTIONS //TODO MOVE THIS SHIT INTO THE FRAGMENTMANAGER
         */
        fun getGameInformation(gameId: Int): GameInformation {

            //TODO MAKE GAME INFO FETACHBLE FROM A SQL DB that sync from the network

            //but for now... just fetch data from the
            val info = GameInformation(
                    GameGridViewFragment.GAME_TITLES[gameId],
                    GameGridViewFragment.GAME_LEADERBOARDS[gameId],
                    GameGridViewFragment.GAME_ICONS[gameId],
                    GameGridViewFragment.GAME_ID[gameId],
                    GameGridViewFragment.GAME_MINPLAYERS[gameId],
                    GameGridViewFragment.GAME_MAXPPLAYER[gameId])

            info.usesAdvertisementServices = GameGridViewFragment.USES_ADS[gameId]
            info.usesLeaderBoardServices = GameGridViewFragment.USES_LeaderBoard[gameId]
            info.usesTurnBasedMultiplayerService = GameGridViewFragment.USES_TBMP[gameId]
            info.usesRealTimeMultiplayerServices = GameGridViewFragment.USES_RTMP[gameId]
            info.usesDayTimer = GameGridViewFragment.USES_DAY_CLOCK[gameId]
            return info
        }



        fun showNoRealTimeRoomFragment() {
            try {
                mSectionsPagerAdapter!!.showDefaultSocialFragment()
            } catch (e: NullPointerException) {
                Log.e(TAG, "showNoRealTimeRoomFragment", e)
            }

        }
    }

    fun showRealTimeRoomFragment() {
        try {
            actionBarView!!.hideProgressBar()
            mSectionsPagerAdapter!!.showRealTimeRoom()
        } catch (e: NullPointerException) {
            Log.e(TAG, "showRealTimeRoomFragment: ", e)
        }

    }

    fun showMainInterface() {
        if (d_ShowMainInterface) Log.i(TAG, "showMainInterface() ")
        lockableViewPager?.visibility = View.VISIBLE
        activityTabRoot?.visibility = View.VISIBLE
        actionBarView?.visibility = View.VISIBLE
    }

    private fun setGameBlurPercent(value: Float) {
        if (mGameManager?.isLoaded ?: false && mGameManager?.getController() != null) {
            mGameManager?.getController()?.setGraphicsEffectPercent(value)
        }
    }

    public fun showProgressBarinActionBar(){
        actionBarView.showProgressBar()
    }
}



