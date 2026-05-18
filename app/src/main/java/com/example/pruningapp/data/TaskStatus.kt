package com.example.pruningapp.data

enum class TaskStatus(val value: String) {
    PENDING("pending"),
    DONE("done");

    companion object {
        fun from(value: String): TaskStatus = entries.firstOrNull { it.value == value } ?: PENDING
    }
}

val Task.taskStatus: TaskStatus get() = TaskStatus.from(status)
val Task.isDone: Boolean get() = taskStatus == TaskStatus.DONE
