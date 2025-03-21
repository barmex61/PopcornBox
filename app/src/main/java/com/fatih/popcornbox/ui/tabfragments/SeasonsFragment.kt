package com.fatih.popcornbox.ui.tabfragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.fatih.popcornbox.R
import com.fatih.popcornbox.adapter.SeasonAdapter
import com.fatih.popcornbox.databinding.FragmentSeasonsBinding
import com.fatih.popcornbox.other.Status
import com.fatih.popcornbox.viewmodel.DetailsFragmentViewModel
import javax.inject.Inject


class SeasonsFragment @Inject constructor() : Fragment() {

    private val viewModel:DetailsFragmentViewModel by lazy{
        ViewModelProvider(requireActivity())[DetailsFragmentViewModel::class.java]
    }
    private var _binding:FragmentSeasonsBinding?=null
    private val binding:FragmentSeasonsBinding
        get() = _binding!!

    private var seasonAdapter:SeasonAdapter?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding=DataBindingUtil.inflate(inflater,R.layout.fragment_seasons,container,false)

        seasonAdapter= SeasonAdapter(R.layout.season_layout_row)
        binding.episodesRecyclerView.apply {
            layoutManager=LinearLayoutManager(requireContext())
            adapter=seasonAdapter
            hasFixedSize()
        }
        observeLiveData()
        return binding.root
    }

    private fun observeLiveData(){
        viewModel.detailResponse.observe(viewLifecycleOwner) { resource ->
            if (resource != null) {
                when (resource.status) {

                    Status.SUCCESS -> {
                        resource.data?.let {
                            seasonAdapter!!.list=it.seasons?: listOf()
                        }?:{
                            binding.seasonLottie.apply {
                                visibility=View.VISIBLE
                                playAnimation()
                            }
                        }
                    }
                    Status.ERROR->{
                        binding.seasonLottie.apply {
                            visibility=View.VISIBLE
                            playAnimation()
                        }
                    }
                    else->Unit
                }
            }
        }
    }

    override fun onDestroyView() {
        seasonAdapter=null
        _binding=null
        super.onDestroyView()
    }
}