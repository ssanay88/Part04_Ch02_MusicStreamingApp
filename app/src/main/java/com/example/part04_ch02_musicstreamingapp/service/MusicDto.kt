package com.example.part04_ch02_musicstreamingapp.service

data class MusicDto(
    val musics: List<MusicEntity>    // 서버에서 받아오는 데이터 모델을 MusicEntity로 사용하고 , 뷰에 사용할 음악 데이터는 MusicModel로 분리 중간에 Rapper를 통해 래핑
)
