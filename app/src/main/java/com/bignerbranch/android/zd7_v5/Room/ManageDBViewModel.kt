package com.bignerbranch.android.zd7_v5.Room

import androidx.lifecycle.*
import com.bignerbranch.android.zd7_v5.Room.*
import kotlinx.coroutines.launch

class ManageDBViewModel(private val repository: BusDepotRepository) : ViewModel() {

    private val _statusMessage = MutableLiveData("")
    val statusMessage: LiveData<String> get() = _statusMessage

    private val _errorMessage = MutableLiveData("")
    val errorMessage: LiveData<String> get() = _errorMessage

    val buses: LiveData<List<Bus>> = repository.getAllBuses().asLiveData()
    val drivers: LiveData<List<Driver>> = repository.getAllDrivers().asLiveData()
    val routes: LiveData<List<Routes>> = repository.getAllRoutes().asLiveData()

    fun loadAllData() {
        viewModelScope.launch {
            try {
                // Просто обновляем LiveData
                _statusMessage.value = "Данные загружены"
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки: ${e.message}"
            }
        }
    }

    // Bus operations
    fun addBus(bus: Bus) {
        viewModelScope.launch {
            try {
                repository.insertBus(bus)
                _statusMessage.postValue("Автобус добавлен успешно")
            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка добавления автобуса: ${e.message}")
            }
        }
    }

    suspend fun getBusById(id: Int): Bus? {
        return repository.getBusById(id)
    }

    fun updateBus(bus: Bus) {
        viewModelScope.launch {
            try {
                repository.updateBus(bus)
                _statusMessage.postValue("Автобус обновлен успешно")
            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка обновления автобуса: ${e.message}")
            }
        }
    }

    fun deleteBus(bus: Bus) {
        viewModelScope.launch {
            try {
                repository.deleteBus(bus)
                _statusMessage.postValue("Автобус удален успешно")
            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка удаления автобуса: ${e.message}")
            }
        }
    }

    // Driver operations
    fun addDriver(driver: Driver) {
        viewModelScope.launch {
            try {
                repository.insertDriver(driver)
                _statusMessage.postValue("Водитель добавлен успешно")
            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка добавления водителя: ${e.message}")
            }
        }
    }

    suspend fun getDriverById(id: Int): Driver? {
        return repository.getDriverById(id)
    }

    fun updateDriver(driver: Driver) {
        viewModelScope.launch {
            try {
                repository.updateDriver(driver)
                _statusMessage.postValue("Водитель обновлен успешно")
            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка обновления водителя: ${e.message}")
            }
        }
    }

    fun deleteDriver(driver: Driver) {
        viewModelScope.launch {
            try {
                repository.deleteDriver(driver)
                _statusMessage.postValue("Водитель удален успешно")
            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка удаления водителя: ${e.message}")
            }
        }
    }

    // Routes operations
    fun addRoute(route: Routes) {
        viewModelScope.launch {
            try {
                repository.insertRoute(route)
                _statusMessage.postValue("Маршрут добавлен успешно")
            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка добавления маршрута: ${e.message}")
            }
        }
    }

    suspend fun getRouteById(id: Int): Routes? {
        return repository.getRouteById(id)
    }

    fun updateRoute(route: Routes) {
        viewModelScope.launch {
            try {
                repository.updateRoute(route)
                _statusMessage.postValue("Маршрут обновлен успешно")
            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка обновления маршрута: ${e.message}")
            }
        }
    }

    fun deleteRoute(route: Routes) {
        viewModelScope.launch {
            try {
                repository.deleteRoute(route)
                _statusMessage.postValue("Маршрут удален успешно")
            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка удаления маршрута: ${e.message}")
            }
        }
    }
    val driversWithDetails: LiveData<List<DriverWithDetails>> =
        repository.getAllDriversWithDetails().asLiveData()

    // Назначить водителя на маршрут
    fun assignDriverToRoute(driverId: Int, routesId: Int, busId: Int) {
        viewModelScope.launch {
            try {
                val linkId = repository.assignDriverToRoute(driverId, routesId, busId)
                _statusMessage.postValue("Водитель назначен на маршрут! ID связи: $linkId")
            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка назначения: ${e.message}")
            }
        }
    }

    // Убрать водителя с маршрута
    fun removeDriverFromRoute(linkId: Long, driverId: Int) {
        viewModelScope.launch {
            try {
                repository.removeDriverFromRoute(linkId, driverId)
                _statusMessage.postValue("Водитель снят с маршрута")
            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка: ${e.message}")
            }
        }
    }

    // Получить маршруты водителя
    fun getDriverRoutes(driverId: Int): LiveData<List<RouteBusLink>> {
        return repository.getRoutesByDriver(driverId).asLiveData()
    }

    // Создать водителя с базовой зарплатой
    fun addDriverWithSalary(name: String, password: String, baseSalary: Double, busId: Int) {
        viewModelScope.launch {
            try {
                val driver = Driver(
                    name = name,
                    password = password,
                    baseSalary = baseSalary,
                    busId = busId,
                    basebus = busId,
                    bonusMultiplier = 1.0
                )
                repository.insertDriver(driver)
                _statusMessage.postValue("Водитель добавлен с зарплатой: $baseSalary ₽")
            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка: ${e.message}")
            }
        }
    }
}

class ManageDBViewModelFactory(
    private val repository: BusDepotRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManageDBViewModel::class.java)) {
            return ManageDBViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
