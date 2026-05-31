package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "substances")
data class Substance(
    @PrimaryKey val formula: String,
    val name: String,
    val description: String,
    val category: String,
    val colorHex: String,
    val isElement: Boolean,
    val atomicNumber: Int = 0,
    val periodicPeriod: Int = 0, // Row (1-7)
    val periodicGroup: Int = 0,  // Column (1-18)
    val discovered: Boolean = false,
    val isStarter: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
