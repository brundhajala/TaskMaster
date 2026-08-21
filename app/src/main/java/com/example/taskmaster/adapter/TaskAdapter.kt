package com.example.taskmaster.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmaster.R
import com.example.taskmaster.database.Task

class TaskAdapter(
    private var tasks: List<Task>,
    private val onTaskChecked: (Task, Boolean) -> Unit,
    private val onTaskClicked: (Task) -> Unit,
    private val onTaskLongClicked: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val title: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val description: TextView = itemView.findViewById(R.id.tvTaskDescription)
        val priority: TextView = itemView.findViewById(R.id.tvTaskPriority)
        val dueDate: TextView = itemView.findViewById(R.id.tvTaskDueDate)
        val completed: CheckBox = itemView.findViewById(R.id.cbTaskCompleted)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)

        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {

        val task = tasks[position]

        holder.title.text = task.title
        holder.description.text = task.description
        holder.priority.text = "Priority: ${task.priority}"
        holder.dueDate.text = "Due: ${task.dueDate}"

        holder.completed.setOnCheckedChangeListener(null)
        holder.completed.isChecked = task.isCompleted

        holder.completed.setOnCheckedChangeListener { _, isChecked ->
            onTaskChecked(task, isChecked)
        }

        holder.itemView.setOnClickListener {
            onTaskClicked(task)
        }

        holder.itemView.setOnLongClickListener {
            onTaskLongClicked(task)
            true
        }
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}