package com.harmonylift.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "practice_sessions")
data class PracticeSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val durationMs: Long,
    val notesDetected: Int,
    val pitchStability: Float,
    val accuracy: Float,
    val score: Int,
    val mode: String,
    val instrument: String
)
