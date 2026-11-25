package com.kvlabs.demo.domain.model

enum class Priority(val displayName: String, val level: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3);

    companion object {
        fun fromLevel(level: Int): Priority =
            entries.find { it.level == level } ?: MEDIUM
    }
}
