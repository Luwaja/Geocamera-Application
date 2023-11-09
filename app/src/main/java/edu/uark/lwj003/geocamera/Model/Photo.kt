package edu.uark.lwj003.geocamera.Model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "photo_table")
data class Photo(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "latitude") var latitude: Double,
    @ColumnInfo(name = "longitude") var longitude: Double,
    @ColumnInfo(name = "timestamp") val timestamp: Date,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "photo_path") val photoPath: String
)
