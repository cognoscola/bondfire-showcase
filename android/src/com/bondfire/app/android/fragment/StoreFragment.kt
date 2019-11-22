package com.bondfire.app.android.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

import com.bondfire.app.R
import com.bondfire.app.android.activity.MainActivity
import com.bondfire.app.android.adapter.StoreSectionAdapter
import com.bondfire.app.android.utils.social.NetworkManager
import com.bondfire.app.android.view.ActionBarChildView
import com.bondfire.app.android.view.ActionBarView

/**
 * Created by alvaregd on 20/03/16.

 */
class StoreFragment : Fragment() {

    internal var rootView: View? = null
    internal var clickedChild = false

    lateinit internal var sectionList: RecyclerView
    lateinit internal var sectionAdapter: StoreSectionAdapter

    private var listener: ChildFragmentListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            rootView = inflater!!.inflate(R.layout.fragment_store, container, false)
        }

        val mActionBarView = rootView!!.findViewById(R.id.action_bar_child) as ActionBarView
        val mActionBarProfileView = ActionBarChildView.inflateBar(activity, mActionBarView)
        mActionBarView.addView(mActionBarProfileView)

        (rootView!!.findViewById(R.id.child_action_bar_title) as TextView).text = resources.getString(R.string.store_title)

        rootView!!.findViewById<LinearLayout>(R.id.b_child_back_button).setOnClickListener { activity?.onBackPressed() }

        /* Configure the store recycler view */
        sectionAdapter = StoreSectionAdapter(activity, 0)
        sectionList = rootView!!.findViewById(R.id.recycler_store_section) as RecyclerView
        sectionList.layoutManager = LinearLayoutManager(activity)
        sectionList.adapter = sectionAdapter

        return rootView
    }

    override fun onResume() {
        super.onResume()
        clickedChild = false
        listener!!.onInstanceShown(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (!clickedChild) {
            (activity as MainActivity)?.showMainInterface()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (d_onActivityResult) Log.e(Tag, "handleApiConnectivity inside Fragment")
        if (requestCode == NetworkManager.RC_RESOLVE && resultCode == -1) {

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.listener = activity as ChildFragmentListener?
    }

    companion object {

        private val Tag = "StoreFragment"
        private val d_onActivityResult = true

        fun newInstance(): StoreFragment {
            return StoreFragment()
        }
    }
}
