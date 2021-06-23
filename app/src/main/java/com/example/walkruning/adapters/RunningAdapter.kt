package com.example.walkruning.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.walkruning.R
import com.example.walkruning.db.Running
import com.example.walkruning.other.TrackingUtility
import kotlinx.android.synthetic.main.item_run.view.*
import java.text.SimpleDateFormat
import java.util.*


// TODO: 23.06.2021
//  RunningFragmenttai recyclerView içerisine oluşturduğumuz item_run xml indeki uı kısımlarını runningFragmentte gösterdik.
//  TrackingFragmentte harita çalıştırılıp sonrasında harita takibini bitirdikten sonra en son haritada nere kalmışsak o kısmı runningFragmenta gösterdik.
class RunningAdapter: RecyclerView.Adapter<RunningAdapter.RunningViewHolder>() {

    inner class RunningViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    private val diffCallback = object : DiffUtil.ItemCallback<Running>() {

        override fun areItemsTheSame(oldItem: Running, newItem: Running): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Running, newItem: Running): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this,diffCallback)

    fun submitList(list: List<Running>) = differ.submitList(list)

    // Oluşturduğumuz item_runs.xml ini recyclerView de runningFragmentta tanımladık
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunningViewHolder {
        return  RunningViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.item_run,
                        parent,
                        false
                )
        )
    }

    override fun getItemCount(): Int {
        return  differ.currentList.size
    }

    // RunningFragmentta Database e kayıtt ettiğimiz bilgileri item_run.xml daki uı lara tanınlandı.
    override fun onBindViewHolder(holder: RunningViewHolder, position: Int) {
        val running = differ.currentList[position]
        holder.itemView.apply {
            Glide.with(this).load(running.image).into(ivRunImage)

            val calender = Calendar.getInstance().apply {
                timeInMillis = running.timestamp
            }

            val dateFormat = SimpleDateFormat("dd.MM.yy",Locale.getDefault())
            tvDate.text = dateFormat.format(calender.time)

            val avgSpeed = "${running.avgSpeedInKMH}km/h"
            tvAvgSpeed.text = avgSpeed

            val distance = "${running.distanceInMeters / 1000f}km"
            tvDistance.text = distance

            val caloriesBurned = "${running.caloriesBurned}kcal"
            tvCalories.text = caloriesBurned

            tvTime.text = TrackingUtility.getFormattedStopWatchTime(running.timeInMillis)




        }
    }


}