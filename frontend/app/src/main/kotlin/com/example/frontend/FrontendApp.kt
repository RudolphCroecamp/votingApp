package com.example.frontend

import android.app.Application
import com.example.frontend.data.AppContainer
import com.example.frontend.data.election.createReminderChannel

class FrontendApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
        createReminderChannel(this)
    }
}
