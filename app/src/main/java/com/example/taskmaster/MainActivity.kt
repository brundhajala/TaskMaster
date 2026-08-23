package com.example.taskmaster

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmaster.adapter.TaskAdapter
import com.example.taskmaster.database.AppDatabase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var fabAddTask: FloatingActionButton
    private lateinit var etSearch: EditText

    private lateinit var btnAll: Button
    private lateinit var btnCompleted: Button
    private lateinit var btnPending: Button

    private lateinit var database: AppDatabase

    // Firebase Firestore
    private val firestore = FirebaseFirestore.getInstance()

    private var searchJob: Job? = null
    private var loadTasksJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Local Room database
        database = AppDatabase.getDatabase(this)

        recyclerView = findViewById(R.id.recyclerViewTasks)
        fabAddTask = findViewById(R.id.fabAddTask)
        etSearch = findViewById(R.id.etSearch)

        btnAll = findViewById(R.id.btnAll)
        btnCompleted = findViewById(R.id.btnCompleted)
        btnPending = findViewById(R.id.btnPending)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Task Adapter
        taskAdapter = TaskAdapter(
            tasks = emptyList(),

            // When task checkbox changes
            onTaskChecked = { task, isChecked ->

                lifecycleScope.launch {

                    // Update local Room database
                    database.taskDao().updateTask(
                        task.copy(isCompleted = isChecked)
                    )

                    // Update Firebase Firestore
                    uploadTaskToFirestore(
                        taskId = task.id,
                        isCompleted = isChecked
                    )
                }
            },

            // When task is clicked
            onTaskClicked = { task ->

                val intent = Intent(
                    this,
                    AddTaskActivity::class.java
                )

                intent.putExtra(
                    "TASK_ID",
                    task.id
                )

                startActivity(intent)
            },

            // Long press to delete
            onTaskLongClicked = { task ->

                AlertDialog.Builder(this)
                    .setTitle("Delete Task")
                    .setMessage(
                        "Are you sure you want to delete this task?"
                    )
                    .setPositiveButton("Delete") { _, _ ->

                        lifecycleScope.launch {

                            // Delete from Room
                            database.taskDao().deleteTask(task)

                            // Delete from Firestore
                            deleteTaskFromFirestore(task.id)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        recyclerView.adapter = taskAdapter

        // Load local tasks
        loadAllTasks()

        // Sync existing local tasks with Firebase
        syncTasksToFirestore()

        // -------------------------
        // SEARCH
        // -------------------------

        etSearch.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    searchJob?.cancel()

                    searchJob = lifecycleScope.launch {

                        if (s.isNullOrBlank()) {

                            database.taskDao()
                                .getAllTasks()
                                .collectLatest {

                                    taskAdapter.updateTasks(it)
                                }

                        } else {

                            database.taskDao()
                                .searchTasks(s.toString())
                                .collectLatest {

                                    taskAdapter.updateTasks(it)
                                }
                        }
                    }
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                }
            }
        )

        // -------------------------
        // ALL TASKS
        // -------------------------

        btnAll.setOnClickListener {

            loadAllTasks()
        }

        // -------------------------
        // COMPLETED TASKS
        // -------------------------

        btnCompleted.setOnClickListener {

            lifecycleScope.launch {

                database.taskDao()
                    .getCompletedTasks()
                    .collectLatest {

                        taskAdapter.updateTasks(it)
                    }
            }
        }

        // -------------------------
        // PENDING TASKS
        // -------------------------

        btnPending.setOnClickListener {

            lifecycleScope.launch {

                database.taskDao()
                    .getPendingTasks()
                    .collectLatest {

                        taskAdapter.updateTasks(it)
                    }
            }
        }

        // -------------------------
        // ADD TASK
        // -------------------------

        fabAddTask.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    AddTaskActivity::class.java
                )
            )
        }
    }

    // =========================================================
    // LOAD ALL TASKS
    // =========================================================

    private fun loadAllTasks() {

        loadTasksJob?.cancel()

        loadTasksJob = lifecycleScope.launch {

            database.taskDao()
                .getAllTasks()
                .collectLatest {

                    taskAdapter.updateTasks(it)
                }
        }
    }

    // =========================================================
    // FIREBASE - UPLOAD TASK
    // =========================================================

    private fun uploadTaskToFirestore(
        taskId: Any,
        isCompleted: Boolean
    ) {

        val taskData = hashMapOf<String, Any>(
            "taskId" to taskId.toString(),
            "isCompleted" to isCompleted
        )

        firestore
            .collection("tasks")
            .document(taskId.toString())
            .set(taskData)
            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Task synced with Firebase",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    this,
                    "Firebase sync failed: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    // =========================================================
    // FIREBASE - DELETE TASK
    // =========================================================

    private fun deleteTaskFromFirestore(
        taskId: Any
    ) {

        firestore
            .collection("tasks")
            .document(taskId.toString())
            .delete()
            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Task deleted from Firebase",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    this,
                    "Firebase delete failed: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    // =========================================================
    // FIREBASE - SYNC EXISTING LOCAL TASKS
    // =========================================================

    private fun syncTasksToFirestore() {

        lifecycleScope.launch {

            try {

                val tasks = database.taskDao()
                    .getAllTasks()
                    .first()

                for (task in tasks) {

                    uploadTaskToFirestore(
                        taskId = task.id,
                        isCompleted = task.isCompleted
                    )
                }

            } catch (e: Exception) {

                Toast.makeText(
                    this@MainActivity,
                    "Unable to sync tasks with Firebase",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // =========================================================
    // ACTIVITY RESUME
    // =========================================================

    override fun onResume() {

        super.onResume()

        loadAllTasks()

        // Sync local tasks again when returning to the screen
        syncTasksToFirestore()
    }
}