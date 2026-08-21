package com.example.taskmaster

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmaster.adapter.TaskAdapter
import com.example.taskmaster.database.AppDatabase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var fabAddTask: FloatingActionButton
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = AppDatabase.getDatabase(this)

        recyclerView = findViewById(R.id.recyclerViewTasks)
        fabAddTask = findViewById(R.id.fabAddTask)

        recyclerView.layoutManager = LinearLayoutManager(this)

        taskAdapter = TaskAdapter(
            tasks = emptyList(),

            onTaskChecked = { task, isChecked ->
                lifecycleScope.launch {
                    database.taskDao().updateTask(
                        task.copy(isCompleted = isChecked)
                    )
                }
            },

            onTaskClicked = { task ->
                val intent = Intent(this, AddTaskActivity::class.java)
                intent.putExtra("TASK_ID", task.id)
                startActivity(intent)
            },

            onTaskLongClicked = { task ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Task")
                    .setMessage("Are you sure you want to delete this task?")
                    .setPositiveButton("Delete") { _, _ ->
                        lifecycleScope.launch {
                            database.taskDao().deleteTask(task)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        recyclerView.adapter = taskAdapter

        lifecycleScope.launch {
            database.taskDao().getAllTasks().collectLatest { tasks ->
                taskAdapter.updateTasks(tasks)
            }
        }

        fabAddTask.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            database.taskDao().getAllTasks().collectLatest { tasks ->
                taskAdapter.updateTasks(tasks)
            }
        }
    }
}