package com.example.taskmaster.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val description: String,

    val priority: String,

    val dueDate: String,

    val isCompleted: Boolean = false
)