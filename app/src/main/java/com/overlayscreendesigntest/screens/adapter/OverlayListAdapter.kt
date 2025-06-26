package com.overlayscreendesigntest.screens.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.overlayscreendesigntest.R
import com.overlayscreendesigntest.data.SimilarProduct


class OverlayListAdapter(
    private var items: List<SimilarProduct>,
    private var showHeader: Boolean = false,
    private val onItemClicked: (SimilarProduct) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    fun updateItems(newItems: List<SimilarProduct>, fromGlobalSearch: Boolean = false) {
        items = newItems
        showHeader = fromGlobalSearch
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (showHeader && position == 0) VIEW_TYPE_HEADER else VIEW_TYPE_ITEM
    }

    override fun getItemCount(): Int = items.size + if (showHeader) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.overlay_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.overlay_item, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val actualPosition = if (showHeader) position - 1 else position
            holder.bind(items[actualPosition])
        }
    }

    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view)

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.item_image)
        val name: TextView = itemView.findViewById(R.id.item_name)
        val price: TextView = itemView.findViewById(R.id.item_price)
        val btnBuy: AppCompatButton = itemView.findViewById(R.id.btn_buy)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

        fun bind(item: SimilarProduct) {
            name.text = item.name
            price.text = item.price

            Glide.with(itemImage.context)
                .load(item.matching_image)
                .placeholder(R.drawable.img_place_holder)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        itemImage.setImageResource(R.drawable.img_place_holder)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }
                })
                .into(itemImage)

            btnBuy.setOnClickListener {
                onItemClicked(item)
            }
        }
    }
}