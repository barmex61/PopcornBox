package com.fatih.popcornbox.ui.tabfragments

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.fatih.popcornbox.R
import com.fatih.popcornbox.adapter.RecommendFragmentAdapter
import com.fatih.popcornbox.databinding.FragmentRecommendBinding
import com.fatih.popcornbox.other.Constants
import com.fatih.popcornbox.other.Constants.movieSearch
import com.fatih.popcornbox.other.Constants.tvSearch
import com.fatih.popcornbox.other.Status
import com.fatih.popcornbox.ui.DetailsFragment
import com.fatih.popcornbox.viewmodel.RecommendationFragmentViewModel

class RecommendFragment:Fragment(R.layout.fragment_recommend) {

    private var _binding: FragmentRecommendBinding?=null
    private val binding: FragmentRecommendBinding
        get() = _binding!!
    private var recyclerView: RecyclerView?=null
    private var totalAvailablePages=2
    private lateinit var onScrollListener:RecyclerView.OnScrollListener
    private var selectedId=1
    private lateinit var recommendAdapter: RecommendFragmentAdapter
    private lateinit var viewModel: RecommendationFragmentViewModel
    private var lottieView : LottieAnimationView ?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding= FragmentRecommendBinding.inflate(inflater,container,false)
        lottieView = binding.recommendationLottie
        viewModel= ViewModelProvider(requireActivity())[RecommendationFragmentViewModel::class.java]
        recommendAdapter= RecommendFragmentAdapter(R.layout.fragment_recommend_row)
        selectedId=arguments?.getInt("id")?:selectedId
        if (savedInstanceState?.getBoolean("isRotated") != true){
            viewModel.resetData()
        }
        recommendAdapter.setMyOnClickLambda {url, id, pair ,isTvShow->
            pair?.let {
                findNavController().navigate(R.id.action_detailsFragment_self, bundleOf("id" to id,"vibrantColor" to it.first,"darkMutedColor" to it.second,"url" to url,"isTvShow" to if (DetailsFragment.isItInMovieList) movieSearch else tvSearch)
                    ,NavOptions.Builder().setPopUpTo(R.id.detailsFragment,true).build())
            }?: findNavController().navigate(R.id.action_detailsFragment_self, bundleOf("id" to id,"vibrantColor" to R.color.white,"darkMutedColor" to R.color.black2,"url" to url,"isTvShow" to null,"isTvShow" to if (DetailsFragment.isItInMovieList) movieSearch else tvSearch),
                NavOptions.Builder().setPopUpTo(R.id.detailsFragment,true).build())
        }
        doInitialization()
        return binding.root
    }

    private fun doInitialization(){
        recyclerView=binding.recommendRecyclerView
        val columnWidth = resources.getDimensionPixelSize(R.dimen.grid_column_width)
        val spanCount = maxOf(1, Resources.getSystem().displayMetrics.widthPixels / columnWidth)
        recyclerView!!.layoutManager= GridLayoutManager(requireContext(), spanCount)
        recyclerView!!.adapter = recommendAdapter
        onScrollListener=object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && viewModel.recommendationCurrentPage.value!! < totalAvailablePages) {
                    viewModel.recommendationCurrentPage.value= viewModel.recommendationCurrentPage.value!! +1
                    if (DetailsFragment.isItInMovieList){
                        viewModel.getRecommendations(movieSearch,selectedId)
                    }else{
                        viewModel.getRecommendations(tvSearch,selectedId)
                    }
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        }
        recyclerView!!.addOnScrollListener(onScrollListener)
        if (DetailsFragment.isItInMovieList){
            viewModel.getRecommendations(movieSearch,selectedId)
        }else{
            viewModel.getRecommendations(tvSearch,selectedId)
        }
        observeLiveData()
    }

    private fun observeLiveData(){
        viewModel.discoverResponse.observe(viewLifecycleOwner){
            when(it.status){
                Status.SUCCESS->{
                    it.data?.let {
                        totalAvailablePages=it.total_pages
                        recommendAdapter.list=it.results
                    }?:{
                        showLottie()
                    }
                    if (it.data?.total_results == 0){
                        showLottie()
                    }
                }
                Status.ERROR->{
                    showLottie()
                }
                else->Unit
            }
        }
    }

    private fun showLottie(){
        lottieView?.visibility=View.VISIBLE
        lottieView?.playAnimation()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("isRotated",true)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        lottieView = null
        recyclerView?.adapter = null
        recyclerView=null
        _binding=null
        super.onDestroyView()
    }
}