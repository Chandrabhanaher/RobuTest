package com.chandrabhan.robutest.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.chandrabhan.robutest.R
import com.chandrabhan.robutest.models.ResponseData
import java.util.ArrayList

class ProductAdapter(
    private val context: Context,
    private var productList: MutableList<ResponseData>
) : RecyclerView.Adapter<ProductAdapter.ItemViewHolder>(){
    var filerResult: List<ResponseData> = productList
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(context).inflate(R.layout.product_items, parent, false)
        )
    }
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val items = productList[position]
        holder.textId.text = items.id
        if(items.images.isEmpty()){
            Glide.with(context).load(R.drawable.ic_logo).into(holder.imageView)
        }else{
            Glide.with(context).load(items.images).into(holder.imageView)
        }
        holder.txtName.text = items.name
        holder.txtPrice.text = items.price.toString()
    }
    override fun getItemCount(): Int {
        return filerResult.size
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.textId)
        lateinit var textId: TextView
        @BindView(R.id.imageView)
        lateinit var imageView: ImageView
        @BindView(R.id.txtName)
        lateinit var txtName: TextView
        @BindView(R.id.txtPrice)
        lateinit var txtPrice: TextView

        init {
            ButterKnife.bind(this, itemView)
        }
    }

}
