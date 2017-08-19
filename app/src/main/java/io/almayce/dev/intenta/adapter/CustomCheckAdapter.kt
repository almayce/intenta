package io.almayce.dev.intenta.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.almayce.dev.intenta.R
import io.almayce.dev.intenta.model.ObservableList
import io.almayce.dev.intenta.model.Point


/**
 * Created by almayce on 20.07.17.
 */

class CustomCheckAdapter(val context: Context, private val list: ObservableList<Point>) : RecyclerView.Adapter<CustomCheckAdapter.ViewHolder>() {
    private val inflater: LayoutInflater
    private var clickListener: ItemClickListener? = null
    private var longClickListener: ItemLongClickListener? = null


    init {
        this.inflater = LayoutInflater.from(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_check, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the textview in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val title = list.get(position).title
        val status = list.get(position).status
        holder.tvTitle.text = title
        holder.tvStatus.text = status
    }

    // total number of rows
    override fun getItemCount(): Int {
        return list.size()
    }


    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener,View.OnLongClickListener {
        var tvTitle: TextView
        var tvStatus: TextView

        init {
            tvTitle = itemView.findViewById(R.id.tvTitle) as TextView
            tvStatus = itemView.findViewById(R.id.tvStatus) as TextView
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
        //        return observableList.get(id);
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
