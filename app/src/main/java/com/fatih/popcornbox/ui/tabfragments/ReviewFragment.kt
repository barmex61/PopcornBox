package com.fatih.popcornbox.ui.tabfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.fatih.popcornbox.R
import com.fatih.popcornbox.adapter.ReviewAdapter
import com.fatih.popcornbox.databinding.FragmentReviewBinding
import com.fatih.popcornbox.entities.remote.reviewresponse.ReviewResult
import com.fatih.popcornbox.other.Constants.checkIsItInMovieListOrNot
import com.fatih.popcornbox.other.Constants.movieSearch
import com.fatih.popcornbox.other.Constants.tvSearch
import com.fatih.popcornbox.other.Status
import com.fatih.popcornbox.viewmodel.DetailsFragmentViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class ReviewFragment:Fragment(R.layout.fragment_review) {

    private var _binding:FragmentReviewBinding?=null
    private val binding:FragmentReviewBinding
        get() = _binding!!
    private var recyclerView:RecyclerView?=null
    private var adapter: ReviewAdapter?=null
    private var viewModel:DetailsFragmentViewModel?=null
    private var selectedId:Int=1
    private var position=0
    private var resultList= listOf<ReviewResult>()
    private var vibrantColor : Int=0
    private var lottieView : LottieAnimationView ?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding=DataBindingUtil.inflate(inflater,R.layout.fragment_review,container,false)
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
        selectedId= arguments?.getInt("id") ?:selectedId
        vibrantColor = arguments?.getInt("vibrantColor") ?: vibrantColor
        viewModel=ViewModelProvider(requireActivity())[DetailsFragmentViewModel::class.java]
        if (checkIsItInMovieListOrNot()){
            viewModel?.getReviews(movieSearch,selectedId,1)
        }else{
            viewModel?.getReviews(tvSearch,selectedId,1)
        }
        lottieView = binding.lottieView
        adapter= ReviewAdapter(R.layout.review_recycler_row)
        recyclerView=binding.reciewRecyclerView
        recyclerView!!.layoutManager=LinearLayoutManager(requireContext())
        recyclerView!!.adapter=adapter
        val spinnerArray= arrayOf(resources.getString(R.string.date),resources.getString(R.string.rating))
        val arrayAdapter=ArrayAdapter(requireContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,spinnerArray)
        binding.spinner.apply {
            adapter=arrayAdapter
            onItemSelectedListener=object:AdapterView.OnItemSelectedListener{
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if(this@ReviewFragment.position!=position){
                        this@ReviewFragment.position=position
                        val sortedList=  if (position==0){
                            resultList.sortedByDescending {
                                it.updated_at
                            }

                        }else{
                            resultList.sortedByDescending {
                                it.author_details.rating
                            }
                        }
                        this@ReviewFragment.adapter?.list=sortedList
                        this@ReviewFragment.recyclerView?.smoothScrollToPosition(0)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?)=Unit
            }
        }
        observeLiveData()
        return binding.root
    }


    private fun observeLiveData(){
        viewModel?.reviewResponse?.observe(viewLifecycleOwner){
            when(it.status){
                Status.LOADING->{}
                Status.ERROR->{showLottie()}
                Status.SUCCESS->{

                    if(it.data?.results?.isEmpty() == true){
                        showLottie()
                    }
                    it.data?.let {
                        resultList=it.results
                        adapter?.list=resultList
                    }
                    binding.count=it.data?.total_results
                }
            }
        }
    }

    private fun showLottie(){
        binding.sortText.visibility=View.GONE
        binding.reviewCountText.visibility=View.GONE
        lottieView?.apply {
            visibility=View.VISIBLE
            playAnimation()
        }
    }


    override fun onDestroyView() {
        lottieView = null
        resultList= listOf()
        adapter=null
        recyclerView=null
        viewModel=null
        _binding=null
        super.onDestroyView()
    }

}