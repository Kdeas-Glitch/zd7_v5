package com.bignerbranch.android.zd7_v5.Room

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class BusDepotViewModel(private val repository: BusDepotRepository) : ViewModel() {

    // LiveData потоки
    val allDrivers = repository.getAllDrivers().asLiveData()
    val allBuses = repository.getAllBuses().asLiveData()
    val allRoutes = repository.getAllRoutes().asLiveData()  // ← Теперь работает
    val allLinks = repository.getAllLinks().asLiveData()

    // Driver operations
    fun insertDriver(driver: Driver) = viewModelScope.launch {
        repository.insertDriver(driver)
    }

    fun updateDriver(driver: Driver) = viewModelScope.launch {
        repository.updateDriver(driver)
    }

    fun deleteDriver(driver: Driver) = viewModelScope.launch {
        repository.deleteDriver(driver)
    }
    suspend fun getDriverByName(name: String): Driver? {
        return repository.getDriverByName(name)
    }
    fun getPasswordByName(name: String): Driver? {
        return try {
            // Синхронный вызов через runBlocking
            runBlocking {
                repository.getDriverByName(name)
            }
        } catch (e: Exception) {
            Log.e("ViewModel", "Error: ${e.message}")
            null
        }
    }

    // Bus operations
    fun insertBus(bus: Bus) = viewModelScope.launch {
        repository.insertBus(bus)
    }

    fun updateBus(bus: Bus) = viewModelScope.launch {
        repository.updateBus(bus)
    }

    fun deleteBus(bus: Bus) = viewModelScope.launch {
        repository.deleteBus(bus)
    }

    // Routes operations - ДОБАВЬТЕ ЭТИ МЕТОДЫ!
    fun insertRoute(route: Routes) = viewModelScope.launch {
        repository.insertRoute(route)
    }

    fun updateRoute(route: Routes) = viewModelScope.launch {
        repository.updateRoute(route)
    }

    fun deleteRoute(route: Routes) = viewModelScope.launch {
        repository.deleteRoute(route)
    }

    // Link operations
    fun createLink(routesId: Int, busId: Int) = viewModelScope.launch {
        repository.createLink(routesId, busId)
    }
}

class BusDepotViewModelFactory(
    private val repository: BusDepotRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BusDepotViewModel::class.java)) {
            return BusDepotViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}