package com.example.taskmaster.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY id DESC")
    fun getCompletedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY id DESC")
    fun getPendingTasks(): Flow<List<Task>>

    @Query("""
        SELECT * FROM tasks
        WHERE title LIKE '%' || :searchQuery || '%'
        OR description LIKE '%' || :searchQuery || '%'
        ORDER BY id DESC
    """)
    fun searchTasks(searchQuery: String): Flow<List<Task>>

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTotalTaskCount(): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1")
    suspend fun getCompletedTaskCount(): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0")
    suspend fun getPendingTaskCount(): Int


    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): Task?
}