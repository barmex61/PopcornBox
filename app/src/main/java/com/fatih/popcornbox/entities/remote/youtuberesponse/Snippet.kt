package com.fatih.popcornbox.entities.remote.youtuberesponse

data class Snippet(
    val channelTitle: String,
    val description: String,
    val publishedAt: String,
    val thumbnails: Thumbnails,
    val title: String
)