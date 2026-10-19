package com.example.frontend

import android.app.Application
import com.example.frontend.data.AppContainer

class FrontendApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }
}
