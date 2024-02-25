package com.fatih.popcornbox.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fatih.popcornbox.R
import com.fatih.popcornbox.databinding.AddViewRowBinding
import com.fatih.popcornbox.databinding.FragmentMainRvRowBinding
import com.fatih.popcornbox.entities.remote.discoverresponse.DiscoverResult
import com.fatih.popcornbox.other.Constants.getVibrantColor
import com.google.android.gms.ads.AdRequest

class HomeFragmentAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_NORMAL = 0
    private val VIEW_TYPE_ADVIEW = 1
    var spanCount = 3

    var myItemClickLambda:((String?,Int,Pair<Int,Int>?,Boolean?)->Unit)?=null

    fun setMyOnClickLambda(lambda:(String?,Int,Pair<Int,Int>?,Boolean?) ->Unit){
        this.myItemClickLambda=lambda
    }

    class HomeViewHolder(val binding: FragmentMainRvRowBinding): RecyclerView.ViewHolder(binding.root)
    class AddViewHolder(val binding:AddViewRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        //return VIEW_TYPE_NORMAL
        return if ((position + 1) % ((spanCount*10) + 1) == 0) VIEW_TYPE_ADVIEW else VIEW_TYPE_NORMAL
    }

    val diffUtil=object:DiffUtil.ItemCallback<DiscoverResult>(){
        override fun areContentsTheSame(oldItem: DiscoverResult, newItem: DiscoverResult): Boolean {
            return oldItem==newItem
        }

        override fun areItemsTheSame(oldItem: DiscoverResult, newItem: DiscoverResult): Boolean {
            return oldItem.id==newItem.id
        }
    }

    val asyncListDiffer: AsyncListDiffer<DiscoverResult> = AsyncListDiffer(this,diffUtil)

    var list:List<DiscoverResult>
        get() = asyncListDiffer.currentList
        set(value) = asyncListDiffer.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_NORMAL){
            val binding = DataBindingUtil.inflate<FragmentMainRvRowBinding>(LayoutInflater.from(parent.context),
                R.layout.fragment_main_rv_row,parent,false)
            HomeViewHolder(binding)
        }else{
            val binding = DataBindingUtil.inflate<AddViewRowBinding>(LayoutInflater.from(parent.context),
                R.layout.add_view_row,parent,false)
           AddViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder:RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_NORMAL){
            holder as HomeViewHolder
            val selectedPosition=list[position]
            holder.binding.apply {
                imageUrl=selectedPosition.poster_path
                voteAverage=selectedPosition.vote_average
                releaseDate=selectedPosition.release_date
                firstAirDate=selectedPosition.first_air_date
            }
            holder.itemView.setOnClickListener {
                val pair= getVibrantColor(holder.binding.movieImage)
                if(position<list.size){
                    list[position].id?.let { id->
                        myItemClickLambda?.invoke(list[position].poster_path,id,pair,null)
                    }
                }
            }
        }else{
            holder as AddViewHolder
            val adRequest: AdRequest = AdRequest.Builder().build()
            holder.binding.adView.loadAd(adRequest)
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

}