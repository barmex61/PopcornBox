package com.fatih.popcornbox.viewmodel

import androidx.lifecycle.*
import com.fatih.popcornbox.entities.local.RoomEntity
import com.fatih.popcornbox.entities.remote.creditsresponse.CreditsResponse
import com.fatih.popcornbox.entities.remote.detailresponse.DetailResponse
import com.fatih.popcornbox.entities.remote.imageresponse.ImageResponse
import com.fatih.popcornbox.entities.remote.reviewresponse.ReviewResponse
import com.fatih.popcornbox.other.Resource
import com.fatih.popcornbox.repository.PopcornRepositoryInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DetailsFragmentViewModel @Inject constructor(private val popcornRepo: PopcornRepositoryInterface):ViewModel() {

    private var _detailResponse=MutableLiveData<Resource<DetailResponse>>()
    val detailResponse:LiveData<Resource<DetailResponse>>
    get() = _detailResponse

    private var _imageResponse=MutableLiveData<Resource<ImageResponse>>()
    val imageResponse:LiveData<Resource<ImageResponse>>
    get() = _imageResponse

    private var _isItInDatabase = MutableLiveData<Boolean?>()
    val isItInDatabase:LiveData<Boolean?>
    get() = _isItInDatabase

    private var _isItInFavorite = MutableLiveData<Boolean?>()
    val isItInFavorite:LiveData<Boolean?>
        get() = _isItInFavorite

    private var _creditsResponse=MutableLiveData<Resource<CreditsResponse>>()
    val creditsResponse:LiveData<Resource<CreditsResponse>>
    get() = _creditsResponse

    private var _reviewResponse=MutableLiveData<Resource<ReviewResponse>>()
    val reviewResponse:LiveData<Resource<ReviewResponse>>
    get() = _reviewResponse

    var entityList=popcornRepo.getAllRoomEntity()

    fun getDetails(searchName:String,id:Int,language:String) {

       _detailResponse= popcornRepo.getDetails(searchName,id, language).catch {
           it.printStackTrace()
       }.asLiveData(viewModelScope.coroutineContext) as MutableLiveData<Resource<DetailResponse>>
       getImages(searchName,id)
       getCredits(searchName,id)
    }

    private fun getImages(searchName : String , id : Int)=viewModelScope.launch {
        _imageResponse.value=Resource.loading(null)
        _imageResponse = popcornRepo.getImages(searchName,id).catch {
            it.printStackTrace()
        }.asLiveData(viewModelScope.coroutineContext) as MutableLiveData<Resource<ImageResponse>>

    }

    fun insertRoomEntity(roomEntity: RoomEntity)=viewModelScope.launch(Dispatchers.Default) {
        popcornRepo.insertRoomEntity(roomEntity)
        isItIntDatabase(roomEntity.field_id.toInt())
    }
    fun deleteRoomEntity(idInput: Int)=viewModelScope.launch(Dispatchers.Default) {
        popcornRepo.deleteRoomEntity(idInput)
        isItIntDatabase(idInput)
    }

    fun updateFavorite(idInput: Int,isFavorite:Boolean)=viewModelScope.launch {
        popcornRepo.updateFavorite(idInput, isFavorite)
        isItIntDatabase(idInput)
    }

    fun isItIntDatabase(idInput:Int)  = viewModelScope.launch(Dispatchers.Default){
        val response=popcornRepo.getSelectedRoomEntity(idInput)
        withContext(Dispatchers.Main){
            _isItInDatabase.value=response?.first
            _isItInFavorite.value=response?.second
        }
    }
    fun getCredits(name:String,id:Int)=viewModelScope.launch {
        _creditsResponse.value= Resource.loading(null)
        _creditsResponse.value=popcornRepo.getCredits(name,id)
    }

    fun resetData()=viewModelScope.launch {
        _detailResponse=MutableLiveData<Resource<DetailResponse>>()
        _imageResponse=MutableLiveData<Resource<ImageResponse>>()
        _isItInDatabase= MutableLiveData<Boolean?>()
        _isItInFavorite=MutableLiveData<Boolean?>()
        _creditsResponse=MutableLiveData<Resource<CreditsResponse>>()
        _reviewResponse=MutableLiveData<Resource<ReviewResponse>>()
    }

    fun getReviews(name:String,id:Int,page:Int)=viewModelScope.launch {
        _reviewResponse.value=Resource.loading(null)
        _reviewResponse.value=popcornRepo.getReviews(name, id, page)

    }

}