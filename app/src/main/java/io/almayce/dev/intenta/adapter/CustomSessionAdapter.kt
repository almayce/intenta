package io.almayce.dev.intenta.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.almayce.dev.intenta.R
import io.almayce.dev.intenta.model.ObservableList
import io.almayce.dev.intenta.model.Session


/**
 * Created by almayce on 20.07.17.
 */

class CustomSessionAdapter(val context: Context, var list: ObservableList<Session>) : RecyclerView.Adapter<CustomSessionAdapter.ViewHolder>() {

    private val inflater: LayoutInflater
    private var clickListener: ItemClickListener? = null
    private var longClickListener: ItemLongClickListener? = null


    init {
        this.inflater = LayoutInflater.from(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_session, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the textview in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val title = list.get(position).title
        holder.tvTitle.text = title
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.itemView.clearAnimation()
    }

    // total number of rows
    override fun getItemCount(): Int {
        return list.size()
    }


    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        var tvTitle: TextView

        init {
            tvTitle = itemView.findViewById(R.id.tvTitle) as TextView
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(view: View) {
            if (clickListener != null) clickListener!!.onItemClick(view, adapterPosition)
        }

        override fun onLongClick(view: View): Boolean {
            view.contentDescription = tvTitle.text.toString()
            if (clickListener != null) longClickListener!!.onItemLongClick(view, adapterPosition)
            return true
        }
    }

    // convenience method for getting data at click position
    fun getItem(id: Int): String? {
        return null
    }

    // allows clicks events to be caught
    fun setClickListener(itemClickListener: ItemClickListener) {
        this.clickListener = itemClickListener
    }

    fun setLongClickListener(itemLongClickListener: ItemLongClickListener) {
        this.longClickListener = itemLongClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    interface ItemLongClickListener {
        fun onItemLongClick(view: View, position: Int)
    }
}
