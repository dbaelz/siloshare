package de.dbaelz.siloshare.model

import java.time.Instant

data class Checklist(
    val items: MutableList<ChecklistItem> = mutableListOf(),
    var updatedAt: Instant? = Instant.now()
)
