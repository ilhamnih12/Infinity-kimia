package com.example.data.local

import androidx.room.*
import com.example.data.model.Substance
import kotlinx.coroutines.flow.Flow

@Dao
interface SubstanceDao {
    @Query("SELECT * FROM substances ORDER BY isElement DESC, atomicNumber ASC, formula ASC")
    fun getAllSubstances(): Flow<List<Substance>>

    @Query("SELECT * FROM substances WHERE discovered = 1 ORDER BY timestamp DESC")
    fun getDiscoveredSubstances(): Flow<List<Substance>>

    @Query("SELECT * FROM substances WHERE formula = :formula LIMIT 1")
    suspend fun getSubstanceByFormula(formula: String): Substance?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSubstance(substance: Substance)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSubstances(substances: List<Substance>)

    @Query("UPDATE substances SET discovered = 1, timestamp = :timestamp WHERE formula = :formula")
    suspend fun markAsDiscovered(formula: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM substances")
    suspend fun getCount(): Int

    @Update
    suspend fun updateSubstance(substance: Substance)
}
