package com.yesitlab.zyvo

enum class MetricMode {
    COUNTER,
    CLOCK;

    companion object {
        fun find(value: Int): MetricMode = values().firstOrNull { it.ordinal == value } ?: COUNTER
    }
}