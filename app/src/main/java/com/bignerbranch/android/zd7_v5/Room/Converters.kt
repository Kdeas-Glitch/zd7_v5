package com.bignerbranch.android.zd7_v5.Room

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromBoolean(value: Boolean): Int {
        return if (value) 1 else 0
    }

    @TypeConverter
    fun toBoolean(value: Int): Boolean {
        return value == 1
    }
}