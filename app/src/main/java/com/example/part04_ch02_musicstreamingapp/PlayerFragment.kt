package com.example.part04_ch02_musicstreamingapp

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.part04_ch02_musicstreamingapp.databinding.FragmentPlayerBinding
import com.example.part04_ch02_musicstreamingapp.service.MusicDto
import com.example.part04_ch02_musicstreamingapp.service.MusicService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PlayerFragment : Fragment(R.layout.fragment_player) {

    private lateinit var playListAdapter: PlayListAdapter

    private var binding: FragmentPlayerBinding? = null
    private var isWatchingPlaylistView = true    // 플레이리스트를 보고 있는지 , 플레이어를 보고 있는지 확인용

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentPlayerBinding = FragmentPlayerBinding.bind(view)
        binding = fragmentPlayerBinding

        initPlayListBtn(fragmentPlayerBinding)
        initRecyclerView(fragmentPlayerBinding)

        getMusicListFromServer()

    }

    private fun initRecyclerView(fragmentPlayerBinding: FragmentPlayerBinding) {

        playListAdapter = PlayListAdapter {
            // TODO 음악을 재생
        }

        fragmentPlayerBinding.playlistRecyclerView.apply {
            adapter = playListAdapter
            layoutManager = LinearLayoutManager(context)
        }

    }

    private fun initPlayListBtn(fragmentPlayerBinding: FragmentPlayerBinding) {
        fragmentPlayerBinding.playlistImageView.setOnClickListener {

            // TODO 예외처리 : 서버에서 데이터가 다 불러오지 않은 상태 일 때

            fragmentPlayerBinding.playerViewGroup.isVisible = isWatchingPlaylistView    // 플레이리스트를 보고 있었을 경우 플레이어로 전환
            fragmentPlayerBinding.playListViewGroup.isVisible = isWatchingPlaylistView.not()    // 위와 반대

            isWatchingPlaylistView = !isWatchingPlaylistView
        }
    }

    private fun getMusicListFromServer() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://run.mocky.io")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(MusicService::class.java)
            .also {
                it.listMusics()
                    .enqueue(object : Callback<MusicDto> {
                        override fun onResponse(
                            call: Call<MusicDto>,
                            response: Response<MusicDto>
                        ) {
                            // 서버에서 성공적으로 데이터를 받아올 경우
                            Log.d("로그", "${response.body()}")
                            // nullable 해제
                            response.body()?.let {
                                // MusicDto안에 있는 리스트 musics를 mapIndexed로 열어서 musicEntity로 접근할 수 있다.
                                var musicModelList = it.musics.mapIndexed { index, musicEntity ->
                                    // musicEntity를 mapper함수를 이용하여 musicModel롤 변환
                                    musicEntity.mapper(index.toLong())    // 인덱스를 id로 사용
                                }

                                playListAdapter.submitList(musicModelList)     // 어댑터 리스트에 추가가

                            }


                        }

                        override fun onFailure(call: Call<MusicDto>, t: Throwable) {
                            TODO("Not yet implemented")
                        }

                    })
            }
    }

    companion object {
        // newInstance 함수를 이용하여 프래그먼트 인스턴스를 만들어 주는 이유 : 프래그먼트 객체를 만들 때 다른 값을 사용하여 만들 경우 apply등을 이용하여
        // 쉽게 접근하도록 해준다.
        fun newInstance(): PlayerFragment {
            return PlayerFragment()
        }
    }

}