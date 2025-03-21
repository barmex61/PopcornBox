package com.fatih.popcornbox.adapter


import android.view.View
import android.widget.TextView
import androidx.core.view.doOnDetach
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fatih.popcornbox.R
import com.fatih.popcornbox.databinding.ReviewRecyclerRowBinding
import com.fatih.popcornbox.entities.remote.reviewresponse.ReviewResult
import com.fatih.popcornbox.ui.DetailsFragment
import kotlinx.coroutines.Runnable


class ReviewAdapter(
    val layout:Int,
) : BaseAdapter<ReviewResult,ReviewRecyclerRowBinding>(layout){

    private var runnable: Runnable?=null
    private fun TextView.startRunnable( showMoreText:TextView){
        runnable?.let {
            this.removeCallbacks(it)
        }
        doOnDetach {
            removeCallbacks(runnable)
            runnable = null
        }
        runnable = Runnable {
            if (this.lineCount>4){
                showMoreText.visibility=View.VISIBLE
            }else{
                showMoreText.visibility=View.GONE
            }
        }
        this.post(runnable)
    }

    override var diffUtil: DiffUtil.ItemCallback<ReviewResult> = object : DiffUtil.ItemCallback<ReviewResult>(){
        override fun areContentsTheSame(oldItem: ReviewResult, newItem: ReviewResult): Boolean {
            return oldItem.id==newItem.id
        }

        override fun areItemsTheSame(oldItem: ReviewResult, newItem: ReviewResult): Boolean {
            return oldItem==newItem
        }
    }

    override var asyncListDiffer: AsyncListDiffer<ReviewResult> = AsyncListDiffer(this,diffUtil)

    override fun onBindViewHolder(holder: MyViewHolder<ReviewRecyclerRowBinding>, position: Int) {

        holder.binding.result=list[position]
        val showMoreText=holder.binding.showMoreText
        val contextText=holder.binding.contentText
        holder.binding.showMoreText.setTextColor(DetailsFragment.vibrantColor?: R.color.white)
        contextText.startRunnable(showMoreText)
        showMoreText.setOnClickListener {
            if (showMoreText.text=="Show more"){
                holder.binding.contentText.maxLines=100
                showMoreText.text="Show less"
            }else{
                holder.binding.contentText.maxLines=4
                showMoreText.text="Show more"
            }
        }
    }
}