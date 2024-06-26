package com.fatih.popcornbox.movieapi

import com.fatih.popcornbox.BuildConfig.YOUTUBE_API_KEY
import com.fatih.popcornbox.entities.remote.creditsresponse.CreditsResponse
import com.fatih.popcornbox.entities.remote.detailresponse.DetailResponse
import com.fatih.popcornbox.entities.remote.imageresponse.ImageResponse
import com.fatih.popcornbox.entities.remote.reviewresponse.ReviewResponse
import com.fatih.popcornbox.entities.remote.youtuberesponse.YoutubeResponse
import com.fatih.popcornbox.BuildConfig.API_KEY
import com.fatih.popcornbox.entities.remote.discoverresponse.DiscoverResponse
import com.fatih.popcornbox.entities.remote.videoresponse.VideoResponse

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PopcornApi {
    //https://api.themoviedb.org/3/movie/505642/similar?api_key=ae624ef782f69d5092464dffa234178b&language=en-US&page=1
    //https://api.themoviedb.org/3/movie/{movie_id}/recommendations?api_key=ae624ef782f69d5092464dffa234178b&language=en-US&page=1
    //https://api.themoviedb.org/3/movie/505642?api_key=ae624ef782f69d5092464dffa234178b&language=en-US
    //https://api.themoviedb.org/3/tv/100088/watch/providers?api_key=ae624ef782f69d5092464dffa234178b
    //https://youtube.googleapis.com/youtube/v3/videos?part=snippet%2CcontentDetails%2Cstatistics&id=Ks-_Mh1QhMc%2Cc0KYU2j0TM4%2CeIho2S0ZahI&key=AIzaSyDjn7FjfG2kUgWP4D5-w1GoigWWyyT_ZQs
    //https://api.themoviedb.org/3/discover/tv?api_key=ae624ef782f69d5092464dffa234178b&sort_by=popularity.desc&page=1
    //https://api.themoviedb.org/3/search/movie?api_key=ae624ef782f69d5092464dffa234178b&query=Spi&page=1
    //https://api.themoviedb.org/3/tv/85552/videos?api_key=ae624ef782f69d5092464dffa234178b
    //https://api.themoviedb.org/3/movie/1037353/credits?api_key=ae624ef782f69d5092464dffa234178b&language=en-US
    //https://api.themoviedb.org/3/discover/movie?api_key=ae624ef782f69d5092464dffa234178b&sort_by=popularity.desc&page=1&with_genres=80
    //https://api.themoviedb.org/3/movie/505642/reviews?api_key=ae624ef782f69d5092464dffa234178b&language=en-US&page=1

    @GET("{name}/{id}/videos")
    suspend fun getVideos(
        @Path("name")name:String,
        @Path("id")id:Int,
        @Query("api_key") api_key: String =API_KEY):Response<VideoResponse>
    @GET("{name}/{id}/similar")
    suspend fun getFamiliar(
        @Path("name") name:String,
        @Path("id") id:Int,
        @Query("api_key") api_key: String= API_KEY,
        @Query("page") page:Int):Response<DiscoverResponse>
    @GET("{name}/{id}/recommendations")
    suspend fun getRecommendations(
        @Path("name") name:String,
        @Path("id") id:Int,
        @Query("api_key") api_key: String =  API_KEY,
        @Query("page") page:Int):Response<DiscoverResponse>

    @GET("{name}/{id}/reviews")
    suspend fun getReviews(
       @Path("name") name:String,
       @Path("id") id:Int,
       @Query("api_key") api_key: String =  API_KEY,
       @Query("page") page:Int):Response<ReviewResponse>

    @GET("{name}/{id}/credits")
    suspend fun getCredits(
       @Path("name") name: String,
       @Path("id") id: Int,
       @Query("api_key") api_key: String = API_KEY):Response<CreditsResponse>


    @GET("discover/movie")
    suspend fun getMovies(
        @Query("api_key") api_key:String= API_KEY,
        @Query("sort_by") sort_by:String,
        @Query("page") page:Int,
        @Query("with_genres") genres:String
    ):Response<DiscoverResponse>

    @GET("discover/tv")
    suspend fun getTvShows(
        @Query("api_key") api_key:String= API_KEY,
        @Query("sort_by") sort_by:String,
        @Query("page") page:Int,
        @Query("with_genres") genres:String
    ):Response<DiscoverResponse>

    @GET("search/{name}")
    suspend fun search(
        @Path("name") name:String,
        @Query("api_key") api_key: String =  API_KEY,
        @Query("query") query: String,
        @Query("page") page:Int):Response<DiscoverResponse>

    @GET("{name}/{id}")
    suspend fun getDetails(@Path("name") searchName:String,
                           @Path("id") id:Int,
                           @Query("api_key") api_key:String= API_KEY,
                           @Query("language") language:String ):Response<DetailResponse>

    @GET("{name}/{id}/images")
    suspend fun getImages(@Path("name") name:String,
                          @Path("id") id:Int,
                          @Query("api_key") api_key: String = API_KEY):Response<ImageResponse>

    @GET("videos")
    suspend fun getYoutubeVideoDetails(
        @Query("part")part:String,
        @Query("id")id:String,
        @Query("key")key:String =  YOUTUBE_API_KEY): Response<YoutubeResponse>


}