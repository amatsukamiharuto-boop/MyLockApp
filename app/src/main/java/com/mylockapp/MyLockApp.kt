package com.mylockapp

import android.app.Application
import com.mylockapp.di.AppContainer

class MyLockApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
