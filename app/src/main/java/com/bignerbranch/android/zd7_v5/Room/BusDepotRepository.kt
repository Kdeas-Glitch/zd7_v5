package com.bignerbranch.android.zd7_v5.Room

import kotlinx.coroutines.flow.Flow

class BusDepotRepository(private val database: BusDepotDatabase) {

    // Driver operations
    fun getAllDrivers(): Flow<List<Driver>> = database.driverDao().getAll()

    suspend fun insertDriver(driver: Driver): Long = database.driverDao().insert(driver)

    suspend fun updateDriver(driver: Driver) = database.driverDao().update(driver)

    suspend fun deleteDriver(driver: Driver) = database.driverDao().delete(driver)

    suspend fun getDriverById(id: Int): Driver? = database.driverDao().getById(id)

    suspend fun getDriverByName(name: String): Driver? {
        return database.driverDao().getDriverByName(name)
    }


    // Bus operations
    fun getAllBuses(): Flow<List<Bus>> = database.busDao().getAll()

    suspend fun insertBus(bus: Bus): Long = database.busDao().insert(bus)

    suspend fun updateBus(bus: Bus) = database.busDao().update(bus)

    suspend fun deleteBus(bus: Bus) = database.busDao().delete(bus)

    suspend fun getBusById(id: Int): Bus? = database.busDao().getById(id)

    // Routes operations - ДОБАВЬТЕ ЭТИ МЕТОДЫ!
    fun getAllRoutes(): Flow<List<Routes>> = database.routesDao().getAll()

    suspend fun insertRoute(route: Routes): Long = database.routesDao().insert(route)

    suspend fun updateRoute(route: Routes) = database.routesDao().update(route)

    suspend fun deleteRoute(route: Routes) = database.routesDao().delete(route)

    suspend fun getRouteById(id: Int): Routes? = database.routesDao().getById(id)

    // RouteBusLink operations

    suspend fun createLinkWithDriver(
        routesId: Int,
        busId: Int,
        driverId: Int,  // Добавляем обязательный параметр
        isActive: Boolean = true,
        notes: String? = null
    ): Long {
        val link = RouteBusLink(
            routesId = routesId,
            busId = busId,
            driverId = driverId,  // Теперь передаем
            isActive = isActive,
            notes = notes
        )
        return database.routeBusLinkDao().insert(link)
    }
    fun getAllLinks(): Flow<List<RouteBusLink>> = database.routeBusLinkDao().getAll()

    fun getActiveLinks(): Flow<List<RouteBusLink>> = database.routeBusLinkDao().getActiveLinks()

    fun getLinksByRoute(routesId: Int): Flow<List<RouteBusLink>> =
        database.routeBusLinkDao().getLinksByRoute(routesId)

    fun getLinksByBus(busId: Int): Flow<List<RouteBusLink>> =
        database.routeBusLinkDao().getLinksByBus(busId)

    // Автоматическое создание связи
    suspend fun createLink(
        routesId: Int,
        busId: Int,
        driverId: Int? = null,
        isActive: Boolean = true,
        notes: String? = null
    ): Long {
        return database.routeBusLinkDao().createOrUpdateLink(
            routesId = routesId,
            busId = busId,
            driverId = driverId,
            isActive = isActive,
            notes = notes
        )
    }

    // Создание связи с автобусом и водителем
    suspend fun assignBusToRoute(
        routeId: Int,
        busId: Int,
        driverId: Int? = null
    ): Long {
        // Проверяем существование маршрута и автобуса
        val route = database.routesDao().getById(routeId)
        val bus = database.busDao().getById(busId)

        if (route == null) throw IllegalArgumentException("Маршрут не найден")
        if (bus == null) throw IllegalArgumentException("Автобус не найден")

        // Автоматически отмечаем автобус как занятый
        database.busDao().update(bus.copy(busy = true))

        return createLink(
            routesId = routeId,
            busId = busId,
            driverId = driverId,
            isActive = true,
            notes = "Назначен ${java.time.LocalDate.now()}"
        )
    }

    // Генерация тестовых связей
    suspend fun generateTestLinks() {
        // Получаем существующие маршруты и автобусы
        val routes = database.routesDao().getAll()
        val buses = database.busDao().getAll()
        val drivers = database.driverDao().getAll()

        // Используем collect для получения списков
        routes.collect { routesList ->
            buses.collect { busesList ->
                drivers.collect { driversList ->
                    if (routesList.isNotEmpty() && busesList.isNotEmpty() && driversList.isNotEmpty()) {

                        // 1. Связь маршрут 1 - автобус 1 - водитель 1
                        createLink(
                            routesId = routesList[0].routesId,
                            busId = busesList[0].busId,
                            driverId = driversList[0].drivId,
                            isActive = true,
                            notes = "Основной рейс утренний"
                        )

                        // 2. Связь маршрут 1 - автобус 2 - водитель 2
                        createLink(
                            routesId = routesList[0].routesId,
                            busId = busesList[1 % busesList.size].busId,
                            driverId = driversList[1 % driversList.size].drivId,
                            isActive = true,
                            notes = "Запасной автобус"
                        )

                        // 3. Связь маршрут 2 - автобус 2 - водитель 3
                        if (routesList.size > 1) {
                            createLink(
                                routesId = routesList[1].routesId,
                                busId = busesList[1 % busesList.size].busId,
                                driverId = driversList[2 % driversList.size].drivId,
                                isActive = true,
                                notes = "Межгородской маршрут"
                            )
                        }

                        // 4. Связь маршрут 3 - автобус 3 - водитель 1 (повторно)
                        if (routesList.size > 2) {
                            createLink(
                                routesId = routesList[2].routesId,
                                busId = busesList[2 % busesList.size].busId,
                                driverId = driversList[0].drivId,
                                isActive = false,
                                notes = "Маршрут на ремонте"
                            )
                        }

                        // 5. Связь маршрут 1 - автобус 3 - без водителя
                        createLink(
                            routesId = routesList[0].routesId,
                            busId = busesList[2 % busesList.size].busId,
                            driverId = null,
                            isActive = true,
                            notes = "Резерв, требуется водитель"
                        )

                    } else {
                    }
                }
            }
        }
    }
    fun getAllDriversWithDetails(): Flow<List<DriverWithDetails>> =
        database.driverDao().getAllDriversWithDetails()

    // Получить маршруты водителя
    fun getRoutesByDriver(driverId: Int): Flow<List<RouteBusLink>> =
        database.routeBusLinkDao().getRoutesByDriver(driverId)

    // Назначить водителя на маршрут с пересчетом зарплаты
    suspend fun assignDriverToRoute(driverId: Int, routesId: Int, busId: Int): Long {
        // 1. Назначаем водителя на маршрут
        val linkId = database.routeBusLinkDao().assignDriverToRoute(driverId, routesId, busId)

        // 2. Пересчитываем бонус
        recalculateDriverBonus(driverId)

        return linkId
    }

    // Убрать водителя с маршрута
    suspend fun removeDriverFromRoute(linkId: Long, driverId: Int) {
        // 1. Удаляем связь
        val link = database.routeBusLinkDao().getById(linkId)
        link?.let {
            database.routeBusLinkDao().delete(it)
        }

        // 2. Пересчитываем бонус
        recalculateDriverBonus(driverId)
    }

    // Пересчитать бонус водителя
    private suspend fun recalculateDriverBonus(driverId: Int) {
        val activeRoutesCount = database.routeBusLinkDao().getActiveRoutesCount(driverId)

        // 5% за каждый маршрут
        val bonusMultiplier = 1.0 + (activeRoutesCount * 0.05)

        database.driverDao().updateBonusMultiplier(driverId, bonusMultiplier)
    }

    // Получить расчетную зарплату водителя
    suspend fun getDriverSalary(driverId: Int): Double {
        return database.driverDao().getCalculatedSalary(driverId) ?: 0.0
    }

    // Получить всех водителей с их маршрутами
    suspend fun getDriversWithRoutes(): List<DriverRoutesInfo> {
        val drivers = database.driverDao().getAll()
        val result = mutableListOf<DriverRoutesInfo>()

        drivers.collect { driverList ->
            driverList.forEach { driver ->
                val routes = database.routeBusLinkDao().getRoutesByDriver(driver.drivId)
                routes.collect { routeList ->
                    val salary = getDriverSalary(driver.drivId)
                    result.add(DriverRoutesInfo(driver, routeList, salary))
                }
            }
        }

        return result
    }
    fun getBusNumbersByDriverRoute(routeId: String): Flow<List<String>> {
        return database.busDao().getBusNumbersByDriverRoute(routeId)
    }

    // Метод для получения полных объектов Bus
    suspend fun getBusesByDriverRoute(routeId: String): List<Bus> {
        return database.busDao().getBusesByDriverRoute(routeId)
    }
}

// Data class для информации о водителе и его маршрутах
data class DriverRoutesInfo(
    val driver: Driver,
    val routes: List<RouteBusLink>,
    val currentSalary: Double
)
