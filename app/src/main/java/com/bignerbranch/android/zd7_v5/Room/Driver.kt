package com.bignerbranch.android.zd7_v5.Room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Drivers")
data class Driver(
    @PrimaryKey(autoGenerate = true)
    val drivId: Int = 0,

    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "password")
    var password: String = "",

    @ColumnInfo(name = "baseSalary")
    var baseSalary: Double,

    @ColumnInfo(name = "routesId", defaultValue = "")
    var routesId: String = "",

    @ColumnInfo(name = "busId")
    var busId: Int,

    @ColumnInfo(name = "baseBus")
    var basebus: Int,

    @ColumnInfo(name = "bonusMultiplier", defaultValue = "1.0")
    var bonusMultiplier: Double = 1.0
)