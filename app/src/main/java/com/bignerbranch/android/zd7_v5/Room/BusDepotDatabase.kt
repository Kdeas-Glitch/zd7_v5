package com.bignerbranch.android.zd7_v5.Room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Routes::class,
        Bus::class,
        Driver::class,
        RouteBusLink::class
    ],
    version =7,
    exportSchema = true
)
abstract class BusDepotDatabase : RoomDatabase() {

    abstract fun routesDao(): RoutesDao
    abstract fun busDao(): BusDao
    abstract fun driverDao(): DriverDao
    abstract fun routeBusLinkDao(): RouteBusLinkDao

    companion object {
        @Volatile
        private var INSTANCE: BusDepotDatabase? = null

        private const val DATABASE_NAME = "bus_depot.db"

        fun getDatabase(context: Context): BusDepotDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BusDepotDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}