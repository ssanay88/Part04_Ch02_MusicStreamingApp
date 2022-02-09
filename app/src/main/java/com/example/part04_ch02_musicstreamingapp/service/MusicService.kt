package com.example.part04_ch02_musicstreamingapp.service

import retrofit2.Call
import retrofit2.http.GET

interface MusicService {

    @GET("/v3/a2b70a78-fe09-4fbc-a685-005afc4af5b5")
    fun listMusics() : Call<MusicDto>
}