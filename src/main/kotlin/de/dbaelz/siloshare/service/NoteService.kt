package de.dbaelz.siloshare.service

import de.dbaelz.siloshare.model.Note
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

interface NoteService {
    fun add(text: String): Note
    fun getAll(): List<Note>
}

class InMemoryNoteService : NoteService {
    private val entries = ConcurrentHashMap<String, Note>()

    override fun add(text: String): Note {
        val id = UUID.randomUUID().toString()

        val entry = Note(
            id = id,
            timestamp = Instant.now(),
            text = text
        )
        entries[id] = entry

        return entry
    }

    override fun getAll(): List<Note> {
        val removeTime = Instant.now().minusSeconds(DEFAULT_REMOVE_SECONDS)

        synchronized(entries) {
            val validEntries = entries.entries
                .filter { it.value.timestamp.isAfter(removeTime) }
                .associate { it.key to it.value }

            entries.clear()
            entries.putAll(validEntries)

            return entries.values.toList()
        }
    }

    private companion object {
        const val DEFAULT_REMOVE_SECONDS = 7200L
    }
}
