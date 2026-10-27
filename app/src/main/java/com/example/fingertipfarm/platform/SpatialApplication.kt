package com.example.fingertipfarm.platform

import android.app.Application
import com.pico.spatial.ui.foundation.dsl.launch
import com.example.fingertipfarm.mainApp

class SpatialApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        launch(::mainApp)
    }
}
