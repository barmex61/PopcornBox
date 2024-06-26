package com.fatih.popcornbox.adapter

import android.annotation.SuppressLint
import android.text.format.DateUtils
import android.view.View
import android.widget.TextView
import androidx.core.view.doOnDetach
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.fatih.popcornbox.databinding.FragmentTrailerRowBinding
import com.fatih.popcornbox.entities.remote.youtuberesponse.İtem
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.ln
import kotlin.math.pow

class YoutubeVideoAdapter (val layout:Int):BaseAdapter<İtem,FragmentTrailerRowBinding>(layout){

    @SuppressLint("SimpleDateFormat")
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    var listener:((Int,İtem)->Unit)?=null
    fun setOnItemClickListener(listener:(Int,İtem)->Unit){
        this.listener=listener
    }

    private var runnable: Runnable?=null
    private fun TextView.startRunnable(showMoreText: TextView){
        runnable?.let {
            this.removeCallbacks(it)
        }
        doOnDetach {
            removeCallbacks(runnable)
            runnable = null
        }
        runnable = kotlinx.coroutines.Runnable {
            if (this.lineCount > 4) {
                showMoreText.visibility = View.VISIBLE
            } else {
                showMoreText.visibility = View.GONE
            }
        }
        this.post(runnable)
    }

    override var diffUtil: DiffUtil.ItemCallback<İtem> = object :DiffUtil.ItemCallback<İtem>(){
        override fun areContentsTheSame(oldItem: İtem, newItem: İtem): Boolean {
            return oldItem.contentDetails==newItem.contentDetails && oldItem.snippet==newItem.snippet && oldItem.statistics==newItem.statistics
        }

        override fun areItemsTheSame(oldItem: İtem, newItem: İtem): Boolean {
            return oldItem.id==newItem.id
        }
    }
    override var asyncListDiffer: AsyncListDiffer<İtem> = AsyncListDiffer(this,diffUtil)


    override fun onBindViewHolder(holder: MyViewHolder<FragmentTrailerRowBinding>, position: Int) {
        try {
                holder.itemView.setOnClickListener {
                    println("position $position")
                    println("listsize ${list.size}")
                    if(position < list.size){
                    listener?.let {
                        it(position,list[position]) }
                    }
                }
                val dateStr = list[position].snippet.publishedAt
                val date= inputFormat.parse(dateStr)
                val niceDateStr = DateUtils.getRelativeTimeSpanString(date?.time?:0L, Calendar.getInstance().timeInMillis, DateUtils.DAY_IN_MILLIS)
                holder.binding.channelTittle=list[position].snippet.channelTitle
                holder.binding.description=list[position].snippet.description
                holder.binding.imageUrl=list[position].snippet.thumbnails.maxres.url
                holder.binding.viewText=coolNumberFormat(list[position].statistics.viewCount.toLong())
                holder.binding.titleText=list[position].snippet.title
                holder.binding.dateText=niceDateStr.toString()
                holder.binding.runTime=formatDuration(list[position].contentDetails.duration)

        }catch (_:Exception){

        }
    }

    private fun coolNumberFormat(count: Long): String {
        if (count < 1000) return "" + count
        val exp = (ln(count.toDouble()) / ln(1000.0)).toInt()
        val format = DecimalFormat("0.#")
        val value: String = format.format(count / 1000.0.pow(exp.toDouble()))
        return String.format("%s%c", value, "kMBTPE"[exp - 1])
    }
    private fun formatDuration(duration: String): String {
        val millis = parseDuration(duration)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis - TimeUnit.MINUTES.toMillis(minutes))
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun parseDuration(duration: String): Long {
        var totalMillis = 0L
        val regex = Regex("PT(\\d+M)?(\\d+S)?")
        val matchResult = regex.find(duration)
        if (matchResult != null) {
            val (minutes, seconds) = matchResult.destructured
            if (minutes.isNotEmpty()) {
                totalMillis += minutes.dropLast(1).toLong() * 60 * 1000
            }
            if (seconds.isNotEmpty()) {
                totalMillis += seconds.dropLast(1).toLong() * 1000
            }
        }
        return totalMillis
    }
}