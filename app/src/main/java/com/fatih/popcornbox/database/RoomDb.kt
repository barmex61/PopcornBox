package com.fatih.popcornbox.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fatih.popcornbox.entities.local.RoomEntity

@Database(entities = [RoomEntity::class], version = 2, exportSchema = false)
abstract class RoomDb :RoomDatabase() {
    abstract fun roomDao():RoomDao
}