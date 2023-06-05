package com.rishiz.jsmadmin.adapter

//import android.content.Context
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rishiz.jsmadmin.R
import com.rishiz.jsmadmin.databinding.ItemCategoryLayoutBinding
import com.rishiz.jsmadmin.model.CategoryModel

class CategoryAdapter(var context: Context, private val list:ArrayList<CategoryModel>): RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    inner class CategoryViewHolder(view: View):RecyclerView.ViewHolder(view){
        var binding=ItemCategoryLayoutBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
      return CategoryViewHolder(LayoutInflater.from(context).inflate(R.layout.item_category_layout,parent,false))
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
       holder.binding.textView.text=list[position].cat
        Glide.with(context).load(list[position].img).into(holder.binding.imageView)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}