package com.bignerbranch.android.zd7_v5.Room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BusDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bus: Bus): Long

    @Update
    suspend fun update(bus: Bus)

    @Delete
    suspend fun delete(bus: Bus)

    @Query("SELECT * FROM Busses ORDER BY busId")
    fun getAll(): Flow<List<Bus>>

    @Query("SELECT * FROM Busses WHERE busId = :id")
    suspend fun getById(id: Int): Bus?

    @Query("""
        SELECT DISTINCT b.number
        FROM Busses b
        INNER JOIN Drivers d ON b.busId = d.busId
        WHERE d.routesId = :routeId
    """)
    fun getBusNumbersByDriverRoute(routeId: String): Flow<List<String>>

    @Query("""
        SELECT DISTINCT b.*
        FROM Busses b
        INNER JOIN Drivers d ON b.busId = d.busId
        WHERE d.routesId = :routeId
    """)
    suspend fun getBusesByDriverRoute(routeId: String): List<Bus>

}