package de.dbaelz.siloshare.model

data class NewChecklistItem(
    val text: String,
    val done: Boolean = false
)