package com.fatih.popcornbox.ui.tabfragments

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fatih.popcornbox.R
import com.fatih.popcornbox.adapter.CastRecyclerViewAdapter
import com.fatih.popcornbox.databinding.FragmentCastBinding
import com.fatih.popcornbox.other.CastAdapterListener
import com.fatih.popcornbox.other.Status
import com.fatih.popcornbox.ui.DetailsFragment
import com.fatih.popcornbox.viewmodel.DetailsFragmentViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.*


class CastFragment : Fragment(R.layout.fragment_cast) ,CastAdapterListener{

    private var _binding:FragmentCastBinding?=null
    private val binding:FragmentCastBinding
        get() = _binding!!
    private var recyclerView:RecyclerView?=null
    private var job: Job?=null
    private val handler = CoroutineExceptionHandler{ _,throwable->
        println("Caught exception $throwable")
    }
    private lateinit var castAdapter:CastRecyclerViewAdapter
    private lateinit var viewModel: DetailsFragmentViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding= FragmentCastBinding.inflate(inflater,container,false)
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
        viewModel=ViewModelProvider(requireActivity())[DetailsFragmentViewModel::class.java]
        doInitialization()
        return binding.root
    }

    private fun doInitialization(){
        castAdapter=CastRecyclerViewAdapter(R.layout.cast_rview_row,this)
        recyclerView=binding.veilRecyclerView
        val columnWidth = resources.getDimensionPixelSize(R.dimen.grid_column_width)
        val spanCount = maxOf(1, Resources.getSystem().displayMetrics.widthPixels / columnWidth)
        recyclerView!!.layoutManager= GridLayoutManager(requireContext(), spanCount)
        castAdapter.vibrantColor=DetailsFragment.vibrantColor!!
        recyclerView!!.adapter = castAdapter
        observeLiveData()
    }

    override fun setImages(hideProgressBar: Boolean) {
        _binding?.let {
            if ( hideProgressBar && it.castProgressBar.visibility==View.VISIBLE){
                it.castProgressBar.visibility=View.GONE
            }
        }
    }

    private fun observeLiveData(){
        viewModel.creditsResponse.observe(viewLifecycleOwner){
            when(it.status){
                Status.SUCCESS->{
                    it.data?.let {
                        job?.cancel()
                        job=lifecycleScope.launch(Dispatchers.Main + handler){
                            val castList=async(Dispatchers.Default){
                                it.cast.filter {
                                    !it.profile_path.isNullOrEmpty() && it.profile_path != "null"
                                }.distinctBy {
                                    it.profile_path
                                }.map { cast->
                                    Triple(cast.name,cast.character,cast.profile_path!!)
                                }
                            }
                            val crewList=async(Dispatchers.Default){
                                it.crew.filter {
                                    !it.profile_path.isNullOrEmpty() && it.profile_path != "null"
                                }.distinctBy {
                                    it.profile_path
                                }.map { crew->
                                    Triple(crew.name,crew.job,crew.profile_path!!)
                                }
                            }
                            castAdapter.list=castList.await() + crewList.await()
                        }
                    }
                }
                else->Unit
            }
        }
    }

    override fun onDestroyView() {
        job?.cancel()
        recyclerView=null
        _binding=null
        super.onDestroyView()
    }



}