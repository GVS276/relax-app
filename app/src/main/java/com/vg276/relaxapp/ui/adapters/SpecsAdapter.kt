package com.vg276.relaxapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vg276.relaxapp.R
import com.vg276.relaxapp.container.SettingsItemContainer
import com.vg276.relaxapp.container.SpecialistContainer
import com.vg276.relaxapp.interfaces.OnItemClick

class SpecsAdapter(listSpecs: ArrayList<SpecialistContainer>, onItemClick: OnItemClick) :
    RecyclerView.Adapter<SpecsAdapter.ViewHolder>()
{
    private val list = listSpecs
    private val clickListener = onItemClick

    class ViewHolder(item: View, onItemClick: OnItemClick) : RecyclerView.ViewHolder(item)
    {
        private val clickListener = onItemClick

        private var itemIcon: ImageView? = null
        private var itemText: TextView? = null
        private var itemSubText: TextView? = null

        init {
            itemIcon = itemView.findViewById(R.id.itemIcon)
            itemText = itemView.findViewById(R.id.itemText)
            itemSubText = itemView.findViewById(R.id.itemSubText)
        }

        fun bind(spec: SpecialistContainer)
        {
            itemIcon?.setImageResource(R.drawable.placeholder)

            val name = "${spec.lastName} ${spec.firstName} ${spec.middleName}"
            itemText?.text = name

            val rol = spec.biography.subSequence(0, spec.biography.indexOf(","))
            itemSubText?.text = rol

            // нажатия (отправляем колбэк)
            itemView.setOnClickListener {
                clickListener.onClick(spec.phone, spec)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.item_settings, parent, false)
        return ViewHolder(item, clickListener)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }
}