package com.bignerbranch.android.zd7_v5.Room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteBusLinkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(link: RouteBusLink): Long

    @Update
    suspend fun update(link: RouteBusLink)

    @Delete
    suspend fun delete(link: RouteBusLink)

    @Query("SELECT * FROM route_bus_links ORDER BY linkId")
    fun getAll(): Flow<List<RouteBusLink>>

    @Query("SELECT * FROM route_bus_links WHERE linkId = :id")
    suspend fun getById(id: Long): RouteBusLink?

    @Query("SELECT * FROM route_bus_links WHERE routesId = :routesId AND busId = :busId")
    suspend fun getByRouteAndBus(routesId: Int, busId: Int): RouteBusLink?

    // Автоматическое создание связи с проверкой
    @Transaction
    suspend fun createOrUpdateLink(
        routesId: Int,
        busId: Int,
        driverId: Int? = null,
        isActive: Boolean = true,
        notes: String? = null
    ): Long {
        // Проверяем, существует ли уже такая связь
        val existingLink = getByRouteAndBus(routesId, busId)

        return if (existingLink != null) {
            // Обновляем существующую связь
            val updatedLink = existingLink.copy(
                driverId = driverId ?: existingLink.driverId,
                isActive = isActive,
                notes = notes ?: existingLink.notes
            )
            update(updatedLink)
            existingLink.linkId
        } else {
            // Создаем новую связь
            val newLink = RouteBusLink(
                routesId = routesId,
                busId = busId,
                driverId = driverId!!,
                isActive = isActive,
                notes = notes
            )
            insert(newLink)
        }
    }

    // Создание связи с автоматическим назначением свободного водителя
    @Transaction
    suspend fun createLinkWithAutoDriver(
        routesId: Int,
        busId: Int,
        notes: String? = null
    ): Long {
        // Здесь можно добавить логику поиска свободного водителя
        // Пока просто создаем связь без водителя
        return createOrUpdateLink(
            routesId = routesId,
            busId = busId,
            notes = notes
        )
    }

    // Получение всех активных связей
    @Query("SELECT * FROM route_bus_links WHERE isActive = 1")
    fun getActiveLinks(): Flow<List<RouteBusLink>>

    // Получение связей по маршруту
    @Query("SELECT * FROM route_bus_links WHERE routesId = :routesId")
    fun getLinksByRoute(routesId: Int): Flow<List<RouteBusLink>>

    // Получение связей по автобусу
    @Query("SELECT * FROM route_bus_links WHERE busId = :busId")
    fun getLinksByBus(busId: Int): Flow<List<RouteBusLink>>

    @Query("SELECT * FROM route_bus_links WHERE driverId = :driverId")
    fun getRoutesByDriver(driverId: Int): Flow<List<RouteBusLink>>

    // Получить количество активных маршрутов водителя
    @Query("SELECT COUNT(*) FROM route_bus_links WHERE driverId = :driverId AND isActive = 1")
    suspend fun getActiveRoutesCount(driverId: Int): Int

    // Получить всех водителей на маршруте
    @Query("SELECT * FROM route_bus_links WHERE routesId = :routesId")
    fun getDriversByRoute(routesId: Int): Flow<List<RouteBusLink>>

    // Назначить водителя на маршрут
    @Transaction
    suspend fun assignDriverToRoute(driverId: Int, routesId: Int, busId: Int): Long {
        // Проверяем, не назначен ли уже водитель на этот маршрут
        val existing = getByDriverAndRoute(driverId, routesId)
        if (existing != null) {
            return existing.linkId
        }

        // Создаем новую связь
        val link = RouteBusLink(
            routesId = routesId,
            busId = busId,
            driverId = driverId,
            isActive = true,
            notes = "Назначен ${java.time.LocalDate.now()}"
        )
        return insert(link)
    }

    // Получить связь по водителю и маршруту
    @Query("SELECT * FROM route_bus_links WHERE driverId = :driverId AND routesId = :routesId")
    suspend fun getByDriverAndRoute(driverId: Int, routesId: Int): RouteBusLink?
}