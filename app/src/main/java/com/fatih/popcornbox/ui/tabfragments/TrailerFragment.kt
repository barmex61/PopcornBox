package com.fatih.popcornbox.ui.tabfragments

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.fatih.popcornbox.R
import com.fatih.popcornbox.adapter.YoutubeVideoAdapter
import com.fatih.popcornbox.databinding.FragmentTrailerBinding
import com.fatih.popcornbox.other.Constants.movieSearch
import com.fatih.popcornbox.other.Constants.tvSearch
import com.fatih.popcornbox.other.Status
import com.fatih.popcornbox.ui.DetailsFragment
import com.fatih.popcornbox.viewmodel.TrailerFragmentViewModel
import com.google.android.youtube.player.YouTubePlayerFragment
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.DefaultPlayerUiController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class TrailerFragment @Inject constructor(): Fragment(R.layout.fragment_trailer) {

    private var _binding:FragmentTrailerBinding?=null
    private val binding:FragmentTrailerBinding
        get() = _binding!!
    private lateinit var viewModel: TrailerFragmentViewModel
    private var myVideoId:String=""
    private var selectedId=0
    private var part="snippet,contentDetails,statistics"
    private lateinit var youtubePlayerView: YouTubePlayerView
    private var listener: AbstractYouTubePlayerListener?=null
    private var youtubeVideoAdapter : YoutubeVideoAdapter?=null
    private var myYoutubePlayer:YouTubePlayer?=null
    private var mCurrentTime=0f
    private var mCurrentVideoId=""
    private val tracker=YouTubePlayerTracker()
    private var isThereAnyVideoUrl=false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding= FragmentTrailerBinding.inflate(inflater,container,false)
        viewModel= ViewModelProvider(requireActivity())[TrailerFragmentViewModel::class.java]
        if (savedInstanceState?.getBoolean("isRotated")!=true){
            viewModel.resetData()
        }
        binding.isLoading=true
        doInitialization()
        mCurrentVideoId=savedInstanceState?.getString("video_id")?:myVideoId
        mCurrentTime=savedInstanceState?.getFloat("current_time")?:mCurrentTime

        return binding.root
    }

    @SuppressLint("DiscouragedApi", "InternalInsetResource")
    private fun setupStatusBar(isPortrait:Boolean){
        val statusBarHeightId = resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight = resources.getDimensionPixelSize(statusBarHeightId) +10
        if (isPortrait){
            binding.trailerMainLayout.updatePadding(top = statusBarHeight)
        }else{
            binding.trailerMainLayout.updatePadding(top = 0, bottom = 0, left = 0, right = 0)
        }
    }

    private fun doInitialization(){
        setupStatusBar(controlOrientation())
        youtubePlayerView=binding.youtubePlayer
        youtubePlayerView.enableAutomaticInitialization=false
        if (controlOrientation()){
            youtubePlayerView.exitFullScreen()
        }else{
            youtubePlayerView.enterFullScreen()
        }
        lifecycle.addObserver(youtubePlayerView)
        listener=object:AbstractYouTubePlayerListener(){
            override fun onReady(youTubePlayer: YouTubePlayer) {
                super.onReady(youTubePlayer)
                myYoutubePlayer=youTubePlayer
                myYoutubePlayer!!.addListener(tracker)
                youtubePlayerView.setCustomPlayerUi(DefaultPlayerUiController(youtubePlayerView,myYoutubePlayer!!).apply {
                    showUi(true)
                }.rootView)

                if (mCurrentVideoId.isNotEmpty() && mCurrentTime != 0f){
                    binding.isLoading=false
                    myYoutubePlayer!!.loadVideo(mCurrentVideoId,mCurrentTime)
                }else {
                    if(!viewModel.itemList.value.isNullOrEmpty()){
                        binding.isLoading=false
                        myYoutubePlayer!!.cueVideo(viewModel.itemList.value?.get(0)?.id?:"",0f)
                    }
                }
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                super.onStateChange(youTubePlayer, state)
            }

            override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
                super.onVideoId(youTubePlayer, videoId)
            }
        }
        val iFramePlayerOptions=IFramePlayerOptions.Builder().controls(0).build()
        youtubePlayerView.initialize(listener as AbstractYouTubePlayerListener,iFramePlayerOptions)
        youtubePlayerView.addFullScreenListener(object :YouTubePlayerFullScreenListener{
            override fun onYouTubePlayerEnterFullScreen() {
                requireActivity().requestedOrientation= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }

            override fun onYouTubePlayerExitFullScreen() {
                requireActivity().requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            }
        })
        youtubeVideoAdapter= YoutubeVideoAdapter(R.layout.fragment_trailer_row)

        youtubeVideoAdapter!!.setOnItemClickListener {position,item->
            viewModel.updateList(position,item){
                youtubeVideoAdapter?.list=it
                myYoutubePlayer?.loadVideo(youtubeVideoAdapter!!.list[position].id,0f)
                binding.isLoading=false
                binding.youtubeRecyclerView!!.smoothScrollToPosition(0)
            }
        }
        binding.youtubeRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        binding.youtubeRecyclerView?.adapter = youtubeVideoAdapter

        arguments?.let {
            selectedId=TrailerFragmentArgs.fromBundle(it).id
        }

        if (DetailsFragment.isItInMovieList){
            viewModel.getVideos(movieSearch,selectedId)
        }else{
            viewModel.getVideos(tvSearch,selectedId)
        }
        observeLiveData()
    }


    private fun observeLiveData(){
        viewModel.videoResponse.observe(viewLifecycleOwner){resource->
            if(resource!=null){
                when(resource.status){
                    Status.SUCCESS->{
                        resource.data?.let {
                            it.results.forEach {result->
                                if(result.site=="YouTube"){
                                    binding.youtubePlayer.visibility=View.VISIBLE
                                    binding.youtubeRecyclerView?.visibility=View.VISIBLE
                                    isThereAnyVideoUrl=true
                                    myVideoId = if(myVideoId.isEmpty()){
                                        result.key
                                    }else{
                                        myVideoId+","+result.key
                                    }
                                }
                            }
                            if (!isThereAnyVideoUrl){
                                binding.trailerLottie.visibility=View.VISIBLE
                                binding.trailerLottie.playAnimation()
                            }
                            myVideoId.let {
                                viewModel.getYoutubeResponse(part,it)
                            }
                        }
                    }
                    Status.ERROR->{
                        binding.trailerLottie.visibility=View.VISIBLE
                        binding.trailerLottie.playAnimation()}
                    else->Unit
                }
            }
        }

        viewModel.youtubeResponse.observe(viewLifecycleOwner){resources->
            if(resources!=null){
                when(resources.status){

                    Status.SUCCESS->{
                        resources.data?.let { it->
                            viewModel.itemList.value=it.items.toMutableList()
                            youtubeVideoAdapter?.list=it.items

                        }
                    }
                    else->Unit
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("video_id", tracker.videoId)
        outState.putFloat("current_time", tracker.currentSecond)
        outState.putBoolean("isRotated",true)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {

        requireActivity().requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        youtubeVideoAdapter=null
        isThereAnyVideoUrl=false
        myYoutubePlayer?.removeListener(listener as AbstractYouTubePlayerListener)
        _binding=null
        super.onDestroy()
    }

    private fun controlOrientation() = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

}