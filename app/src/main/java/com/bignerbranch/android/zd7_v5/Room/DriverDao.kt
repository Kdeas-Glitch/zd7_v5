package com.bignerbranch.android.zd7_v5.Room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DriverDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(driver: Driver): Long

    @Update
    suspend fun update(driver: Driver)

    @Delete
    suspend fun delete(driver: Driver)

    @Query("SELECT * FROM Drivers ORDER BY drivId")
    fun getAll(): Flow<List<Driver>>

    @Query("SELECT * FROM Drivers WHERE drivId = :id")
    suspend fun getById(id: Int): Driver?

    @Query("SELECT * FROM drivers WHERE name = :name LIMIT 1")
    suspend fun getDriverByName(name: String): Driver?
    @Query("UPDATE Drivers SET bonusMultiplier = :multiplier WHERE drivId = :driverId")
    suspend fun updateBonusMultiplier(driverId: Int, multiplier: Double)
    @Query("SELECT * FROM Drivers ORDER BY name")
    fun getAllDrivers(): Flow<List<Driver>>

    // Получить расчетную зарплату
    @Query("SELECT baseSalary * bonusMultiplier FROM Drivers WHERE drivId = :driverId")
    suspend fun getCalculatedSalary(driverId: Int): Double?

    // Получить водителей с их расчетными зарплатами
    @Query("""
        SELECT d.*, 
               (d.baseSalary * d.bonusMultiplier) as calculatedSalary,
               COUNT(rbl.linkId) as routeCount
        FROM Drivers d
        LEFT JOIN route_bus_links rbl ON d.drivId = rbl.driverId AND rbl.isActive = 1
        GROUP BY d.drivId
        ORDER BY d.drivId
    """)
    fun getAllDriversWithDetails(): Flow<List<DriverWithDetails>>
}

// Новый data class для расширенной информации
data class DriverWithDetails(
    @Embedded
    val driver: Driver,

    @ColumnInfo(name = "calculatedSalary")
    val calculatedSalary: Double,

    @ColumnInfo(name = "routeCount")
    val routeCount: Int
)
