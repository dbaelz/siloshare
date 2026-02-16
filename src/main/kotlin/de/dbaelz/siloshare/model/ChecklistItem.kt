package de.dbaelz.siloshare.model

data class ChecklistItem(
    val id: String,
    var text: String,
    var done: Boolean = false
)
