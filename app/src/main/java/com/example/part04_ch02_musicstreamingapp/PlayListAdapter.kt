package com.example.part04_ch02_musicstreamingapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PlayListAdapter(private val callback: (MusicModel) -> Unit): ListAdapter<MusicModel, PlayListAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
        fun bind(item: MusicModel) {

            val itemTrackTextView = view.findViewById<TextView>(R.id.itemTrackTextView)
            val itemMusicianTextView = view.findViewById<TextView>(R.id.itemMusicianTextView)
            val itemCoverImageView = view.findViewById<ImageView>(R.id.itemCoverImageView)

            itemMusicianTextView.text = item.musician
            itemTrackTextView.text = item.track


            Glide.with(itemCoverImageView.context)
                .load(item.coverUrl)
                .into(itemCoverImageView)

            if (item.isPlaying) {
                itemView.setBackgroundColor(Color.GRAY)    // 재생중인 음악일 경우 배경을 회색으로 표시
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT)    // 재생중이 아닐 경우 투명
            }

            itemView.setOnClickListener {
                callback(item)
            }


        }
    }

    // 뷰 홀더가 생성될 때
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_music, parent, false))
    }

    // 뷰 홀더에 뷰가 묶일 때
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        currentList[position].also { musicModel ->
            holder.bind(musicModel)
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<MusicModel>() {
            override fun areItemsTheSame(oldItem: MusicModel, newItem: MusicModel): Boolean {
                // id 값만 비교
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: MusicModel, newItem: MusicModel): Boolean {
                // 내부 컨텐츠 모두 비교
                return oldItem == newItem
            }

        }
    }

}