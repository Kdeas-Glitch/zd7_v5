package com.bignerbranch.android.zd7_v5.Room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Routes")
data class Routes(
    @PrimaryKey(autoGenerate = true)
    val routesId: Int = 0,

    @ColumnInfo(name = "map")
    var map: String
)