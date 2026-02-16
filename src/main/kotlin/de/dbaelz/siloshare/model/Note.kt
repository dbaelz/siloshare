package de.dbaelz.siloshare.model

import java.time.Instant

data class Note(
    val id: String,
    val timestamp: Instant,
    val text: String,
    var checklist: Checklist? = null
)
