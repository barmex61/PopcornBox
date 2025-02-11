package com.fatih.popcornbox.adapter

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fatih.popcornbox.other.setImageUrl

class PosterImageViewPagerAdapter (private val shouldFitXY:Boolean) :RecyclerView.Adapter<PosterImageViewPagerAdapter.PosterImageViewHolder>() {

    private val diffUtil=object:DiffUtil.ItemCallback<String>(){
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem==newItem
        }

        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem==newItem
        }
    }

    private val asyncListDiffer=AsyncListDiffer(this,diffUtil)

    var urlList:List<String>
        get() = asyncListDiffer.currentList
        set(value) = asyncListDiffer.submitList(value)

    var singleUrl:Boolean = false

    class PosterImageViewHolder(val imageView:ImageView):RecyclerView.ViewHolder(imageView)
    override fun getItemCount(): Int {
        return urlList.size
    }

    override fun onBindViewHolder(holder:PosterImageViewHolder, position: Int) {
        if (singleUrl){
            holder.imageView.setImageUrl(urlList[position], false, isYoutube = false,true)
        }else{
            holder.imageView.setImageUrl(urlList[position],shouldFitXY,false,true)

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):PosterImageViewHolder {
        val imageView=ImageView(parent.context).apply {
            val layoutParams=LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT)
            this.layoutParams=layoutParams
        }
        return PosterImageViewHolder(imageView)
    }
}