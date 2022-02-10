package com.example.part04_ch02_musicstreamingapp

import com.example.part04_ch02_musicstreamingapp.service.MusicEntity

// MusicEntity 클래스가 mapper라는 함수를 가지게 된다.
fun MusicEntity.mapper(id: Long): MusicModel =
    MusicModel(
        id = id,    // id는 노래의 순서를 표시하기 위한 고유값
        streamUrl = this.streamUrl,
        coverUrl = this.coverUrl,
        track = this.track,
        musician = this.musician
    )