package com.example.pruningapp.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

// Use ISO_LOCAL_DATE (strict) so that invalid calendar dates (e.g. Feb 29 in non-leap
// years) throw rather than silently rolling over to the next valid date.
private val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd")
    .withResolverStyle(ResolverStyle.STRICT)

// Returns (startDate, endDate) pairs for current + next year, excluding windows
// whose end date is already in the past relative to `today`.
// MM-dd strings that fail to parse are silently skipped.
fun computeTaskDates(
    startMonthDay: String,
    endMonthDay: String,
    today: LocalDate = LocalDate.now()
): List<Pair<LocalDate, LocalDate>> {
    val result = mutableListOf<Pair<LocalDate, LocalDate>>()
    for (yearOffset in 0..1) {
        val year = today.year + yearOffset
        val startDate = try { LocalDate.parse("$year-$startMonthDay", FORMATTER) } catch (_: Exception) { continue }
        val endDate = try { LocalDate.parse("$year-$endMonthDay", FORMATTER) } catch (_: Exception) { continue }
        if (endDate.isBefore(today)) continue
        result += startDate to endDate
    }
    return result
}

// Single source of truth for task generation from a MM-dd pruning rule window.
// Inserts tasks for current year + next year, skipping windows whose end date is already past.
// Deduplicates via countTaskForPlantAndDate before inserting.
suspend fun AppDatabase.generateTasksForRule(
    plantId: Long,
    startMonthDay: String,
    endMonthDay: String,
    type: String
) {
    for ((startDate, endDate) in computeTaskDates(startMonthDay, endMonthDay)) {
        val count = taskDao().countTaskForPlantAndDate(
            plantId, startDate.format(FORMATTER), type
        )
        if (count == 0) {
            taskDao().insertTask(
                Task(
                    plantId = plantId,
                    date = startDate.format(FORMATTER),
                    endDate = endDate.format(FORMATTER),
                    type = type,
                    status = "pending"
                )
            )
        }
    }
}
