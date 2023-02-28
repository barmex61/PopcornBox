package com.fatih.popcornbox.entities.remote.detailresponse

data class ProductionCompany(
    val id: Int,
    val logo_path: String,
    val name: String,
    val origin_country: String
):java.io.Serializable