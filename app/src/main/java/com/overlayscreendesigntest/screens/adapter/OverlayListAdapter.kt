package com.overlayscreendesigntest.screens.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.overlayscreendesigntest.R
import com.overlayscreendesigntest.data.SimilarProduct


class OverlayListAdapter(private var items: List<SimilarProduct>,
                         private val onItemClicked: (SimilarProduct) -> Unit) : RecyclerView.Adapter<OverlayListAdapter.ViewHolder>() {

    fun updateItems(newItems: List<SimilarProduct>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.overlay_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.item_image)
        val name: TextView = itemView.findViewById(R.id.item_name)
        val price: TextView = itemView.findViewById(R.id.item_price)
        val btnBuy: AppCompatButton = itemView.findViewById(R.id.btn_buy)

        fun bind(item: SimilarProduct) {
            val stringBuilder = StringBuilder()

            // Appending text to the StringBuilder
            stringBuilder.append("$ ")
            stringBuilder.append(item.price)

            // Convert StringBuilder to a String
            name.text = item.name
            name.text = item.name
            price.text = stringBuilder.toString()
            Glide.with(itemImage.context).load(item.matching_image).into(itemImage)

            btnBuy.setOnClickListener {
                onItemClicked(item)
            }
        }
    }
}







