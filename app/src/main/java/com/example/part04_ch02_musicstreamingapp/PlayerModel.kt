package com.example.part04_ch02_musicstreamingapp

// 플레이어를 사용할 때 필요한 각종 정보들을 따로 담아두는 데이터 모델
// 우선 초기화 전이기 때문에 모두 가상의 값으로 일단 설정해준다.
data class PlayerModel (
    private val playMusicList: List<MusicModel> = emptyList(),    // Player에서 사용할 리스트 , 밖에서 참조 불가
    var currentPosition: Int = -1,    // 음악을 선택할 때 사용할 position , 초기화 전이기 때문에 -1로 설정
    var isWatchingPlayListView: Boolean = true    // 플레이리스트를 보고 있는지 , 플레이어를 보고 있는지 확인용
) {

    // MusicModel안에는 현재 재생중인지 판단하는 isPlaying값이 있는데 PlayModel의 currentPosition을 통해 해당 음악이
    // 재생중인지 업데이트하는 함수
    // 플레이어에서 곡 현재 재생중인 곡의 상태를 변경시킨뒤 MusicModel들의 리스트를 리사이클러뷰 어댑터에 반환하도록 해준다.
    fun getAdapterModels(): List<MusicModel> {
        // 현재 index와 currentPosition이 일치하는 경우 재생중이라고 판단.
        // 가져온 musicModel과 인덱스를 적용하여 반환
        return playMusicList.mapIndexed { index, musicModel ->
            // copy : 원하는 값만 수정한 뒤 똑같은 새로운 모델로 만들어준다.
            val newItem = musicModel.copy(
                isPlaying = index == currentPosition     // 현재 선택한 음악과 같은 위치의 음악의 재생 상태를 true로 바꾼뒤 그대로 리스트를 반환
            )

            newItem

        }
    }

    fun updateCurrentPosition(musicModel: MusicModel) {
        // 받아온 musicModel의 인덱스 값으로 업데이트해준다.
        currentPosition = playMusicList.indexOf(musicModel)
    }

    // 다음곡을 재생 , 다음 음악 (musicModel)을 반환하는 함수
    fun nextMusic(): MusicModel? {
        // 재생할 음악이 없을 경우 null 반환
        if (playMusicList.isEmpty()) return null
        // 현재 곡이 마지막 곡일 경우 처음 곡으로 돌아간다.
        currentPosition = if ((currentPosition+1) == playMusicList.size) 0 else currentPosition + 1
        return playMusicList[currentPosition]
    }

    // 이전곡을 재생
    fun prevMusic(): MusicModel? {
        if (playMusicList.isEmpty()) return null
        // 현재 곡이 처음 곡일 경우 마지막 곡을 재생
        currentPosition = if ((currentPosition-1) < 0) playMusicList.lastIndex else currentPosition - 1
        return playMusicList[currentPosition]
    }

    fun currentMusicModel() : MusicModel? {
        if (playMusicList.isEmpty()) return null

        return playMusicList[currentPosition]

    }

}