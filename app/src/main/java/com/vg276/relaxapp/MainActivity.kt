package com.vg276.relaxapp

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.vg276.relaxapp.server.SQLServerCore
import com.vg276.relaxapp.server.UserAccountManager
import com.vg276.relaxapp.server.UserFormError
import com.vg276.relaxapp.server.UserFormListener

class MainActivity : AppCompatActivity()
{
    private var navController : NavController? = null
    private var navView: BottomNavigationView? = null

    // SQL server
    var sqlConnection: SQLServerCore? = null
    var accountManager : UserAccountManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSqlDataBase()
    }

    override fun onResume() {
        super.onResume()
        sqlConnection?.reconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        sqlConnection?.closeConnection()
    }

    private fun initNavigationBar()
    {
        navView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        navView?.setupWithNavController(navController!!)
    }

    private fun initSqlDataBase()
    {
        sqlConnection = SQLServerCore(this)
        sqlConnection?.connect(object : UserFormListener {
            override fun onResult(result: UserFormError, any: Any?) {
                if (result == UserFormError.ConnectSuccess)
                {
                    // Load UI
                    runOnUiThread {
                        setContentView(R.layout.activity_main)
                        initNavigationBar()
                    }

                    // Auto login
                    accountManager = UserAccountManager.instance(sqlConnection!!)
                    accountManager?.let {
                        val isAuth = it.isUserAuth()
                        if (isAuth) it.autoLogin(null)
                    }
                }
            }
        })
    }

    private fun showBottomBar()
    {
        val isShow = when(navController?.currentDestination?.id)
        {
            R.id.navigation_calendar,
            R.id.navigation_specialist,
            R.id.navigation_profile -> true
            else -> false
        }

        if (isShow)
            navView?.visibility = View.VISIBLE
        else
            navView?.visibility = View.GONE
    }

    override fun onBackPressed()
    {
        val isFinish = when(navController?.currentDestination?.id)
        {
            R.id.navigation_calendar,
            R.id.navigation_specialist,
            R.id.navigation_profile -> true
            else -> false
        }

        if (!isFinish)
        {
            super.onBackPressed()
            showBottomBar()
        }
        else
            finish()
    }

    fun navigateFragment(toId: Int, bundle: Bundle?, currentId: Int, inclusive: Boolean)
    {
        val navOptions = NavOptions.Builder().setPopUpTo(currentId, inclusive).build()
        navController?.navigate(toId, bundle, navOptions)
        navView?.visibility = View.GONE
    }
}