package com.example.part04_ch02_musicstreamingapp

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.part04_ch02_musicstreamingapp.service.MusicDto
import com.example.part04_ch02_musicstreamingapp.service.MusicService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PlayerFragment : Fragment(R.layout.fragment_player) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        getMusicListFromServer()

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
                            Log.d("로그", "${response.body()}")
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