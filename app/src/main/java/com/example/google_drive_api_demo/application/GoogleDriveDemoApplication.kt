package com.example.google_drive_api_demo.application

import android.app.Application

class GoogleDriveDemoApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        app = this
    }

    companion object {
        @JvmStatic
        lateinit var app: GoogleDriveDemoApplication
            private set
    }

}