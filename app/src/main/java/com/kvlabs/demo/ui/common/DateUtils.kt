package com.kvlabs.demo.ui.common

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {

    private val fullDateFormat = SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    fun formatFullDateTime(timestamp: Long): String {
        return fullDateFormat.format(Date(timestamp))
    }

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    fun getRelativeDueText(dueDate: Long, isCompleted: Boolean): String {
        if (isCompleted) return "Completed"

        val now = System.currentTimeMillis()
        val diff = dueDate - now

        if (diff < 0) {
            val overdueMillis = -diff
            val overdueHours = TimeUnit.MILLISECONDS.toHours(overdueMillis)
            val overdueDays = TimeUnit.MILLISECONDS.toDays(overdueMillis)
            return when {
                overdueDays > 0 -> "Overdue by $overdueDays ${if (overdueDays == 1L) "day" else "days"}"
                overdueHours > 0 -> "Overdue by $overdueHours ${if (overdueHours == 1L) "hr" else "hrs"}"
                else -> "Overdue just now"
            }
        }

        val dueCalendar = Calendar.getInstance().apply { timeInMillis = dueDate }
        val nowCalendar = Calendar.getInstance().apply { timeInMillis = now }

        val isSameDay = dueCalendar.get(Calendar.YEAR) == nowCalendar.get(Calendar.YEAR) &&
                dueCalendar.get(Calendar.DAY_OF_YEAR) == nowCalendar.get(Calendar.DAY_OF_YEAR)

        if (isSameDay) {
            return "Due today, ${formatTime(dueDate)}"
        }

        nowCalendar.add(Calendar.DAY_OF_YEAR, 1)
        val isTomorrow = dueCalendar.get(Calendar.YEAR) == nowCalendar.get(Calendar.YEAR) &&
                dueCalendar.get(Calendar.DAY_OF_YEAR) == nowCalendar.get(Calendar.DAY_OF_YEAR)

        if (isTomorrow) {
            return "Due tomorrow, ${formatTime(dueDate)}"
        }

        val daysLeft = TimeUnit.MILLISECONDS.toDays(diff)
        return if (daysLeft in 2..7) {
            "Due in $daysLeft days"
        } else {
            "Due ${formatDate(dueDate)}"
        }
    }
}
