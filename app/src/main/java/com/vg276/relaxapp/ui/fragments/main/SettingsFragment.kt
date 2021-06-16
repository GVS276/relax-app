package com.vg276.relaxapp.ui.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vg276.relaxapp.MainActivity
import com.vg276.relaxapp.MainFragment
import com.vg276.relaxapp.R
import com.vg276.relaxapp.container.SettingsItemContainer
import com.vg276.relaxapp.interfaces.OnItemClick
import com.vg276.relaxapp.ui.adapters.SettingsAdapter

class SettingsFragment: MainFragment(), OnItemClick
{
    companion object
    {
        internal const val SETTINGS_ITEM_TAG = "settingsItem"
        internal const val ABOUT_ITEM_TAG = "aboutItem"
    }

    private var list: ArrayList<SettingsItemContainer> = ArrayList()
    private var settingsRecyclerView: RecyclerView? = null

    override fun fragmentTitle(): String {
        return getString(R.string.profile)
    }

    override fun fragmentBackEnabled(): Boolean {
        return false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsRecyclerView = view.findViewById(R.id.settingsRecyclerView)
        settingsRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        // вызываем метод для инициализации адаптера и формирования списка настроек
        setListSettings()

        // UI
        initUI(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        list.clear()
    }

    private fun initUI(view: View)
    {
        val userAvatar : ImageView = view.findViewById(R.id.userAvatar)
        val userPhone : TextView = view.findViewById(R.id.userPhone)
        val userName : TextView = view.findViewById(R.id.userName)
        val authContent : LinearLayout = view.findViewById(R.id.authContent)

        val activity = activity as MainActivity
        activity.accountManager?.let {
            var idNavigate = R.id.navigation_login
            var bundle: Bundle? = null

            if (it.isUserAuth())
            {
                idNavigate = R.id.navigation_user_profile
                val phone = it.getUserPhone()
                val name = it.getUserName()
                val role = it.getUserPrivilege().name

                bundle = bundleOf(
                        "userName" to name,
                        "userPhone" to phone,
                        "userRole" to role)

                userPhone.text = phone
                userName.text = name
                userName.visibility = View.VISIBLE
            } else
                userName.visibility = View.GONE

            authContent.setOnClickListener {
                activity.navigateFragment(idNavigate,
                        bundle, R.id.navigation_profile, false)
            }
        }.also {
           // if (it == null)

        }
    }

    private fun setListSettings()
    {
        // пункты
        val settingsItem = SettingsItemContainer(SETTINGS_ITEM_TAG)
        settingsItem.title = getString(R.string.settings)
        settingsItem.subTitle = getString(R.string.settings_subtitle)
        settingsItem.resIcon = R.drawable.settings

        val aboutItem = SettingsItemContainer(ABOUT_ITEM_TAG)
        aboutItem.title = getString(R.string.about)
        aboutItem.subTitle = getString(R.string.about_subtitle)
        aboutItem.resIcon = R.drawable.about

        // добавляем пункты в список
        list.add(settingsItem)
        list.add(aboutItem)

        // добавляем лист в адаптер
        val adapter = SettingsAdapter(list, this)
        settingsRecyclerView?.adapter = adapter
    }

    // нажатия для пунктов, где id - это индитификатор пункта указаный при формировании списка
    override fun onClick(id: String, any: Any?) {
        when(id)
        {
            SETTINGS_ITEM_TAG ->
                Toast.makeText(requireContext(), "Настройки", Toast.LENGTH_SHORT).show()

            ABOUT_ITEM_TAG ->
                Toast.makeText(requireContext(), "О компании", Toast.LENGTH_SHORT).show()
        }
    }
}