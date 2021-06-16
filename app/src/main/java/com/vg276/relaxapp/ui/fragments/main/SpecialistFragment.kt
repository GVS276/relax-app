package com.vg276.relaxapp.ui.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vg276.relaxapp.MainActivity
import com.vg276.relaxapp.MainFragment
import com.vg276.relaxapp.R
import com.vg276.relaxapp.container.SpecialistContainer
import com.vg276.relaxapp.interfaces.OnItemClick
import com.vg276.relaxapp.server.UserFormError
import com.vg276.relaxapp.server.UserFormListener
import com.vg276.relaxapp.server.UserPrivileges
import com.vg276.relaxapp.ui.adapters.SpecsAdapter
import java.util.ArrayList

class SpecialistFragment: MainFragment(), UserFormListener, OnItemClick
{
    private var emptyList: TextView? = null
    private var specsRecyclerView: RecyclerView? = null

    override fun fragmentTitle(): String {
        return getString(R.string.specialists)
    }

    override fun fragmentBackEnabled(): Boolean {
        return false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_specialist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI(view)
        adminPrivilegeUI(view)
    }

    private fun initUI(view: View)
    {
        emptyList = view.findViewById(R.id.emptyList)
        specsRecyclerView = view.findViewById(R.id.specsRecyclerView)
        specsRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        val activity = activity as MainActivity
        activity.sqlConnection?.getListSpecs(this)
    }

    private fun adminPrivilegeUI(view: View)
    {
        // Администраторы могут добавлять своих сотрудников
        // в список специалистов для пользователей

        val activity = activity as MainActivity
        val checker = activity.accountManager!!.isUserAuth() &&
                activity.accountManager!!.getUserPrivilege() == UserPrivileges.ADMIN

        val visibility = if (checker)
            View.VISIBLE
        else
            View.GONE

        val addBtn : TextView = view.findViewById(R.id.addBtn)
        addBtn.visibility = visibility
        addBtn.setOnClickListener {
            activity.navigateFragment(R.id.navigation_create_account_spec,
                    null, R.id.navigation_specialist, false)
        }
    }

    override fun onResult(result: UserFormError, any: Any?) {
        activity?.runOnUiThread {
            if (result == UserFormError.GetAllSpecs)
            {
                any?.let {
                    val list = it as ArrayList<SpecialistContainer>
                    if (list.isEmpty())
                    {
                        emptyList?.visibility = View.VISIBLE
                    } else {
                        val adapter = SpecsAdapter(list, this)
                        specsRecyclerView?.adapter = adapter
                        emptyList?.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onClick(id: String, any: Any?) {
        any?.let {
            val spec = it as SpecialistContainer
            val name = "${spec.lastName} ${spec.firstName} ${spec.middleName}"

            val bundle = bundleOf("biography" to spec.biography, "name" to name)
            val activity = activity as MainActivity
            activity.navigateFragment(
                R.id.navigation_spec_profile, bundle,
                R.id.navigation_specialist, false)
        }
    }
}