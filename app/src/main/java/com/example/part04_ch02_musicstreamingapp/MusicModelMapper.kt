package com.example.part04_ch02_musicstreamingapp

import com.example.part04_ch02_musicstreamingapp.service.MusicDto
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

// 서버에서 받아오는 음악들을 담은 리스트를 이용한다.
fun MusicDto.mapper(): PlayerModel =
    PlayerModel(
        // 음악에 대한 정보를 담은 리스트인 musics 를 mapIndexed를 통해 인덱스와 함께 풀어서 접근 가능하도록 해준다.
        // MusicDto안에 있는 리스트 musics를 mapIndexed로 열어서 musicEntity로 접근할 수 있다.
        playMusicList = this.musics.mapIndexed { index, musicEntity ->
            // MusicEntity를 원소로 가지는 리스트(musics)를 MusicDto에서 받아온 뒤, MusicModel로 다시 매핑을 시켜준다.
            // 두번 매핑하는 과정을 통해 PlayerModel에 필요한 리스트로 변환시켜준다.
            musicEntity.mapper(index.toLong())    // musicEntity를 mapper함수를 이용하여 musicModel롤 변환 , 인덱스를 id로 사용
        }
    )