package com.bondfire.app.android.managers

import android.content.Context
import androidx.core.app.Fragment
import androidx.core.app.FragmentActivity
import android.util.Log

import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import com.bondfire.app.android.activity.MainActivity
import com.bondfire.app.android.data.GameInformation
import com.bondfire.app.android.network.realtime.BondfireMessage
import com.bondfire.app.services.AdController
import com.bondfire.app.services.PlayServicesObject
import com.bondfire.app.android.fragment.BackgroundFragment
import com.bondfire.app.android.fragment.GamePlayFragment
import com.bondfire.app.android.fragment.LibgdxContainerFragment
import com.bondfire.app.bfUtils.BondfireGraphicsModifier
import kotlinx.android.synthetic.main.activity_main.*

/** This class is responsible for managing the state of the LibGdx context
 * When we are running a game, this class handles actions when the game is Paused,
 * Running or Stopped.
 * The app also handles loading and unloading of games and manages the Libgdx fragment
 * This class is referenced whenever the app requires action on the game.
 */
class GameManager(internal var context: Context, private val containerId: Int, AutoStartGame: Boolean) {

    internal enum class GameState {
        Running,
        Paused,
        Stopped
    }

    internal var mState = GameState.Stopped
    var controllFragment: Fragment? = null
    internal var mContainer: AndroidFragmentApplication? = null
    internal var mPlayServicesObject: PlayServicesObject? = null
    internal var controller: AdController? = null
    var information: GameInformation? = null

    var userPaused = false

    init {
        if (d_constructor) Log.e(TAG, "GameManager() construct, setting Background Fragmeng")

        if (!AutoStartGame) {
            mContainer = BackgroundFragment.newInstance()
            val trans = (context as FragmentActivity).supportFragmentManager.beginTransaction()
            trans.replace(containerId, mContainer)
            trans.commit()
        }
    }

    fun injectController(controller: Fragment) {
        if (d_injectConroller) Log.e(TAG, "injectController")
        controllFragment = controller
    }

    fun LoadGame(information: GameInformation) {
        if (d_LoadGame) Log.e(TAG, "Load Game() Enter")

        // start the game fragment if its null
        if (mContainer !is LibgdxContainerFragment) {
            createLibgdxFragment(information)
        } else {

            if (d_LoadGame) Log.i(TAG, "LoadGame() IN ID " + information.gameId)
            if (d_LoadGame) Log.i(TAG, "LoadGame() COMPARE ID " + (mContainer as LibgdxContainerFragment).gameInfo.gameId)

            //we have a game already loaded, check if we must load a new game or not
            if (information.gameId != (mContainer as LibgdxContainerFragment).gameInfo.gameId) {
                if (d_LoadGame) Log.i(TAG, "LoadGame() Need to load a different Game")

                //resume the game
                createLibgdxFragment(information)

            } else {
                if (d_LoadGame) Log.i(TAG, "LoadGame() Already have the game Loaded")
            }
        }
    }

    fun createLibgdxFragment(information: GameInformation) {
        this.information = information
        mContainer?.onResume()
        mContainer = LibgdxContainerFragment.newInstance(information)
        val trans = (context as FragmentActivity).supportFragmentManager.beginTransaction()
        trans.replace(containerId, mContainer)
        trans.commit()
        mState = GameState.Running
        (context as MainActivity).networkManager.realTimeManager.broadcastClientStatus(BondfireMessage.STATUS_GAME)
    }

    fun Pause() {
        if (d_Pause) Log.e(TAG, "Pause() ,Enter")
        try {
            if (mContainer != null && mContainer is LibgdxContainerFragment) {
                if (!isPaused) {
                    /*if(d_Pause) Log.e(TAG,"Pausing");*/
                    (controllFragment as GamePlayFragment).showInterface()
                    (context as MainActivity).lockableViewPager?.setGamePaused(true)
                    //                mContainer.onPause();
                }
            }
            mState = GameState.Paused
        } catch (e: NullPointerException) {
            Log.e(TAG, "controllerFragment Null", e)
        }

    }

    val isPaused: Boolean
        get() = mState == GameState.Paused || mState == GameState.Stopped

    val isRunning: Boolean
        get() = mState == GameState.Running


    fun onResumeTemp() {
        if (mContainer != null && mContainer is LibgdxContainerFragment) {
            if (isPaused) {
                mContainer!!.onResume()
            }
        }
    }

    fun onResume() {
        if (mContainer != null && mContainer is LibgdxContainerFragment) {
            if (!isRunning) {
                mState = GameState.Running
                mContainer!!.onResume()
                (context as MainActivity).networkManager.realTimeManager.broadcastClientStatus(BondfireMessage.STATUS_GAME)
            }
        }
    }

    fun ExitGame() {

        try {
            if (mContainer != null && mContainer is LibgdxContainerFragment) {
                if (d_ExitGame) Log.e(TAG, "Game is Paused, Exiting... ")
                mContainer!!.onResume()
                mContainer = BackgroundFragment.newInstance()
                val trans = (context as FragmentActivity).supportFragmentManager.beginTransaction()
                trans.replace(containerId, mContainer)
                trans.commit()
                mState = GameState.Stopped
                controllFragment = null
                information = null

                //Tell our client and others that we are going to the lobby
                (context as MainActivity).networkManager.realTimeManager.broadcastClientStatus(BondfireMessage.STATUS_LOBBY)


            }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Try to switch out fragment after   onSaveInstanceState (quitingg)", e)
        }

    }

    val isLoaded: Boolean
        get() = mState != GameState.Stopped && information != null


    fun getController(): BondfireGraphicsModifier? {
        if (mContainer is BondfireGraphicsModifier) {
            return mContainer as BondfireGraphicsModifier?
        } else {
            return null
        }
    }

    val isShowingInterface: Boolean
        get() {
            if (controllFragment != null)
                return (controllFragment as GamePlayFragment).isShowingInterface
            else
                return false
        }

    fun flashIndicators() {
        if (controllFragment != null) {
            if (controllFragment is GamePlayFragment) {
                (controllFragment as GamePlayFragment).flashIndicators()
            }
        }
    }

    companion object {

        private val TAG = GameManager::class.java.name
        private val d_ExitGame = true
        private val d_Pause = true
        private val d_constructor = true
        private val d_LoadGame = true
        private val d_injectConroller = true
    }
}