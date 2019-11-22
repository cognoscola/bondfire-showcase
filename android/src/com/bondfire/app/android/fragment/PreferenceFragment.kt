package com.bondfire.app.android.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment

import com.bondfire.app.R
import com.bondfire.app.android.activity.MainActivity
import com.bondfire.app.android.activity.GamePlayServiceActivity
import com.bondfire.app.android.utils.social.NetworkManager
import com.bondfire.app.android.view.ActionBarChildView
import com.bondfire.app.android.view.ActionBarView

class PreferenceFragment : Fragment() {

    val TAG: String = PreferenceFragment::class.java.name
    internal var rootView: View? = null
    lateinit internal var account_action: View

    internal var clickedChild = false

    private var listener: ChildFragmentListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            rootView = inflater!!.inflate(R.layout.preferences, container, false)
        }

        val mActionBarView = rootView!!.findViewById(R.id.action_bar_child) as ActionBarView
        val mActionBarProfileView = ActionBarChildView.inflateBar(activity, mActionBarView)
        mActionBarView.addView(mActionBarProfileView)

        if ((activity as GamePlayServiceActivity).signedInGPGS) {
            displayLogout()
        } else {
            displayLogin()
        }


        rootView!!.findViewById<LinearLayout>(R.id.b_child_back_button).setOnClickListener { (activity as GamePlayServiceActivity).onBackPressed() }

        val back = rootView!!.findViewById(R.id.ll_about) as LinearLayout
        back.setOnClickListener {
            clickedChild = true
            val frag = AboutFragment.newInstance()
            val trans = (activity as GamePlayServiceActivity).supportFragmentManager.beginTransaction()
            trans.replace(R.id.childContainer, frag).addToBackStack("FRAGMENT_ABOUT")
            trans.commit()
        }
        return rootView
    }

    private fun displayLogout() {
        account_action = rootView!!.findViewById(R.id.b_sign_out)
        account_action.visibility = View.VISIBLE
        account_action.setOnClickListener {
            (activity as GamePlayServiceActivity).logoutGPGS()
            Toast.makeText(activity, "Signed out", Toast.LENGTH_SHORT).show()
            account_action.visibility = View.GONE
            displayLogin()
        }
    }

    override fun onResume() {
        super.onResume()
        clickedChild = false
        listener!!.onInstanceShown(this)
    }

    private fun displayLogin() {

        account_action = rootView!!.findViewById(R.id.b_sign_in)
        account_action.visibility = View.VISIBLE
        account_action.setOnClickListener { (activity as GamePlayServiceActivity).loginGPGS() }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        try {
            if (!clickedChild) {
                (activity as MainActivity)?.showMainInterface();
            }
        } catch (e:Exception) {
            Log.d(TAG, "onDestroyView: ");
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG,"onActivityResult Inside Fragment")
        if (requestCode == NetworkManager.RC_RESOLVE && resultCode == -1) {
            account_action.visibility = View.GONE
            displayLogout()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.listener = activity as ChildFragmentListener?

    }

    companion object {

        private val d_onActivityResult = true

        fun newInstance(): PreferenceFragment {
            return PreferenceFragment()
        }
    }
}
