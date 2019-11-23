package com.bondfire.app.android.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.badlogic.gdx.backends.android.AndroidXFragmentApplication
import com.bondfire.app.Main
import com.bondfire.app.bfUtils.BondfireGraphicsModifier


class BackgroundFragment : AndroidXFragmentApplication(), BondfireGraphicsModifier {


    internal var rootView: View? = null
    internal var main: Main? = null

    override fun startActivity(intent: Intent?) {

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            main = Main()
            rootView = initializeForView(main)
        }
        return rootView
    }

    override fun setGraphicsEffectPercent(effectPerfect: Float) {

    }



    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (main is BondfireGraphicsModifier) {
            main?.onPageScrolled(position,positionOffset,positionOffsetPixels)
        }
    }

    override fun exit() {
        super.exit()
    }

    companion object {

        fun newInstance(): BackgroundFragment {
            return BackgroundFragment()
        }
    }

}


