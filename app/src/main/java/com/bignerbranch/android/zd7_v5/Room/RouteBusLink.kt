package com.bignerbranch.android.zd7_v5.Room

import androidx.room.*

@Entity(
    tableName = "route_bus_links",
    foreignKeys = [
        ForeignKey(
            entity = Routes::class,
            parentColumns = ["routesId"],
            childColumns = ["routesId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Bus::class,
            parentColumns = ["busId"],
            childColumns = ["busId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(  // Добавляем связь с водителем
            entity = Driver::class,
            parentColumns = ["drivId"],
            childColumns = ["driverId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["routesId", "busId", "driverId"], unique = true, name = "idx_unique_assignment"),
        Index(value = ["routesId"], name = "idx_route"),
        Index(value = ["busId"], name = "idx_bus"),
        Index(value = ["driverId"], name = "idx_driver")
    ]
)
data class RouteBusLink(
    @PrimaryKey(autoGenerate = true)
    val linkId: Long = 0,

    @ColumnInfo(name = "routesId")
    val routesId: Int,

    @ColumnInfo(name = "busId")
    val busId: Int,

    @ColumnInfo(name = "driverId")  // Теперь обязательное поле
    val driverId: Int?,

    @ColumnInfo(name = "isActive")
    val isActive: Boolean = true,

    @ColumnInfo(name = "notes")
    val notes: String? = null
)