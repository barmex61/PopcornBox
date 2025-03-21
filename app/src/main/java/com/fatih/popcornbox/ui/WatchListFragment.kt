package com.fatih.popcornbox.ui

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.fatih.popcornbox.R
import com.fatih.popcornbox.adapter.WatchListAdapter
import com.fatih.popcornbox.databinding.FragmentWatchListBinding
import com.fatih.popcornbox.other.Constants.movieSearch
import com.fatih.popcornbox.other.Constants.tvSearch
import com.fatih.popcornbox.viewmodel.DetailsFragmentViewModel

class WatchListFragment:Fragment() {

    private val viewModel: DetailsFragmentViewModel by lazy{
        ViewModelProvider(requireActivity())[DetailsFragmentViewModel::class.java]
    }
    private var _binding: FragmentWatchListBinding?=null
    private val binding: FragmentWatchListBinding
        get() = _binding!!

    private var watchListAdapter: WatchListAdapter?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding= DataBindingUtil.inflate(inflater,R.layout.fragment_watch_list,container,false)
        setStatusBarPadding()
        watchListAdapter= WatchListAdapter(R.layout.watch_list_row).apply {
            setMyOnClickLambda { url, id, pair ,isTvShow->
                val search= if (isTvShow == true) tvSearch else movieSearch
                pair?.let {
                    findNavController().navigate(WatchListFragmentDirections.actionWatchListFragmentToDetailsFragment(id,pair.first,pair.second,url,search))
                }?: findNavController().navigate(WatchListFragmentDirections.actionWatchListFragmentToDetailsFragment(id,R.color.white,R.color.black2,url,search))
            }
        }
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.watchListRecyclerView.apply {
            val columnWidth = resources.getDimensionPixelSize(R.dimen.grid_column_width)
            val spanCount = maxOf(1, Resources.getSystem().displayMetrics.widthPixels / columnWidth)
            layoutManager= GridLayoutManager(requireContext(), spanCount)
            adapter=watchListAdapter
            hasFixedSize()
        }
        observeLiveData()
        return binding.root
    }

    private fun observeLiveData(){
        viewModel.entityList.observe(viewLifecycleOwner) { result->
            watchListAdapter?.list=result
        }
    }

    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    private fun setStatusBarPadding(){
        val statusBarHeightId = resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight = resources.getDimensionPixelSize(statusBarHeightId) +10
        binding.layoutWatchList.updatePadding(top = statusBarHeight)
    }

    override fun onDestroyView() {
        watchListAdapter=null
        _binding=null
        super.onDestroyView()
    }
}