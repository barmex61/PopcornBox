package com.fatih.popcornbox.ui

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PopcornApplication:Application(){


    companion object{
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()

        appContext=applicationContext
    }
}