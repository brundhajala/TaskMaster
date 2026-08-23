package com.example.taskmaster

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.taskmaster.database.AppDatabase
import com.example.taskmaster.database.Task
import kotlinx.coroutines.launch
import java.util.Calendar

class AddTaskActivity : AppCompatActivity() {

    private lateinit var etTaskTitle: EditText
    private lateinit var etTaskDescription: EditText
    private lateinit var spPriority: Spinner
    private lateinit var btnSelectDate: Button
    private lateinit var tvSelectedDate: TextView
    private lateinit var btnSaveTask: Button
    private lateinit var database: AppDatabase

    private var selectedDate = "No Due Date"
    private var taskId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        database = AppDatabase.getDatabase(this)

        etTaskTitle = findViewById(R.id.etTaskTitle)
        etTaskDescription = findViewById(R.id.etTaskDescription)
        spPriority = findViewById(R.id.spPriority)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        btnSaveTask = findViewById(R.id.btnSaveTask)

        // Priority Spinner
        val priorities = arrayOf("High", "Medium", "Low")

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            priorities
        )

        spPriority.adapter = adapter

        // Date Picker
        btnSelectDate.setOnClickListener {

            val calendar = Calendar.getInstance()

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val picker = DatePickerDialog(
                this,
                { _, y, m, d ->

                    selectedDate = "$d/${m + 1}/$y"
                    tvSelectedDate.text = selectedDate

                },
                year,
                month,
                day
            )

            picker.show()
        }

        taskId = intent.getIntExtra("TASK_ID", -1)

        if (taskId != -1) {

            lifecycleScope.launch {

                val task = database.taskDao().getTaskById(taskId)

                runOnUiThread {

                    if (task != null) {

                        etTaskTitle.setText(task.title)
                        etTaskDescription.setText(task.description)

                        selectedDate = task.dueDate
                        tvSelectedDate.text = selectedDate

                        val position = priorities.indexOf(task.priority)

                        if (position >= 0)
                            spPriority.setSelection(position)
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

            val task = Task(
                id = if (taskId == -1) 0 else taskId,
                title = title,
                description = description,
                priority = spPriority.selectedItem.toString(),
                dueDate = selectedDate,
                isCompleted = false
            )

            lifecycleScope.launch {

                if (taskId == -1)
                    database.taskDao().insertTask(task)
                else
                    database.taskDao().updateTask(task)

                runOnUiThread {

                    Toast.makeText(
                        this@AddTaskActivity,
                        "Task Saved!",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                }
            }
        }
    }
}