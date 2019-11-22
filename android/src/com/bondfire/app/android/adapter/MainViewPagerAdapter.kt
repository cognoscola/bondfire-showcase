package com.bondfire.app.android.adapter

import androidx.viewpager.widget.PagerAdapter
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import com.bondfire.app.android.activity.MainActivity
import com.bondfire.app.android.data.GameInformation
import com.bondfire.app.android.fragment.ConnectionsFragment
import com.bondfire.app.android.fragment.GameInstructionFragment
import com.bondfire.app.android.fragment.GamePlayFragment
import com.bondfire.app.android.fragment.GameGridViewFragment
import com.bondfire.app.android.fragment.RealTimeRoomFragment
import com.bondfire.app.android.fragment.TurnBasedRoomFragment
import com.bondfire.app.android.fragment.SocialMediaFragment
import com.bondfire.app.android.fragment.TurnBasedFragment
import com.bondfire.app.android.interfaces.OnGameStateChangedListener
import kotlinx.android.synthetic.main.activity_main.*

class MainViewPagerAdapter(internal var fm:
                           FragmentManager, private val act: MainActivity) : FragmentPagerAdapter(fm), OnGameStateChangedListener {

    private var isOutsideGame = true

    //Fragment at Profile Tab stuff
    private var ParentFragmentAtPos0: Fragment? = null
    private var ParentFragmentAtPos1: Fragment? = null
    private var ParentFragmentAtPos2: Fragment? = null

    private var pendingPos2: Class<*>? = null

    fun checkPendingFragments() {
        if (pendingPos2 == ConnectionsFragment::class.java) {
            showDefaultSocialFragment()
            pendingPos2 = null
        }
    }

    override fun getItem(position: Int): Fragment {

        if (d_getItem) Log.e(TAG, "getItem()" + "Position:" + position)
        when (position) {
            MainActivity.State_Cloud -> {
                if (ParentFragmentAtPos0 == null) {
                    showDefaultInformationFragment()
                }
                return ParentFragmentAtPos0!!
            }
            MainActivity.State_Game -> {
                if (ParentFragmentAtPos1 == null) {
                    showDefaultGameFragment()
                }
                return ParentFragmentAtPos1!!
            }
            MainActivity.State_Social -> {
                if (ParentFragmentAtPos2 == null) {
                    if (d_getItem) Log.e(TAG, "Null Tab Bids, making Default()")
                    showDefaultSocialFragment()
                }
                return ParentFragmentAtPos2!!
            }
            else -> {
                //TODO RETURN AN ERROR FRAGMENT INSTEAD
                if (d_getItem) Log.e(TAG, "DEFAULTED")
                return ConnectionsFragment.newInstance()
            }
        }
    }

    override fun getCount(): Int {
        return NUM_OF_ITEMS
    }

    /**
     * SWITCH TO SPECIFIC FRAGMENTS  *
     */
    fun showDefaultInformationFragment() {
        if (d_showProfileFragment) Log.e(TAG, "ShowProfileFragment()")
        try {
            if (ParentFragmentAtPos0 != null)
                fm.beginTransaction().remove(ParentFragmentAtPos0).commit()
            ParentFragmentAtPos0 = SocialMediaFragment.newInstance(null)
            notifyDataSetChanged()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "showDefaultInformationFragment: Tried to change to main Social media fragment", e)
        }

    }

    fun showDefaultGameFragment() {
        try {
            isOutsideGame = true
            if (ParentFragmentAtPos1 != null)
                fm.beginTransaction().remove(ParentFragmentAtPos1).commit()
            ParentFragmentAtPos1 = GameGridViewFragment.newInstance(0)
            notifyDataSetChanged()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "showDefaultGameFragment: Tried to switch to GameGridViewFragment ", e)
        }

    }

    fun showDefaultSocialFragment() {
        try {
            if (ParentFragmentAtPos2 != null)
                fm.beginTransaction().remove(ParentFragmentAtPos2).commit()
            ParentFragmentAtPos2 = ConnectionsFragment.newInstance()
            notifyDataSetChanged()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "showDefaultSocialFragment: Tried to change To ConnectionsFragment", e)
            pendingPos2 = ConnectionsFragment::class.java
        }

    }

    fun clean() {
        if (d_clean) Log.e(TAG, "Clean")

        ParentFragmentAtPos0 = null
        ParentFragmentAtPos1 = null
        ParentFragmentAtPos2 = null
    }

    //DIFFERENT TRAVERSALS
    override fun getItemPosition(`object`: Any?): Int {
        if (d_getItemPosition) Log.e(TAG, "GetItemPosition" + "Enter")


        if (`object` is GameGridViewFragment && ParentFragmentAtPos1 is GamePlayFragment) {
            if (d_getItemPosition)
                Log.e(TAG, "GetItemPosition" + "OBJ instanceof GameViewFrag && pos1 instanceof GamePlayFragment")
            return PagerAdapter.POSITION_NONE
        } else if (`object` is ConnectionsFragment && ParentFragmentAtPos2 is TurnBasedFragment) {
            if (d_getItemPosition)
                Log.e(TAG, "GetItemPosition" + "OBJ instanceof ConnectionsFrag && pos2 instanceof TurnBasedFrag")
            return PagerAdapter.POSITION_NONE
        } else if (`object` is TurnBasedFragment && ParentFragmentAtPos2 is TurnBasedRoomFragment) {
            if (d_getItemPosition)
                Log.e(TAG, "GetItemPosition" + "OBJ instanceof TurnBasedFragment && pos2 instanceof RoomFragment")
            return PagerAdapter.POSITION_NONE
        } else if (`object` is ConnectionsFragment && ParentFragmentAtPos2 is TurnBasedRoomFragment) {
            if (d_getItemPosition)
                Log.e(TAG, "GetItemPosition" + "OBJ instanceof ConnectionsFrag && pos2 instanceof RoomFragment")
            return PagerAdapter.POSITION_NONE
        } else if (`object` is SocialMediaFragment && ParentFragmentAtPos0 is GameInstructionFragment) {
            if (d_getItemPosition)
                Log.e(TAG, "GetItemPosition" + "OBJ instanceof SocialMediaFragment && pos0 instanceof GameInstructionFragment")
            return PagerAdapter.POSITION_NONE

        } else if (`object` is ConnectionsFragment && ParentFragmentAtPos2 is RealTimeRoomFragment) {
            if (d_getItemPosition) Log.e(TAG, "getItemPosition() ConnectionsFragment && RealTimeRoomFragment")
            return PagerAdapter.POSITION_NONE
        } else if (`object` is TurnBasedRoomFragment && ParentFragmentAtPos2 is TurnBasedFragment) {
            if (d_getItemPosition)
                Log.e(TAG, "GetItemPosition" + "OBJ instanceof RoomFragment && pos0 instanceof TurnBasedFragment")
            return PagerAdapter.POSITION_NONE
            /*} else if (object instanceof ConnectionsFragment && ParentFragmentAtPos2 instanceof TurnBasedFragment) {
            if (d_getItemPosition)
                Log.e(TAG, "GetItemPosition" + "OBJ instanceof ConnectionsFragment && pos0 instanceof TurnBasedFragment");
            return POSITION_NONE;
        } else if (object instanceof ConnectionsFragment && ParentFragmentAtPos2 instanceof TurnBasedRoomFragment) {
            if (d_getItemPosition)
                Log.e(TAG, "GetItemPosition" + "OBJ instanceof ConnectionsFragment && pos0 instanceof RoomFragment");
            return POSITION_NONE;*/
        } else if (`object` is RealTimeRoomFragment) {
            if (d_getItemPosition)
                Log.e(TAG, "GetItemPosition" + "OBJ instanceof  TurnBasedFragment")
            return PagerAdapter.POSITION_NONE
        } else if (`object` is TurnBasedFragment) {
            if (d_getItemPosition)
                Log.e(TAG, "GetItemPosition" + "OBJ instanceof  TurnBasedFragment")
            return PagerAdapter.POSITION_NONE
        } else if (`object` is TurnBasedRoomFragment) {
            if (d_getItemPosition)
                Log.e(TAG, "GetItemPosition" + "OBJ instanceof  RoomFragment")
            return PagerAdapter.POSITION_NONE
        } else if (`object` is GameInstructionFragment) {
            if (d_getItemPosition)
                Log.e(TAG, "GetItemPosition" + "OBJ instanceof   GameInstructionFragment")
            return PagerAdapter.POSITION_NONE
        } else if (`object` is ConnectionsFragment) {
            return PagerAdapter.POSITION_NONE
        } else if (`object` is TurnBasedFragment) {

            if (d_getItemPosition)
                Log.e(TAG, "GetItemPosition" + "OBJ instanceof  TurnBasedFragment")
            return PagerAdapter.POSITION_NONE

        } else if (`object` is GamePlayFragment) {
            if (d_getItemPosition)
                Log.e(TAG, "GetItemPosition" + "OBJ instanceof  GamePlayFragment")
            if (isOutsideGame)
                return PagerAdapter.POSITION_NONE
            else
                return PagerAdapter.POSITION_UNCHANGED
        }//FROM TURNBASEDFRAGMENT TO ROOM FRAGMENT
        /*else if (object instanceof TurnBasedFragment && ParentFragmentAtPos2 instanceof TurnBasedRoomFragment) {
            if (d_getItemPosition)
                Log.e(TAG, "GetItemPosition" + "OBJ instanceof TurnBasedFragment && pos0 instanceof RoomFragment");
            return POSITION_NONE;
        }*/

        if (d_getItemPosition) Log.e(TAG, "GetItemPosition" + "Position Unchanged")
        return PagerAdapter.POSITION_UNCHANGED
    }

    fun replaceGameFragmentWithPlay(information: GameInformation) {
        if (d_replaceGameFragmentWithPlay) Log.e(TAG, "replaceGameFragmentWithPlay()")

        try {
            //Swap the main game play fragment
            if (d_replaceGameFragmentWithPlay) Log.e(TAG, "Swapped Main Fragment")

            if (ParentFragmentAtPos1 !is GamePlayFragment) {
                try {
                    if (ParentFragmentAtPos1 != null)
                        fm.beginTransaction().remove(ParentFragmentAtPos1).commit()
                    ParentFragmentAtPos1 = GamePlayFragment.newInstance(0, act.mGameManager!!, this, information)
                    act.mGameManager?.injectController(ParentFragmentAtPos1!!)
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "replaceGameFragmentWithPlay: ", e)
                }

            } else {
                //there is already a game
                //                ((GamePlayFragment)ParentFragmentAtPos1).continueWithGameLogic();
                (ParentFragmentAtPos1 as GamePlayFragment).swapGame(information)
            }

            //swap the information page fragment
            if (ParentFragmentAtPos0 !is GameInstructionFragment) {
                try {
                    if (ParentFragmentAtPos0 != null)
                        fm.beginTransaction().remove(ParentFragmentAtPos0).commit()
                    ParentFragmentAtPos0 = GameInstructionFragment.newInstance()
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "replaceGameFragmentWithPlay: Tried to switch to GameInstructionFragment", e)
                }

            }
            //Switch the right tab frag
            if (information.usesTurnBasedMultiplayerService) {
                if (ParentFragmentAtPos2 !is TurnBasedFragment) {

                    try {

                        if (d_replaceGameFragmentWithPlay) Log.e(TAG, "Swapped Bird Fragment")
                        if (ParentFragmentAtPos2 != null)
                            fm.beginTransaction().remove(ParentFragmentAtPos2).commit()
                        ParentFragmentAtPos2 = TurnBasedFragment.newInstance(2, information)
                    } catch (e: IllegalStateException) {
                        Log.e(TAG, "replaceGameFragmentWithPlay: tried to switch to TurnBasedFragment", e)
                    }

                }
            } else if (information.usesRealTimeMultiplayerServices) {
                if (ParentFragmentAtPos2 !is RealTimeRoomFragment) {

                }
            }

            notifyDataSetChanged()
            isOutsideGame = false

        } catch (e: NullPointerException) {
            Log.e(TAG, "replaceGameFragmentWithPlay() Something is null", e)
        }
    }

    val instructionsFragment: GameInstructionFragment?
        get() = ParentFragmentAtPos0 as GameInstructionFragment?

    fun showTurnBasedMatches() {
        if (d_showTurnBasedMatches) Log.e(TAG, "showTurnBasedMatches")

        try {
            if (ParentFragmentAtPos2 != null)
                fm.beginTransaction().remove(ParentFragmentAtPos2).commit()
            ParentFragmentAtPos2 = TurnBasedFragment.newInstance(2, act?.mGameManager!!.information)
            notifyDataSetChanged()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "showTurnBasedMatches: Tried to switch to TurnBasedFragment", e)
        }

    }

    fun showTurnBasedRoom() {
        if (d_turnBasedRoom) Log.e(TAG, "showTurnBasedRoom()")

        try {
            if (ParentFragmentAtPos2 != null)
                fm.beginTransaction().remove(ParentFragmentAtPos2).commit()
            ParentFragmentAtPos2 = TurnBasedRoomFragment.newInstance(2)
            notifyDataSetChanged()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "showTurnBasedRoom: Tried to switch to TurnBasedRoomFragment", e)
        }

    }

    fun showRealTimeRoom() {
        if (d_showRealTimeRoom) Log.i(TAG, "showRealTimeRoom() ")
        try {
            if (ParentFragmentAtPos2 != null)
                fm.beginTransaction().remove(ParentFragmentAtPos2).commit()
            ParentFragmentAtPos2 = RealTimeRoomFragment.newInstance(2)
            notifyDataSetChanged()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "showRealTimeRoom: Tried to go to RealTimeRoomFragment", e)
        }

    }

    fun isExitable(current_page: Int): Boolean {
        if (d_isExitable) Log.e(TAG, "isExiable()")

        try {
            if (act.mGameManager!!.isPaused) {
                /** yes? allow us to leave the app  */
                // act.mGameManager.onResumeTemp();
                if (d_isExitable) Log.e(TAG, "Already Paused, exiting")
                return true
            } else {

                if (d_isExitable) Log.e(TAG, "Not Paused yet, set to pause")
                act.mGameManager!!.Pause()
                act.lockableViewPager?.setGamePaused(true)


                /** NO? then pause it  */
                Toast.makeText(act, "Press again to leave app.", Toast.LENGTH_SHORT).show()
                return false
            }
        } catch (e: NullPointerException) {
            Log.e(TAG, "isExitable() Exception", e)
            return true
        }

    }

    override fun onDestroyGame() {
        showDefaultGameFragment()
        if (ParentFragmentAtPos2 !is RealTimeRoomFragment) {
            showDefaultSocialFragment()
        }
        showDefaultInformationFragment() //TODO do not change social fragment if in real time
    }

    companion object {

        private val TAG = "MainViewPagerAdpater "
        private val d_getItem = false
        private val d_getItemPosition = false
        private val d_replaceprofilefragment = false
        private val d_replaceGameFragmentWithPlay = false
        private val d_isExitable = false
        private val d_clean = false
        private val d_showProfileFragment = false
        private val d_turnBasedRoom = false
        private val d_showTurnBasedMatches = false
        private val d_showRealTimeRoom = true

        private val NUM_OF_ITEMS = 3 // No of ViewPager items
    }
}

