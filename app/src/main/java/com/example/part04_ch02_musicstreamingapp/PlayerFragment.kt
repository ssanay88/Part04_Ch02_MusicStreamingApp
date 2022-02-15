package com.example.part04_ch02_musicstreamingapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.part04_ch02_musicstreamingapp.databinding.FragmentPlayerBinding
import com.example.part04_ch02_musicstreamingapp.service.MusicDto
import com.example.part04_ch02_musicstreamingapp.service.MusicService
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class PlayerFragment : Fragment(R.layout.fragment_player) {

    private lateinit var playListAdapter: PlayListAdapter    // 재생 목록에 사용한 리사이클러뷰 어댑터

    private var playerModel: PlayerModel = PlayerModel()    // Player에서 사용할 각종 정보들을 담은 데이터모델
    private var binding: FragmentPlayerBinding? = null    // 프래그먼트 뷰바인딩
    private var player: SimpleExoPlayer? = null    // Player에 사용할 Exoplayer - 오디오만 사용
    private val updateSeekRunnable = Runnable {
        updateSeek()
    }

    // 뷰가 생성될 때
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰바인딩 생성
        val fragmentPlayerBinding = FragmentPlayerBinding.bind(view)
        binding = fragmentPlayerBinding

        initPlayView(fragmentPlayerBinding)    // PlayerView 설정
        initPlayListBtn(fragmentPlayerBinding)    // PlayList 버튼에 관한 설정
        initPlayControllBtn(fragmentPlayerBinding)    // Player의 버튼에 관한 설정
        initSeekBar(fragmentPlayerBinding)    // SeekBar에 클릭 리스너를 설정
        initRecyclerView(fragmentPlayerBinding)    // PlayList 리사이클러뷰에 관한 설정

        getMusicListFromServer()

    }

    private fun initSeekBar(fragmentPlayerBinding: FragmentPlayerBinding) {
        fragmentPlayerBinding.playerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            // 진행 상태가 변할 때
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            }

            // SeekBar를 터치했을 때
            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            // SeekBar를 움직인 후 손을 뗄 때
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                player?.seekTo((seekBar.progress * 1000).toLong())    // play의 seek를 움직임을 종료한 progress의 milliseconds로 옮긴다.
            }

        })

        fragmentPlayerBinding.playlistSeekBar.setOnTouchListener { view, motionEvent ->
            false
        }    // 플레이리스트에 있는 작은 SeekBar는 클릭 불가능하게 설정
    }

    private fun initPlayControllBtn(fragmentPlayerBinding: FragmentPlayerBinding) {
        fragmentPlayerBinding.playControlImageView.setOnClickListener {
            val player = this.player ?: return@setOnClickListener

            if (player.isPlaying) {
                // 재생 중인 상태였을 경우
                player.pause()
            } else {
                // 일시 정지 상태였을 경우
                player.play()
            }

        }

        // 다음곡 버튼 클릭
        fragmentPlayerBinding.skipNextImageView.setOnClickListener {

            val nextMusic = playerModel.nextMusic() ?: return@setOnClickListener
            playMusic(nextMusic)

        }

        // 이전곡 버튼 클릭
        fragmentPlayerBinding.skipPrevImageView.setOnClickListener {

            val prevMusic = playerModel.prevMusic() ?: return@setOnClickListener
            playMusic(prevMusic)
        }
    }

    // 간이 플레이어 설정
    private fun initPlayView(fragmentPlayerBinding: FragmentPlayerBinding) {
        context?.let {
            player = SimpleExoPlayer.Builder(it).build()    // SimpleExoPlayer 초기화
        }

        fragmentPlayerBinding.playerView.player = player

        binding?.let { binding ->

            player?.addListener(object : Player.EventListener {

                // Player가 재생되거나 일시정지가 될 때 콜백되는 함수
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)

                    if (isPlaying) {
                        // 재생중이던 경우
                        binding.playControlImageView.setImageResource(R.drawable.ic_baseline_pause_48)
                    } else {
                        // 일시정지 상태였을 경우
                        binding.playControlImageView.setImageResource(R.drawable.ic_baseline_play_arrow_48)
                    }
                }

                // 리사이클러뷰에서 곡이 변경될 때 뷰 변경 - 현재 곡 아이템 회색배경으로 표시 , playMusicModel의 currentPosition도 변경
                // PlayerView도 여기서 계속 변경
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)

                    val newIndex = mediaItem?.mediaId ?: return    // 받아온 값이 null일 경우 return을 통한 예외처리
                    playerModel.currentPosition = newIndex.toInt()    // 선택한 곡의 id(=인덱스)를 currentPosition으로 설정
                    // PlayerView를 변경 , 변경된 곡의 musicModel를 가져와서 변경
                    updatePlayerView(playerModel.currentMusicModel())

                    playListAdapter.submitList(playerModel.getAdapterModels())    // 재생중인 음악 상태를 변경한 리스트 submit

                }

                // 플레이어의 상태가 변화할 때 콜백함수 - 재생 준비 완료, 재생 종료, 재생중 , 재생할 노래가 없는 상태
                override fun onPlaybackStateChanged(state: Int) {
                    super.onPlaybackStateChanged(state)

                    // Play상태가 변할 경우 호출, 안에서 runnable을 통해 재생 중일 경우 반복하여 updateSeek실행 -> UI 업데이트
                    // 재생할 노래가 없거나 종료될 경우
                    updateSeek()

                }

            })
        }
    }

    // Seekbar를 움직여 상태를 변경하고 플레이 타임또한 변경
    private fun updateSeek() {

        val player = this.player ?: return
        // 현재의 position과 duration
        val duration = if (player.duration >= 0) player.duration else 0   // 재생 중인 아이템의 총 길이
        val position = player.currentPosition    // 재생 중인 아이템의 현재 위치

        // TODO UI update
        updateSeekUi(duration,position)

        val state = player.playbackState    // player의 현재 상태를 가져온다.

        // 두번 세번 호출되는 것을 방지
        view?.removeCallbacks(updateSeekRunnable)    // PlaybackState를 통해 실행될 경우 즉시 실행해야 하기 때문에 1초 대기를 해 줄 필요가 없다.
        // 플레이어에 재생할 노래가 없는 경우가 아니고  재생 종료가 아닌 경우 -> 재생 중
        if (state != Player.STATE_IDLE && state != Player.STATE_ENDED) {
            // 1초 후 다시 UI 업데이트 , runnable안에서 updateSeek() 다시 실행
            view?.postDelayed(updateSeekRunnable, 1000)    // 1초 대기 후 runnable이 실행
        }


    }

    private fun updateSeekUi(duration: Long, position: Long) {
        binding?.let { binding ->
            binding.playlistSeekBar.max = (duration / 1000).toInt()    // 길이가 너무 길수있기 때문에 초단위로 변경 후 Int형 변환
            binding.playlistSeekBar.progress = (position / 1000).toInt()    // duration과 같은 의미

            binding.playerSeekBar.max = (duration / 1000).toInt()
            binding.playerSeekBar.progress = (position / 1000).toInt()

            binding.playTimeTextView.text = String.format("%02d:%02d",
                TimeUnit.MINUTES.convert(position, TimeUnit.MILLISECONDS),    // 분, TimeUnit을 통해 milliseconds단위의 position을 분으로 변환
                (position / 1000) % 60    // 초
                )
            binding.totalTimeTextView.text = String.format("%02d:%02d",
                TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS),    // 분, TimeUnit을 통해 milliseconds단위의 duration을 분으로 변환
                (duration / 1000) % 60    // 초
            )



        }
    }

    // Player를 업데이트
    private fun updatePlayerView(currentMusicModel: MusicModel?) {
        currentMusicModel ?: return

        binding?.let { binding ->
            binding.trackTextView.text = currentMusicModel.track
            binding.musicianTextView.text = currentMusicModel.musician
            Glide.with(binding.coverImageView.context)
                .load(currentMusicModel.coverUrl)
                .into(binding.coverImageView)

        }
    }

    // 재생 목록 리사이클러뷰 설정
    private fun initRecyclerView(fragmentPlayerBinding: FragmentPlayerBinding) {

        playListAdapter = PlayListAdapter {
            // 아이템을 클릭 시 호출할 콜백 함수
            playMusic(it)    // 음악을 재생
        }

        // 리사이클러뷰에 어댑터와 레이아웃매니져 연결
        fragmentPlayerBinding.playlistRecyclerView.apply {
            adapter = playListAdapter
            layoutManager = LinearLayoutManager(context)
        }

    }

    // 플레이 리스트 버튼에 대한 설정
    private fun initPlayListBtn(fragmentPlayerBinding: FragmentPlayerBinding) {
        fragmentPlayerBinding.playlistImageView.setOnClickListener {

            // 예외처리 : 서버에서 데이터가 다 불러오지 않은 상태 일 때
            // 재생전에는 플레이리스트가 열리지 않고 playerModel이 초기화되기 전이기 때문에 currentPosition이 -1일 것이다.
            if (playerModel.currentPosition == -1) return@setOnClickListener

            fragmentPlayerBinding.playerViewGroup.isVisible = playerModel.isWatchingPlayListView    // 플레이리스트를 보고 있었을 경우 플레이어로 전환
            fragmentPlayerBinding.playListViewGroup.isVisible = playerModel.isWatchingPlayListView.not()    // 위와 반대

            playerModel.isWatchingPlayListView = !playerModel.isWatchingPlayListView
        }
    }

    // 서버에서 음악 목록을 받아오는 함수
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
                                    // 어디서든 사용할 모델로 매핑
                                    playerModel = it.mapper()    // 서버에서 받아온 데이터를 매핑

                                    // 리사이클러뷰에 들어가는 모델의 경우 현재 선택된 음악의 상태(isPlaying)를 업데이트시킨 모델을 넣어준다.
                                    setMusicList(playerModel.getAdapterModels())
                                    playListAdapter.submitList(playerModel.getAdapterModels())     // 어댑터 리스트에 추가
                                }
                            }


                        override fun onFailure(call: Call<MusicDto>, t: Throwable) {
                            TODO("Not yet implemented")
                        }

                    })
            }
    }

    // 받아온 음악 목록을 플레이어에 MediaItem으로 설정
    private fun setMusicList(musicModelList: List<MusicModel>) {

        context?.let {
            player?.addMediaItems(musicModelList.map { musicModelList ->
                    MediaItem.Builder()
                        .setMediaId(musicModelList.id.toString())    // 미디어 구분을 위한 ID 부여
                        .setUri(musicModelList.streamUrl)
                        .build()    // Url을 미디어 아이템으로 변경
            })

            player?.prepare()    // 플레이어 준비 상태
        }
    }

    // 음악을 재생하는 함수
    private fun playMusic(musicModel: MusicModel) {
        // musicModel에서 받아온 값을 playerModel의 Position으로 업데이트해주는 함수
        playerModel.updateCurrentPosition(musicModel)
        // 플레이어는 musicModel의 리스트를 가지고 있기 때문에 이는 playerModel과 순서가 일치한다.
        // 따라서 playerModel의 인덱스를 통해 musicModel의 순서에 접근한다. 노래 시작 포지션은 0초로 한다.
        player?.seekTo(playerModel.currentPosition , 0)
        player?.play()

    }


    override fun onStop() {
        super.onStop()
        player?.pause()
        view?.removeCallbacks(updateSeekRunnable)    // 실행하던 runnable 삭제
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        player?.release()
        view?.removeCallbacks(updateSeekRunnable)
    }


    companion object {
        // newInstance 함수를 이용하여 프래그먼트 인스턴스를 만들어 주는 이유 : 프래그먼트 객체를 만들 때 다른 값을 사용하여 만들 경우 apply등을 이용하여
        // 쉽게 접근하도록 해준다.
        fun newInstance(): PlayerFragment {
            return PlayerFragment()
        }
    }

}