package com.example.part04_ch02_musicstreamingapp

// 뷰에 매칭시켜줄 데이터 모델
data class MusicModel(
    val id: Long,
    val track: String,
    val streamUrl: String,
    val musician: String,
    val coverUrl: String,
    val isPlaying: Boolean = false    // 재생되고 있는지 확인하는 값 , 서버에서는 받아오지 않기 때문에 초기값 False
)