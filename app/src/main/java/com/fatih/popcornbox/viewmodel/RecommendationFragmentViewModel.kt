package com.fatih.popcornbox.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fatih.popcornbox.entities.remote.discoverresponse.DiscoverResponse
import com.fatih.popcornbox.other.Resource
import com.fatih.popcornbox.other.Status
import com.fatih.popcornbox.other.recommend
import com.fatih.popcornbox.repository.PopcornRepositoryInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendationFragmentViewModel @Inject constructor(private val popcornRepository: PopcornRepositoryInterface) :ViewModel(){


    private var _discoverResponse=MutableLiveData<Resource<DiscoverResponse>>()
    val discoverResponse:LiveData<Resource<DiscoverResponse>>
        get() = _discoverResponse

    var recommendationCurrentPage=MutableLiveData(1)

    fun getRecommendations(name: String, id: Int)=viewModelScope.launch {
        _discoverResponse.value= Resource.loading(_discoverResponse.value?.data)
        val response=popcornRepository.getRecommendations(name, id, recommendationCurrentPage.value!!)
        when(response.status){
            Status.SUCCESS->{
                _discoverResponse.value= Resource.success(_discoverResponse.value?.data.recommend(response.data!!.apply {
                    recommendationId=id
                }))
            }
            Status.ERROR->{
                _discoverResponse.value= Resource.error(response.message)
            }
            else->Unit
        }
    }

    fun resetData(){
        _discoverResponse=MutableLiveData<Resource<DiscoverResponse>>()
        recommendationCurrentPage=MutableLiveData(1)
    }


}