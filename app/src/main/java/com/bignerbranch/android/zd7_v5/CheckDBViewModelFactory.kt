package com.bignerbranch.android.zd7_v5

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bignerbranch.android.zd7_v5.Room.BusDepotRepository

class CheckDBViewModelFactory(
    private val repository: BusDepotRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CheckDBViewModel::class.java)) {
            return CheckDBViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}