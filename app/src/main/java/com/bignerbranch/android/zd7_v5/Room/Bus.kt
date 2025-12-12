package com.bignerbranch.android.zd7_v5.Room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Busses")
data class Bus(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "busId")
    val busId: Int = 0,

    @ColumnInfo(name = "condition")
    var condition: String,

    @ColumnInfo(name = "number")
    var number: String,

    @ColumnInfo(name = "busy")
    var busy: Boolean
)