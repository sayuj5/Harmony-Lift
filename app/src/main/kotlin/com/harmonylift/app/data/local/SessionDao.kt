package com.harmonylift.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PracticeSessionEntity)

    @Query("SELECT * FROM practice_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<PracticeSessionEntity>>

    @Query("SELECT SUM(durationMs) FROM practice_sessions")
    fun getTotalPracticeTimeMs(): Flow<Long?>

    @Query("SELECT SUM(score) FROM practice_sessions")
    fun getTotalScore(): Flow<Int?>
}
