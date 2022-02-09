package com.example.part04_ch02_musicstreamingapp.service

import com.google.gson.annotations.SerializedName

// 서버에서 받아오는 데이터들 , 뷰에서 그대로 사용하지 않는다.
data class MusicEntity(
    @SerializedName("track") val track: String,
    @SerializedName("streamUrl") val streamUrl: String,
    @SerializedName("artist") val artist: String,
    @SerializedName("coverUrl") val coverUrl: String
)