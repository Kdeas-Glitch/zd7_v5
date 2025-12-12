package com.bignerbranch.android.zd7_v5.Room

import android.app.Application
import com.bignerbranch.android.zd7_v5.Room.BusDepotDatabase
import com.bignerbranch.android.zd7_v5.Room.BusDepotRepository

class BusDepotApplication : Application() {

    val database by lazy { BusDepotDatabase.getDatabase(this) }
    val repository by lazy { BusDepotRepository(database) }

    override fun onCreate() {
        super.onCreate()
        // Инициализация при необходимости
    }
}