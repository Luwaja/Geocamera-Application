package edu.uark.lwj003.geocamera.Model

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.Date

class DateConverter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    @TypeConverter
    fun fromDate(date: Date?): String? {
        return date?.let { dateFormat.format(it) }
    }

    @TypeConverter
    fun toDate(dateString: String?): Date? {
        return dateString?.let { dateFormat.parse(it) }
    }
}