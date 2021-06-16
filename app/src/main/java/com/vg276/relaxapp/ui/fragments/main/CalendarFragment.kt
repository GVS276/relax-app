package com.vg276.relaxapp.ui.fragments.main

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vg276.relaxapp.MainActivity
import com.vg276.relaxapp.MainFragment
import com.vg276.relaxapp.R
import com.vg276.relaxapp.container.RecordContainer
import com.vg276.relaxapp.container.SpecialistContainer
import com.vg276.relaxapp.server.UserFormError
import com.vg276.relaxapp.server.UserFormListener
import com.vg276.relaxapp.server.UserPrivileges
import java.util.*

class CalendarFragment: MainFragment()
{
    private var myPhone = ""
    private var myRole : UserPrivileges? = null

    override fun fragmentTitle(): String {
        return getString(R.string.record)
    }

    override fun fragmentBackEnabled(): Boolean {
        return false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity
        activity.accountManager?.let {
            myPhone = it.getUserPhone()
            //myRole = it.getUserPrivilege()
        }

        val calendarViewText: TextView = view.findViewById(R.id.calendarViewText)
        val calendarView: CalendarView = view.findViewById(R.id.calendarView)
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val timestamp = "$dayOfMonth:${month+1}:$year"
            getList(timestamp)
        }

        val emptyList : TextView = view.findViewById(R.id.emptyList)
        val recordsRecyclerView: RecyclerView = view.findViewById(R.id.recordsRecyclerView)
        recordsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        getActiveRecordsList(recordsRecyclerView, emptyList)

        myRole?.let {
            if (it != UserPrivileges.USER)
            {
                //calendarView.visibility = View.GONE
                //calendarViewText.visibility = View.GONE
            }
        }
    }

    private fun getActiveRecordsList(view: RecyclerView, emptyList : TextView)
    {
        val activity = activity as MainActivity
        activity.accountManager?.let {
            if (it.isUserAuth()) {
                activity.sqlConnection?.getAllRecords(object : UserFormListener
                {
                    override fun onResult(result: UserFormError, any: Any?) {
                        if (result == UserFormError.GetAllRecords)
                        {
                            any?.let {
                                val list = it as ArrayList<RecordContainer>
                                if (list.isEmpty())
                                {
                                    view.visibility = View.GONE
                                    emptyList.visibility = View.VISIBLE
                                } else {
                                    view.visibility = View.VISIBLE
                                    emptyList.visibility = View.GONE
                                }
                            }
                        }
                    }
                })
            }
        }
    }

    private fun getList(timestamp: String)
    {
        val activity = activity as MainActivity
        activity.accountManager?.let {
            if (it.isUserAuth())
            {
                activity.sqlConnection?.getListSpecs(object : UserFormListener
                {
                    override fun onResult(result: UserFormError, any: Any?) {
                        any?.let { specs ->

                            val listName: ArrayList<String> = ArrayList()
                            val list = specs as ArrayList<SpecialistContainer>
                            list.forEach { spec ->
                                val name = "${spec.lastName} ${spec.firstName} ${spec.middleName}"
                                listName.add(name)
                            }

                            activity.runOnUiThread {
                                val userPhone = it.getUserPhone()
                                dialogRecord(timestamp, userPhone, list, listName)
                            }
                        }
                    }
                })
            } else {
                Toast.makeText(requireContext(), "Пожалуйста, авторизируйтесь, чтобы записаться на массаж!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dialogRecord(timestamp: String,
                             userPhone: String,
                             list: ArrayList<SpecialistContainer>,
                             listName: ArrayList<String>)
    {
        val split = timestamp.split(":")
        val day =
                if (split[0].substring(0, 1).toInt() < 10)
                    "0${split[0]}"
                else
                    split[0]
        val month =
                if (split[1].substring(0, 1).toInt() < 10)
                    "0${split[1]}"
                else
                    split[1]
        val date = "$day.$month.${split[2]}"

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.view_records, null)
        val timePicker : TimePicker = view.findViewById(R.id.timePicker)
        val specSpinner : Spinner = view.findViewById(R.id.specSpinner)

        timePicker.hour = 10
        timePicker.minute = 0
        timePicker.setIs24HourView(true)
        timePicker.setOnTimeChangedListener { picker, hourOfDay, minute ->
            var decline = hourOfDay >= 21
            decline = decline && minute >= 0

            if (decline)
            {
                picker.hour = 21
                picker.minute = 0
                Toast.makeText(requireContext(),
                        "Время работы до 21:00", Toast.LENGTH_SHORT).show()
            }
        }

        val adapter = ArrayAdapter<String>(requireContext(),
                android.R.layout.simple_list_item_1, listName)
        specSpinner.adapter = adapter

        val clicked = DialogInterface.OnClickListener { dialog, which ->
            when(which) {
                Dialog.BUTTON_POSITIVE ->
                {
                    val toUser = list[specSpinner.selectedItemPosition].phone
                    val record = "$timestamp:${timePicker.hour}:${timePicker.minute}"
                    val activity = activity as MainActivity

                    activity.sqlConnection?.recordUser(
                            userPhone,
                            toUser,
                            record,
                            null)
                }
            }
            dialog?.dismiss()
        }

        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle("Запись на массаж")
        alertDialog.setMessage("Пожалуйста, выберите время на\n$date")
        alertDialog.setView(view)
        alertDialog.setPositiveButton("Записаться", clicked)
        alertDialog.setNegativeButton("Закрыть", clicked)

        val dialog = alertDialog.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.accentColor))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.accentColor))
    }
}