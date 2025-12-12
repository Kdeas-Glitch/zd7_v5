package com.bignerbranch.android.zd7_v5

import androidx.lifecycle.*
import com.bignerbranch.android.zd7_v5.Room.*
import kotlinx.coroutines.launch

class CheckDBViewModel(private val repository: BusDepotRepository) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> get() = _successMessage

    // LiveData
    val buses: LiveData<List<Bus>> = repository.getAllBuses().asLiveData()
    val drivers: LiveData<List<Driver>> = repository.getAllDrivers().asLiveData()
    val routes: LiveData<List<Routes>> = repository.getAllRoutes().asLiveData()
    val routeBusLinks: LiveData<List<RouteBusLink>> = repository.getAllLinks().asLiveData()
    val activeLinks: LiveData<List<RouteBusLink>> = repository.getActiveLinks().asLiveData()

    fun getTitleForType(databaseType: String): String {
        return when (databaseType) {
            "CheckBusses" -> "База данных: Автобусы"
            "CheckDrivers" -> "База данных: Водители"
            "Checkroutes" -> "База данных: Маршруты"
            "CheckRouteBuss" -> "База данных: Связи маршрутов и автобусов"
            else -> "Неизвестная база данных"
        }
    }

    // Генерация тестовых данных
    fun generateTestData() {
        _isLoading.value = true
        _errorMessage.value = ""
        _successMessage.value = ""

        viewModelScope.launch {
            try {
                // 1. Создаем тестовые маршруты
                val route1Id = repository.insertRoute(Routes(map = "Маршрут №1: Центр - Юг"))
                val route2Id = repository.insertRoute(Routes(map = "Маршрут №2: Север - Восток"))
                val route3Id = repository.insertRoute(Routes(map = "Маршрут №3: Кольцевой"))

                // 2. Создаем тестовые автобусы
                val bus1Id = repository.insertBus(Bus(condition = "Хорошее", number = "A123BC", busy = false))
                val bus2Id = repository.insertBus(Bus(condition = "Отличное", number = "B456DE", busy = false))
                val bus3Id = repository.insertBus(Bus(condition = "Требует ремонта", number = "C789FG", busy = false))
                val bus4Id = repository.insertBus(Bus(condition = "Хорошее", number = "D012HI", busy = false))

                // 3. Создаем тестовых водителей
                repository.insertDriver(Driver(
                    name = "Иванов Иван",
                    password = "driver123",
                    baseSalary = 50000.0, // используем baseSalary вместо salary
                    busId = bus1Id.toInt(),
                    basebus = bus1Id.toInt(),
                    bonusMultiplier = 1.0
                    // routesId и salary убраны - их нет в этой версии класса
                ))

                repository.insertDriver(Driver(
                    name = "Петров Петр",
                    password = "pass456",
                    baseSalary = 55000.0, // используем baseSalary вместо salary
                    busId = bus2Id.toInt(),
                    basebus = bus2Id.toInt(),
                    bonusMultiplier = 1.0
                    // routesId и salary убраны
                ))

                // 4. Генерируем связи между маршрутами и автобусами
                repository.generateTestLinks()

                _successMessage.postValue("Тестовые данные успешно созданы!")

            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка генерации данных: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Генерация только связей
    fun generateLinksOnly() {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                repository.generateTestLinks()
                _successMessage.postValue("Связи успешно сгенерированы!")
            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка генерации связей: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Создание конкретной связи
    fun createLink(routesId: Int, busId: Int, driverId: Int? = null) {
        viewModelScope.launch {
            try {
                val linkId = repository.createLink(routesId, busId, driverId)
                _successMessage.postValue("Связь создана (ID: $linkId)")
            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка создания связи: ${e.message}")
            }
        }
    }

    fun loadData(databaseType: String) {
        _isLoading.value = true
        _errorMessage.value = ""
        _successMessage.value = ""

        viewModelScope.launch {
            try {
                // Просто обновляем данные
                kotlinx.coroutines.delay(300) // Небольшая задержка для демонстрации
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка загрузки: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }
}