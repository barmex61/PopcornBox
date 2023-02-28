package com.fatih.popcornbox.entities.remote.reviewresponse

data class ReviewResponse(
    var results: List<ReviewResult>,
    val total_results: Int
)