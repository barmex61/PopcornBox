package com.fatih.popcornbox.ui

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.fatih.popcornbox.R
import com.fatih.popcornbox.other.Constants.isFirstRun
import com.fatih.popcornbox.other.Constants.orientation
import com.fatih.popcornbox.viewmodel.HomeFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var navController: NavController?=null
    private var navHostFragment: NavHostFragment?=null
    private var viewModel: HomeFragmentViewModel?=null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        viewModel= ViewModelProvider(this)[HomeFragmentViewModel::class.java]
        val currentOrientation= resources.configuration.orientation
        if(isFirstRun && currentOrientation == orientation){
            viewModel?.getMovies( "popularity.desc","")
            isFirstRun=false
        }
        orientation=currentOrientation
        WindowCompat.setDecorFitsSystemWindows(window.apply {
            setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                attributes.layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }, false)
        setContentView(R.layout.activity_main)
        setupNavController()
    }

    private fun setupNavController(){
        navHostFragment=supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController=navHostFragment?.navController
    }

    override fun onDestroy() {
        isFirstRun=true
        viewModel=null
        super.onDestroy()
    }

}