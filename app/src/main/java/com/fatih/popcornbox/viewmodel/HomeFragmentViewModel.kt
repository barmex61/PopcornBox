package com.fatih.popcornbox.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fatih.popcornbox.entities.remote.discoverresponse.DiscoverResponse
import com.fatih.popcornbox.other.*
import com.fatih.popcornbox.other.Constants.movieSearch
import com.fatih.popcornbox.other.Constants.stateList
import com.fatih.popcornbox.repository.PopcornRepositoryInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeFragmentViewModel @Inject constructor(private val popcornRepo:PopcornRepositoryInterface):ViewModel() {

    private val _discoverData=MutableLiveData<Resource<DiscoverResponse?>>()
    val discoverData:LiveData<Resource<DiscoverResponse?>>
        get() = _discoverData

    var currentPage=MutableLiveData(1)
    var searchQuery=MutableLiveData("")



    fun getMovies(sort_by:String,genres:String)=viewModelScope.launch{
        if(stateList.last()!=State.MOVIE ){
            currentPage.value=1
            _discoverData.value?.let {
                it.data=null
            }
        }
        stateList.addFilter(State.MOVIE)
        _discoverData.value=Resource.loading(_discoverData.value?.data)
        val response=popcornRepo.getMovies(sort_by, currentPage.value!!, genres)
        when(response.status){
            Status.SUCCESS->{
                _discoverData.value= Resource.success(_discoverData.value!!.data.add(response.data!!.apply {
                    this.genres=genres
                    this.sortBy=sort_by
                } ))
            }
            Status.ERROR->{
                _discoverData.value= Resource.error(response.message)
            }
            else->Unit
        }

    }

    fun getTvShows(sort_by:String,genres:String )=viewModelScope.launch{
        if(stateList.last()!=State.TV_SHOW ){
            currentPage.value=1
            _discoverData.value?.let {
                it.data=null
            }
        }
        stateList.addFilter(State.TV_SHOW)
        _discoverData.value=Resource.loading(_discoverData.value?.data)
        val response=popcornRepo.getTvShows(sort_by, currentPage.value!!, genres)
        when(response.status){
            Status.SUCCESS->{
                _discoverData.value= Resource.success(_discoverData.value!!.data.add(response.data!!.apply {
                    this.genres=genres
                    this.sortBy=sort_by
                } ))
            }
            Status.ERROR->{
                _discoverData.value= Resource.error("Error occurred")
            }
            else->Unit
        }

    }

    fun search(name:String,query:String,buttonClicked:Boolean)=viewModelScope.launch{
        if(buttonClicked){
            if(name == movieSearch) stateList.addFilter(State.MOVIE) else stateList.addFilter(State.TV_SHOW)
            _discoverData.value=Resource.loading(null)
        }
        stateList.addFilter(State.SEARCH)
        val response=popcornRepo.search(name, query, currentPage.value!!)
        _discoverData.value=Resource.loading(_discoverData.value?.data)
        when(response.status){
            Status.SUCCESS->{
                if(searchQuery.value!=query || buttonClicked){
                    _discoverData.value= Resource.success(response.data)
                }else{
                    _discoverData.value= Resource.success(_discoverData.value!!.data.add(response.data!!))
                }
            }
            Status.ERROR->{
                _discoverData.value= Resource.error("Error occurred")
            }
            else->Unit
        }
        searchQuery.value=query

    }
    fun resetData(){
        _discoverData.value=Resource.loading(null)
        currentPage.value=1
    }
}