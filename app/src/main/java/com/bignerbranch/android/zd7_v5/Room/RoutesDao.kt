package com.bignerbranch.android.zd7_v5.Room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(route: Routes): Long

    @Update
    suspend fun update(route: Routes)

    @Delete
    suspend fun delete(route: Routes)

    @Query("SELECT * FROM Routes ORDER BY routesId")
    fun getAll(): Flow<List<Routes>>

    @Query("SELECT * FROM Routes WHERE routesId = :id")
    suspend fun getById(id: Int): Routes?

    @Query("DELETE FROM Routes WHERE routesId = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM Routes")
    suspend fun deleteAll()
}