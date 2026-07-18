package com.harmonylift.app.data

import android.content.Context
import com.harmonylift.app.data.local.HarmonyDatabase
import com.harmonylift.app.data.local.PracticeSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PracticeRepository(context: Context) {
    private val database = HarmonyDatabase.getDatabase(context)
    private val sessionDao = database.sessionDao()

    val totalXp: Flow<Int> = sessionDao.getTotalScore().map { it ?: 0 }
    
    val allSessions: Flow<List<PracticeSessionEntity>> = sessionDao.getAllSessions()
    
    val totalPracticeTimeMs: Flow<Long> = sessionDao.getTotalPracticeTimeMs().map { it ?: 0L }

    suspend fun saveSession(session: PracticeSessionEntity) {
        sessionDao.insertSession(session)
    }

    // Still need a synchronous way to add XP if called from non-coroutine scope,
    // but in V5.1 we use saveSession which has the score directly.
    
    fun getLevelFromXp(xp: Int): Int {
        return (xp / 500) + 1
    }
}
