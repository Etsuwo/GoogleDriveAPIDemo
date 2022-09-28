package com.example.google_drive_api_demo.ui.folder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.google_drive_api_demo.R

class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
    val imageView: ImageView = item.findViewById(R.id.imageView)
    val textView: TextView = item.findViewById(R.id.textView)
}

class RecyclerViewAdapter(
    var list: List<DriveItem>,
    private val onClick: (item: DriveItem) -> Unit
): RecyclerView.Adapter<ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.textView.text = item.fileName
        Glide.with(holder.imageView).load(item.imageUrl).into(holder.imageView)
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = list.size

}