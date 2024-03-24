package com.fatih.popcornbox.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fatih.popcornbox.entities.remote.videoresponse.VideoResponse
import com.fatih.popcornbox.entities.remote.youtuberesponse.YoutubeResponse
import com.fatih.popcornbox.entities.remote.youtuberesponse.İtem
import com.fatih.popcornbox.other.Resource
import com.fatih.popcornbox.other.Status
import com.fatih.popcornbox.other.updateList
import com.fatih.popcornbox.repository.PopcornRepositoryInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrailerFragmentViewModel @Inject constructor(private val popcornRepository: PopcornRepositoryInterface) : ViewModel() {

    private val _videoResponse=MutableLiveData<Resource<VideoResponse>>()
    val videoResponse:LiveData<Resource<VideoResponse>>
        get() = _videoResponse

    private val _youtubeResponse=MutableLiveData<Resource<YoutubeResponse>>()
    val youtubeResponse:LiveData<Resource<YoutubeResponse>>
        get() = _youtubeResponse

    var selectedPosition=MutableLiveData<Int>()
    var selectedItem=MutableLiveData<İtem>()
    var itemList=MutableLiveData<MutableList<İtem>>()

    fun updateList(position:Int,item:İtem,listLambda:(List<İtem>)->Unit)=viewModelScope.launch(Dispatchers.Main){
        itemList.value?.removeAt(position)
        selectedPosition.value=position
        selectedItem.value=item
        itemList.value?.add(selectedPosition.value!!,selectedItem.value!!)
        listLambda(itemList.value?.toList()?: listOf())
    }

    fun getVideos(name: String, id: Int)=viewModelScope.launch {
        _videoResponse.value= Resource.loading(null)
        _videoResponse.value=popcornRepository.getVideos(name, id)
    }

    fun resetData(){
        selectedPosition=MutableLiveData<Int>()
        selectedItem=MutableLiveData<İtem>()
        itemList=MutableLiveData<MutableList<İtem>>()
    }

    fun getYoutubeResponse(part:String,id:String)=viewModelScope.launch {
        _youtubeResponse.value= Resource.loading(_youtubeResponse.value?.data)
        val response=popcornRepository.getYoutubeVideoDetails(part, id)
        when(response.status){
            Status.SUCCESS->{
                response.data?.let {
                    _youtubeResponse.value=if (selectedPosition.value != null){
                        Resource.success(it.updateList(selectedPosition.value!!))
                    }else{
                        Resource.success(it)
                    }
                }
            }
            else->{
                _youtubeResponse.value= Resource.error(response.message)
            }
        }
    }
}