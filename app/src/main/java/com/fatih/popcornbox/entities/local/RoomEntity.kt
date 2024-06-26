package com.fatih.popcornbox.entities.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RoomEntity(
    val favorite:Boolean,
    val name:String,
    val lastAirDate: String,
    val posterPath: String,
    val voteAverage: Double,
    val isTvShow:Boolean,
    @PrimaryKey (autoGenerate = false)
    var field_id: Long
)
