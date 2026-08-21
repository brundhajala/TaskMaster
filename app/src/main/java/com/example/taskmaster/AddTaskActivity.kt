package com.example.taskmaster

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.taskmaster.database.AppDatabase
import com.example.taskmaster.database.Task
import kotlinx.coroutines.launch

class AddTaskActivity : AppCompatActivity() {

    private lateinit var etTaskTitle: EditText
    private lateinit var etTaskDescription: EditText
    private lateinit var btnSaveTask: Button
    private lateinit var database: AppDatabase

    private var taskId: Int = -1
    private var currentTask: Task? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        database = AppDatabase.getDatabase(this)

        etTaskTitle = findViewById(R.id.etTaskTitle)
        etTaskDescription = findViewById(R.id.etTaskDescription)
        btnSaveTask = findViewById(R.id.btnSaveTask)

        // Check if we're editing an existing task
        taskId = intent.getIntExtra("TASK_ID", -1)

        if (taskId != -1) {
            lifecycleScope.launch {
                currentTask = database.taskDao().getTaskById(taskId)

                runOnUiThread {
                    currentTask?.let { task ->
                        etTaskTitle.setText(task.title)
                        etTaskDescription.setText(task.description)
                        btnSaveTask.text = "Update Task"
                    }
                }
            }
        }

        btnSaveTask.setOnClickListener {

            val title = etTaskTitle.text.toString().trim()
            val description = etTaskDescription.text.toString().trim()

            if (title.isEmpty()) {
                etTaskTitle.error = "Enter task title"
                return@setOnClickListener
            }

            lifecycleScope.launch {

                if (taskId == -1) {
                    // Add new task
                    val task = Task(
                        title = title,
                        description = description,
                        priority = "Medium",
                        dueDate = "No Due Date",
                        isCompleted = false
                    )

                    database.taskDao().insertTask(task)

                } else {
                    // Update existing task
                    currentTask?.let { oldTask ->
                        val updatedTask = oldTask.copy(
                            title = title,
                            description = description
                        )

                        database.taskDao().updateTask(updatedTask)
                    }
                }

                runOnUiThread {
                    Toast.makeText(
                        this@AddTaskActivity,
                        if (taskId == -1) "Task Added!" else "Task Updated!",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                }
            }
        }
    }
}