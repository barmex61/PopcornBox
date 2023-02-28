package com.fatih.popcornbox.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter <T,K:ViewDataBinding> (private val layoutId:Int):RecyclerView.Adapter<BaseAdapter.MyViewHolder<K>>(){

    open var vibrantColor:Int=0

    abstract var diffUtil:DiffUtil.ItemCallback<T>

    abstract var asyncListDiffer:AsyncListDiffer<T>

    var list:List<T>
        get() = asyncListDiffer.currentList
        set(value) = asyncListDiffer.submitList(value)

    open var myItemClickLambda:((String?,Int,Pair<Int,Int>?,Boolean?)->Unit)?=null

    open fun setMyOnClickLambda(lambda:(String?,Int,Pair<Int,Int>?,Boolean?) ->Unit){
        this.myItemClickLambda=lambda
    }

    open class MyViewHolder <K>(val binding:K):RecyclerView.ViewHolder((binding as ViewDataBinding).root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder<K> {
        val binding=DataBindingUtil.inflate<K>(LayoutInflater.from(parent.context),layoutId,parent,false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

}