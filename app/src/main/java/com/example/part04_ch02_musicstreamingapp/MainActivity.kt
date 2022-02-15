package com.example.part04_ch02_musicstreamingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer , PlayerFragment.newInstance() )
            .commit()

    }
}

/*
diffUtil?
copy 사용 이유
 */

/*
Exoplayer 사용하기
 - custom controller
 - Playlist 등

androidx.contraintlayout.widget.Group : 음악 플레이 리스트와 재생 화면 사이에 전환에 사용

Seekbar Custom 하기기 : 음악 재생에 이용

 # 음악 스트리밍 앱 #

Retrofit을 이용하여 재생 목록을 받아와 구성함
재생 목록을 클릭하여 ExoPlayer를 이용하여 음악을 재생할 수 있음
이전,다음 트랙 버튼을 눌러서 이전,다음 음악으로 재생하고, UI를 업데이트 할 수 있음
Playlist 화면과 Player 화면 간의 전환을 할 수 있음
Seekbar를 Custom하여 원하는 UI로 표시할 수 있음

 */